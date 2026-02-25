package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunExecutionState
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.ProcessLatexRunStep
import java.io.OutputStream

class LatexStepLogTabComponentTest : BasePlatformTestCase() {

    fun testUsesOnePixelSplitterAndConsoleToolbarActions() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}").virtualFile
        val context = createContext(mainFile)
        val compile = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", compile, "Compile LaTeX"),
            ),
            context,
        )
        val tab = LatexStepLogTabComponent(project, mainFile, handler)
        try {
            assertEquals("OnePixelSplitter", tab.splitterClassNameForTest())
            assertTrue(tab.hasEmbeddedConsoleToolbarForTest())
            assertFalse(tab.usesAdditionalTabToolbarForTest())
        }
        finally {
            tab.dispose()
        }
    }

    fun testTreeUpdatesWithStepStatusesAndRootMergedOutput() {
        val mainFile = myFixture.addFileToProject(
            "main.tex",
            """
            \\documentclass{article}
            \\begin{document}
            hi
            \\end{document}
            """.trimIndent()
        ).virtualFile

        val context = createContext(mainFile)
        val compile = TestProcessHandler()
        val viewer = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", compile, "Compile LaTeX"),
                TestProcessStep("pdf-viewer", "s2", viewer, "Open PDF viewer"),
            ),
            context,
        )
        val tab = LatexStepLogTabComponent(project, mainFile, handler)
        try {
            handler.startNotify()
            compile.emit("compile output\n")
            compile.emit("LaTeX Warning: Citation 'abc' on page 1 undefined on input line 7.\n")
            compile.finish(0)
            viewer.emit("viewer output\n")
            viewer.finish(0)

            flushEdt()

            assertTrue(tab.stepStatus(0) == "WARNING" || tab.stepStatus(0) == "SUCCEEDED")
            assertEquals("SUCCEEDED", tab.stepStatus(1))
            assertEquals(1, tab.renderedOutputStepIndex())
            assertEquals("viewer output\n", tab.renderedOutputForTest())

            tab.selectStepForTest(0)
            flushEdt()
            assertEquals(0, tab.renderedOutputStepIndex())
            assertTrue(tab.renderedOutputForTest().contains("compile output"))

            tab.selectRootForTest()
            flushEdt()
            assertEquals(null, tab.renderedOutputStepIndex())
            assertEquals(
                "compile output\n" +
                    "LaTeX Warning: Citation 'abc' on page 1 undefined on input line 7.\n" +
                    "viewer output\n",
                tab.renderedOutputForTest()
            )
        }
        finally {
            tab.dispose()
        }
    }

    fun testFailedRunMarksPendingStepsAsSkipped() {
        val mainFile = myFixture.addFileToProject("main2.tex", "\\documentclass{article}").virtualFile

        val context = createContext(mainFile)
        val compile = TestProcessHandler()
        val bib = TestProcessHandler()
        val viewer = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", compile, "Compile LaTeX"),
                TestProcessStep("bibtex", "s2", bib, "Run bibliography"),
                TestProcessStep("pdf-viewer", "s3", viewer, "Open PDF viewer"),
            ),
            context,
        )
        val tab = LatexStepLogTabComponent(project, mainFile, handler)
        try {
            handler.startNotify()
            compile.emit("./main2.tex:4: LaTeX Error: Missing $ inserted.\n")
            compile.finish(1)

            flushEdt()

            assertEquals("FAILED", tab.stepStatus(0))
            assertEquals("SKIPPED", tab.stepStatus(1))
            assertEquals("SKIPPED", tab.stepStatus(2))
        }
        finally {
            tab.dispose()
        }
    }

    private fun createContext(mainFile: VirtualFile): LatexRunStepContext {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunExecutionState(resolvedMainFile = mainFile)
        return LatexRunStepContext(runConfig, environment, state, mainFile)
    }

    private fun flushEdt() {
        ApplicationManager.getApplication().invokeAndWait {}
    }

    private class TestProcessStep(
        override val id: String,
        override val configId: String,
        private val handler: ProcessHandler,
        override val displayName: String,
    ) : ProcessLatexRunStep {

        override fun createProcess(context: LatexRunStepContext): ProcessHandler = handler
    }

    private class TestProcessHandler : ProcessHandler() {

        override fun startNotify() {
            super.startNotify()
        }

        fun emit(text: String, outputType: Key<*> = ProcessOutputTypes.STDOUT) {
            notifyTextAvailable(text, outputType)
        }

        fun finish(exitCode: Int) {
            notifyProcessTerminated(exitCode)
        }

        override fun destroyProcessImpl() {
            notifyProcessTerminated(-1)
        }

        override fun detachProcessImpl() {
            notifyProcessDetached()
        }

        override fun detachIsDefault(): Boolean = false

        override fun getProcessInput(): OutputStream? = null
    }
}
