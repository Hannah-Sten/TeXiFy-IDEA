package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.util.Key
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.OutputStream

class StepAwareSequentialProcessHandlerTest : BasePlatformTestCase() {

    fun testSequentialExecutionAndTextForwarding() {
        val first = TestProcessHandler()
        val second = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(0, "latex-compile", "Compile LaTeX", "s1", first),
                LatexStepExecution(1, "pdf-viewer", "Open PDF viewer", "s2", second),
            )
        )

        val eventOrder = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> eventOrder += "start:${event.execution.index}"
                is StepLogEvent.StepOutput -> eventOrder += "out:${event.execution.index}"
                is StepLogEvent.StepFinished -> eventOrder += "finish:${event.execution.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> eventOrder += "run:${event.exitCode}"
            }
        }

        val forwardedText = mutableListOf<String>()
        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                forwardedText += event.text
            }
        })

        handler.startNotify()
        assertTrue(first.started)
        assertFalse(second.started)

        first.emit("first-step\n")
        first.finish(0)

        assertTrue(second.started)

        second.emit("second-step\n")
        second.finish(0)

        assertTrue(handler.isProcessTerminated)
        assertEquals("first-step\n", handler.rawLog(0))
        assertEquals("second-step\n", handler.rawLog(1))
        assertEquals(listOf("first-step\n", "second-step\n"), forwardedText)
        assertEquals(
            listOf(
                "start:0",
                "out:0",
                "finish:0:0",
                "start:1",
                "out:1",
                "finish:1:0",
                "run:0",
            ),
            eventOrder
        )
    }

    fun testFailureShortCircuitsRemainingSteps() {
        val first = TestProcessHandler()
        val second = TestProcessHandler()
        val third = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(0, "latex-compile", "Compile LaTeX", "s1", first),
                LatexStepExecution(1, "bibtex", "Run bibliography", "s2", second),
                LatexStepExecution(2, "pdf-viewer", "Open PDF viewer", "s3", third),
            )
        )

        val eventOrder = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> eventOrder += "start:${event.execution.index}"
                is StepLogEvent.StepOutput -> eventOrder += "out:${event.execution.index}"
                is StepLogEvent.StepFinished -> eventOrder += "finish:${event.execution.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> eventOrder += "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        assertTrue(first.started)

        first.emit("boom\n")
        first.finish(2)

        assertTrue(handler.isProcessTerminated)
        assertFalse(second.started)
        assertFalse(third.started)
        assertEquals("boom\n", handler.rawLog(0))
        assertEquals("", handler.rawLog(1))
        assertEquals("", handler.rawLog(2))
        assertEquals(
            listOf(
                "start:0",
                "out:0",
                "finish:0:2",
                "run:2",
            ),
            eventOrder
        )
    }

    fun testBeforeStartRunsBeforeProcessStartAndFailureStopsRun() {
        val first = TestProcessHandler()
        val second = TestProcessHandler()
        val beforeCalls = mutableListOf<String>()

        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(
                    0,
                    "makeindex",
                    "Run makeindex",
                    "s1",
                    first,
                    beforeStart = {
                        beforeCalls += "s1"
                        throw IllegalStateException("pre failed")
                    },
                ),
                LatexStepExecution(
                    1,
                    "pdf-viewer",
                    "Open PDF viewer",
                    "s2",
                    second,
                    beforeStart = { beforeCalls += "s2" },
                ),
            )
        )

        val events = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> events += "start:${event.execution.index}"
                is StepLogEvent.StepOutput -> events += "out:${event.execution.index}"
                is StepLogEvent.StepFinished -> events += "finish:${event.execution.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> events += "run:${event.exitCode}"
            }
        }

        handler.startNotify()

        assertTrue(handler.isProcessTerminated)
        assertFalse(first.started)
        assertFalse(second.started)
        assertEquals(listOf("s1"), beforeCalls)
        assertTrue(handler.rawLog(0).contains("pre failed"))
        assertEquals(
            listOf(
                "start:0",
                "out:0",
                "finish:0:1",
                "run:1",
            ),
            events
        )
    }

    fun testAfterFinishReceivesExitCodeAndErrorsAreReportedWithoutChangingRunExitCode() {
        val first = TestProcessHandler()
        var observedExit: Int? = null

        val handler = StepAwareSequentialProcessHandler(
            listOf(
                LatexStepExecution(
                    0,
                    "latex-compile",
                    "Compile LaTeX",
                    "s1",
                    first,
                    afterFinish = { exitCode ->
                        observedExit = exitCode
                        throw IllegalStateException("post failed")
                    },
                ),
            )
        )

        val events = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> events += "start:${event.execution.index}"
                is StepLogEvent.StepOutput -> events += "out:${event.execution.index}"
                is StepLogEvent.StepFinished -> events += "finish:${event.execution.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> events += "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        first.finish(0)

        assertTrue(handler.isProcessTerminated)
        assertEquals(0, observedExit)
        assertTrue(handler.rawLog(0).contains("post failed"))
        assertEquals(
            listOf(
                "start:0",
                "out:0",
                "finish:0:0",
                "run:0",
            ),
            events
        )
    }

    private class TestProcessHandler : ProcessHandler() {

        var started: Boolean = false
            private set

        override fun startNotify() {
            started = true
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
