package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepExecution
import nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandler
import java.io.OutputStream

class LatexStepLogTabComponentTest : BasePlatformTestCase() {

    fun testTreeUpdatesWithStepStatusesAndParsedMessages() {
        val mainFile = myFixture.addFileToProject(
            "main.tex",
            """
            \\documentclass{article}
            \\begin{document}
            hi
            \\end{document}
            """.trimIndent()
        ).virtualFile

        val compile = TestProcessHandler()
        val viewer = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(0, "latex-compile", "Compile LaTeX", compile),
                LatexStepExecution(1, "pdf-viewer", "Open PDF viewer", viewer),
            )
        )
        val tab = LatexStepLogTabComponent(project, mainFile, handler)

        handler.startNotify()
        compile.emit("LaTeX Warning: Citation 'abc' on page 1 undefined on input line 7.\n")
        compile.finish(0)
        viewer.emit("viewer opened\n")
        viewer.finish(0)

        flushEdt()

        assertTrue(tab.stepStatus(0) == "WARNING" || tab.stepStatus(0) == "SUCCEEDED")
        assertEquals("SUCCEEDED", tab.stepStatus(1))
    }

    fun testFailedRunMarksPendingStepsAsSkipped() {
        val mainFile = myFixture.addFileToProject("main2.tex", "\\documentclass{article}").virtualFile

        val compile = TestProcessHandler()
        val bib = TestProcessHandler()
        val viewer = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(0, "latex-compile", "Compile LaTeX", compile),
                LatexStepExecution(1, "legacy-bibtex", "Run bibliography", bib),
                LatexStepExecution(2, "pdf-viewer", "Open PDF viewer", viewer),
            )
        )
        val tab = LatexStepLogTabComponent(project, mainFile, handler)

        handler.startNotify()
        compile.emit("./main2.tex:4: LaTeX Error: Missing $ inserted.\n")
        compile.finish(1)

        flushEdt()

        assertEquals("FAILED", tab.stepStatus(0))
        assertEquals("SKIPPED", tab.stepStatus(1))
        assertEquals("SKIPPED", tab.stepStatus(2))
    }

    private fun flushEdt() {
        ApplicationManager.getApplication().invokeAndWait {}
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
