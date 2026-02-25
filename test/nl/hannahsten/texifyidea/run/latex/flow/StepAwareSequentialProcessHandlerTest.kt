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
                processExecution(0, "latex-compile", "s1", first),
                processExecution(1, "pdf-viewer", "s2", second),
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
                processExecution(0, "latex-compile", "s1", first),
                processExecution(1, "bibtex", "s2", second),
                processExecution(2, "pdf-viewer", "s3", third),
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

    fun testInlineExecutionRunsAndPublishesLifecycleEvents() {
        var inlineRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                inlineExecution(0, "file-cleanup", "s1") {
                    inlineRan = true
                    0
                }
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
        assertTrue(inlineRan)
        assertEquals(
            listOf(
                "start:0",
                "finish:0:0",
                "run:0",
            ),
            events
        )
    }

    fun testMixedProcessAndInlineExecution() {
        val compile = TestProcessHandler()
        var cleanupRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                processExecution(0, "latex-compile", "s1", compile),
                inlineExecution(1, "file-cleanup", "s2") {
                    cleanupRan = true
                    0
                }
            )
        )

        handler.startNotify()
        assertTrue(compile.started)
        assertFalse(cleanupRan)

        compile.finish(0)

        assertTrue(handler.isProcessTerminated)
        assertTrue(cleanupRan)
    }

    fun testInlineFailureShortCircuitsRemainingSteps() {
        var firstRan = false
        var secondRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                inlineExecution(0, "file-cleanup", "s1") {
                    firstRan = true
                    2
                },
                inlineExecution(1, "file-cleanup", "s2") {
                    secondRan = true
                    0
                },
            )
        )

        handler.startNotify()

        assertTrue(handler.isProcessTerminated)
        assertTrue(firstRan)
        assertFalse(secondRan)
        assertEquals("", handler.rawLog(1))
    }

    fun testBeforeStartRunsBeforeProcessStartAndFailureStopsRun() {
        val first = TestProcessHandler()
        var cleanupRan = false
        val beforeCalls = mutableListOf<String>()

        val handler = StepAwareSequentialProcessHandler(
            listOf(
                processExecution(
                    index = 0,
                    type = "makeindex",
                    configId = "s1",
                    handler = first,
                    beforeStart = {
                        beforeCalls += "s1"
                        throw IllegalStateException("pre failed")
                    },
                ),
                inlineExecution(1, "file-cleanup", "s2") {
                    cleanupRan = true
                    0
                },
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
        assertFalse(cleanupRan)
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
                processExecution(
                    index = 0,
                    type = "latex-compile",
                    configId = "s1",
                    handler = first,
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

    fun testDestroyProcessSkipsFollowingCleanupStep() {
        val compile = TestProcessHandler()
        var cleanupRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                processExecution(0, "latex-compile", "s1", compile),
                inlineExecution(1, "file-cleanup", "s2") {
                    cleanupRan = true
                    0
                },
            )
        )

        handler.startNotify()
        assertTrue(compile.started)

        handler.destroyProcess()

        assertTrue(handler.isProcessTerminated)
        assertTrue(compile.destroyed)
        assertFalse(cleanupRan)
    }

    private fun processExecution(
        index: Int,
        type: String,
        configId: String,
        handler: ProcessHandler,
        beforeStart: () -> Unit = {},
        afterFinish: (Int) -> Unit = {},
    ): ProcessLatexStepExecution = ProcessLatexStepExecution(
        index = index,
        type = type,
        displayName = type,
        configId = configId,
        processHandler = handler,
        beforeStart = beforeStart,
        afterFinish = afterFinish,
    )

    private fun inlineExecution(
        index: Int,
        type: String,
        configId: String,
        action: () -> Int = { 0 },
    ): InlineLatexStepExecution = InlineLatexStepExecution(
        index = index,
        type = type,
        displayName = type,
        configId = configId,
        action = action,
    )

    private class TestProcessHandler : ProcessHandler() {

        var started: Boolean = false
            private set

        var destroyed: Boolean = false
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
            destroyed = true
            notifyProcessTerminated(-1)
        }

        override fun detachProcessImpl() {
            notifyProcessDetached()
        }

        override fun detachIsDefault(): Boolean = false

        override fun getProcessInput(): OutputStream? = null
    }
}
