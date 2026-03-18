package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.test.assertFailsWith
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.PdfViewerStepOptions
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceViewer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import java.nio.file.Files
import java.nio.file.Path

class PdfViewerRunStepTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = null
            unmockkAll()
        }
        finally {
            super.tearDown()
        }
    }

    fun testStandardViewerExecutesDirectlyInBeforeStart() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockNoViewer()

        step.beforeStart(context)

        assertNull(step.createProcess(context))
        verify(exactly = 1) { NoViewer.openFile(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { NoViewer.forwardSearch(any(), any(), any(), any(), any()) }
    }

    fun testStandardViewerUpdatesForwardSearchActionViewer() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockNoViewer()

        step.beforeStart(context)

        val action = ActionManager.getInstance().getAction("texify.ForwardSearch")
        if (action is ForwardSearchAction) {
            assertEquals(NoViewer, action.viewer)
        }
    }

    fun testStandardViewerSwallowsTeXception() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockkObject(NoViewer)
        every { NoViewer.openFile(any(), any(), any(), any(), any()) } throws TeXception("viewer failed")

        step.beforeStart(context)

        verify(exactly = 1) { NoViewer.openFile(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { NoViewer.forwardSearch(any(), any(), any(), any(), any()) }
    }

    fun testStandardViewerPropagatesNonTexception() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockkObject(NoViewer)
        every { NoViewer.openFile(any(), any(), any(), any(), any()) } throws IllegalStateException("boom")

        val error = assertFailsWith<IllegalStateException> {
            step.beforeStart(context)
        }

        assertEquals("boom", error.message)
    }

    fun testCustomViewerCreatesRealProcessHandler() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = CustomPdfViewer.name
                customViewerCommand = "viewer {pdf} --flag"
            }
        )
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        var capturedWorkingDirectory: Path? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            capturedWorkingDirectory = arg(2)
            expectedHandler
        }

        step.beforeStart(context)
        val process = step.createProcess(context)

        assertEquals(expectedHandler, process)
        assertEquals(listOf("viewer", context.session.resolvedOutputFilePath!!, "--flag"), capturedCommand)
        assertEquals(context.session.workingDirectory, capturedWorkingDirectory)
    }

    fun testCustomViewerAppendsPdfPathWhenPlaceholderMissing() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = CustomPdfViewer.name
                customViewerCommand = "viewer --flag"
            }
        )
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            expectedHandler
        }

        val process = step.createProcess(context)

        assertEquals(expectedHandler, process)
        assertEquals(listOf("viewer", "--flag", context.session.resolvedOutputFilePath!!), capturedCommand)
    }

    fun testNonCustomViewerSkipsCreateProcessEvenWhenCustomCommandExists() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
                customViewerCommand = "viewer {pdf}"
            }
        )
        mockNoViewer()

        step.beforeStart(context)

        assertNull(step.createProcess(context))
        verify(exactly = 0) { NoViewer.openFile(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { NoViewer.forwardSearch(any(), any(), any(), any(), any()) }
    }

    fun testCustomViewerWithoutCommandIsNoOp() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = CustomPdfViewer.name
                customViewerCommand = null
            }
        )

        step.beforeStart(context)

        assertNull(step.createProcess(context))
    }

    fun testAutoCompileModeIsNoOpForStandardAndCustomViewer() {
        val standardContext = createContext().also { it.runConfig.isAutoCompiling = true }
        val standardStep = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockNoViewer()

        standardStep.beforeStart(standardContext)

        assertNull(standardStep.createProcess(standardContext))
        verify(exactly = 0) { NoViewer.openFile(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { NoViewer.forwardSearch(any(), any(), any(), any(), any()) }

        val customContext = createContext().also { it.runConfig.isAutoCompiling = true }
        val customStep = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = CustomPdfViewer.name
                customViewerCommand = "viewer {pdf}"
            }
        )

        assertNull(customStep.createProcess(customContext))
    }

    fun testMissingOutputPathIsNoOpForStandardAndCustomViewer() {
        val standardContext = createContext(outputFilePath = null)
        val standardStep = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockNoViewer()

        standardStep.beforeStart(standardContext)

        assertNull(standardStep.createProcess(standardContext))
        verify(exactly = 0) { NoViewer.openFile(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { NoViewer.forwardSearch(any(), any(), any(), any(), any()) }

        val customContext = createContext(outputFilePath = null)
        val customStep = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = CustomPdfViewer.name
                customViewerCommand = "viewer {pdf}"
            }
        )

        assertNull(customStep.createProcess(customContext))
    }

    fun testDisplayNameUsesConfiguredViewerName() {
        val stepOptions = PdfViewerStepOptions().apply {
            pdfViewerName = EvinceViewer.name
        }

        assertEquals(stepOptions.displayName(), PdfViewerRunStep(stepOptions).displayName)
    }

    private fun mockNoViewer() {
        mockkObject(NoViewer)
        every { NoViewer.openFile(any(), any(), any(), any(), any()) } answers { }
        every { NoViewer.forwardSearch(any(), any(), any(), any(), any()) } answers { }
    }

    private fun createContext(outputFilePath: String? = "main.pdf"): LatexRunStepContext {
        val root = Files.createTempDirectory("texify-pdf-viewer-step")
        val mainFilePath = root.resolve("main.tex")
        val outputDirPath = Files.createDirectories(root.resolve("out"))
        Files.writeString(mainFilePath, "\\\\documentclass{article}")
        outputFilePath?.let { Files.writeString(outputDirPath.resolve(it), "pdf") }

        val mainFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mainFilePath.toString())!!
        val outputDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDirPath.toString())!!

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        runConfig.mainFilePath = mainFilePath.toString()

        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = outputDir,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = outputDir,
            resolvedOutputFilePath = outputFilePath?.let(outputDirPath::resolve)?.toString(),
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
