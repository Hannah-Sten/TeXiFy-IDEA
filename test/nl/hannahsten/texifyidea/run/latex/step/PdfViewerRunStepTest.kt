package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.action.ForwardSearchAction
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceViewer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.updateFilesets
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.selectedTextEditor
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFailsWith

class PdfViewerRunStepTest : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            (ActionManager.getInstance().getAction("texify.ForwardSearch") as? ForwardSearchAction)?.viewer = null
            PdfViewer.additionalViewers = emptyList()
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
        // Using a mockk object would give an exception: java.lang.UnsupportedOperationException: class redefinition failed: attempted to change the schema
        val viewer = mockNoViewer()

        step.beforeStart(context)

        assertNull(step.createProcess(context))
        assertEquals(1, viewer.openFileCalls.size)
        assertEquals(1, viewer.forwardSearchCalls.size)
    }

    fun testStandardViewerUpdatesForwardSearchActionViewer() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        val viewer = mockNoViewer()

        step.beforeStart(context)

        val action = ActionManager.getInstance().getAction("texify.ForwardSearch")
        if (action is ForwardSearchAction) {
            assertEquals(viewer, action.viewer)
        }
    }

    fun testStandardViewerSwallowsTeXception() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        val viewer = mockNoViewer(openFileError = TeXception("viewer failed"))

        step.beforeStart(context)

        assertEquals(1, viewer.openFileCalls.size)
        assertEquals(0, viewer.forwardSearchCalls.size)
    }

    fun testStandardViewerPropagatesNonTexception() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        mockNoViewer(openFileError = IllegalStateException("boom"))

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

    fun testCustomViewerCommandTakesPrecedenceOverStoredViewerName() {
        val context = createContext()
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
                customViewerCommand = "viewer {pdf}"
            }
        )
        val viewer = mockNoViewer()
        val expectedHandler = mockk<KillableProcessHandler>(relaxed = true)
        var capturedCommand: List<String>? = null
        mockkStatic("nl.hannahsten.texifyidea.run.common.CompilationProcessFactoryKt")
        every { createCompilationHandler(any(), any(), any(), any()) } answers {
            capturedCommand = secondArg()
            expectedHandler
        }

        step.beforeStart(context)
        val process = step.createProcess(context)

        assertEquals(expectedHandler, process)
        assertEquals(listOf("viewer", context.session.resolvedOutputFilePath!!), capturedCommand)
        assertEquals(0, viewer.openFileCalls.size)
        assertEquals(0, viewer.forwardSearchCalls.size)
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
        val viewer = mockNoViewer()

        standardStep.beforeStart(standardContext)

        assertNull(standardStep.createProcess(standardContext))
        assertEquals(0, viewer.openFileCalls.size)
        assertEquals(0, viewer.forwardSearchCalls.size)

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
        val viewer = mockNoViewer()

        standardStep.beforeStart(standardContext)

        assertNull(standardStep.createProcess(standardContext))
        assertEquals(0, viewer.openFileCalls.size)
        assertEquals(0, viewer.forwardSearchCalls.size)

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

    fun testStandardViewerIgnoresEditorOutsideCurrentFileset() {
        val main = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
            \input{chapter}
            \end{document}
            """.trimIndent()
        ).virtualFile
        myFixture.addFileToProject("chapter.tex", "chapter")
        val unrelated = myFixture.addFileToProject("other.tex", "other")
        myFixture.updateFilesets()

        val context = createProjectContext(main)
        val step = PdfViewerRunStep(
            PdfViewerStepOptions().apply {
                pdfViewerName = NoViewer.name
            }
        )
        val viewer = mockNoViewer()
        mockkStatic("nl.hannahsten.texifyidea.util.ProjectsKt")
        myFixture.openFileInEditor(unrelated.virtualFile)
        val textEditor = FileEditorManager.getInstance(project).selectedEditor as TextEditor
        every { project.focusedTextEditor() } returns textEditor
        every { project.selectedTextEditor() } returns textEditor

        step.beforeStart(context)

        // It doesn't make sense to forward search if the caret is in an unrelated file, since the user probably wants to keep looking at the same page
        assertEquals(0, viewer.forwardSearchCalls.size)
    }

    private fun mockNoViewer(openFileError: Throwable? = null): RecordingPdfViewer = RecordingPdfViewer(openFileError).also {
        PdfViewer.additionalViewers = listOf(it)
    }

    private class RecordingPdfViewer(
        private val openFileError: Throwable? = null,
    ) : PdfViewer {

        override val name: String = NoViewer.name
        override val displayName: String = NoViewer.displayName

        val openFileCalls = mutableListOf<OpenFileCall>()
        val forwardSearchCalls = mutableListOf<ForwardSearchCall>()

        override fun isAvailable(): Boolean = true

        override fun openFile(pdfPath: String, project: Project, newWindow: Boolean, focusAllowed: Boolean, forceRefresh: Boolean) {
            openFileCalls += OpenFileCall(pdfPath, project, newWindow, focusAllowed, forceRefresh)
            openFileError?.let { throw it }
        }

        override fun forwardSearch(outputPath: String?, sourceFilePath: String, line: Int, project: Project, focusAllowed: Boolean) {
            forwardSearchCalls += ForwardSearchCall(outputPath, sourceFilePath, line, project, focusAllowed)
        }
    }

    private data class OpenFileCall(
        val pdfPath: String,
        val project: Project,
        val newWindow: Boolean,
        val focusAllowed: Boolean,
        val forceRefresh: Boolean,
    )

    private data class ForwardSearchCall(
        val outputPath: String?,
        val sourceFilePath: String,
        val line: Int,
        val project: Project,
        val focusAllowed: Boolean,
    )

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
            editorContext = TextEditorContextSnapshot(focused = TextEditorSnapshot(mainFilePath.toString(), 1), selected = null)
        )
        return LatexRunStepContext(runConfig, environment, state)
    }

    private fun createProjectContext(mainFile: com.intellij.openapi.vfs.VirtualFile): LatexRunStepContext {
        val outputDirPath = Files.createDirectories(Path.of(project.basePath!!, "out"))
        val outputDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDirPath.toString())!!
        Files.writeString(outputDirPath.resolve("main.pdf"), "pdf")

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        runConfig.mainFilePath = mainFile.path

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
            resolvedOutputFilePath = outputDirPath.resolve("main.pdf").toString(),
        )
        return LatexRunStepContext(runConfig, environment, state)
    }
}
