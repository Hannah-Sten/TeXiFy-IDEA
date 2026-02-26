package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.step.InlineLatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.ProcessLatexRunStep
import java.io.OutputStream

class StepAwareSequentialProcessHandlerTest : BasePlatformTestCase() {

    fun testSequentialExecutionAndTextForwarding() {
        val context = createContext("sequence-forward.tex")
        val first = TestProcessHandler()
        val second = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", first),
                TestProcessStep("pdf-viewer", "s2", second),
            ),
            context,
        )

        val eventOrder = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> eventOrder += "start:${event.index}"
                is StepLogEvent.StepOutput -> eventOrder += "out:${event.index}"
                is StepLogEvent.StepFinished -> eventOrder += "finish:${event.index}:${event.exitCode}"
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
        val context = createContext("failure-short-circuit.tex")
        val first = TestProcessHandler()
        val second = TestProcessHandler()
        val third = TestProcessHandler()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", first),
                TestProcessStep("bibtex", "s2", second),
                TestProcessStep("pdf-viewer", "s3", third),
            ),
            context,
        )

        val eventOrder = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> eventOrder += "start:${event.index}"
                is StepLogEvent.StepOutput -> eventOrder += "out:${event.index}"
                is StepLogEvent.StepFinished -> eventOrder += "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> eventOrder += "run:${event.exitCode}"
            }
        }

        handler.startNotify()
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
        val context = createContext("inline-run.tex")
        val lifecycle = mutableListOf<String>()
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestInlineStep(
                    id = "file-cleanup",
                    configId = "s1",
                    before = { lifecycle += "before" },
                    run = {
                        lifecycle += "run"
                        0
                    },
                    after = { _, _ -> lifecycle += "after" },
                )
            ),
            context,
        )

        val events = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> events += "start:${event.index}"
                is StepLogEvent.StepOutput -> events += "out:${event.index}"
                is StepLogEvent.StepFinished -> events += "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> events += "run:${event.exitCode}"
            }
        }

        handler.startNotify()

        assertTrue(handler.isProcessTerminated)
        assertEquals(listOf("before", "run", "after"), lifecycle)
        assertEquals(
            listOf(
                "start:0",
                "finish:0:0",
                "run:0",
            ),
            events
        )
    }

    fun testBeforeStartRunsBeforeProcessStartAndFailureStopsRun() {
        val context = createContext("before-failure.tex")
        val first = TestProcessHandler()
        var cleanupRan = false
        val beforeCalls = mutableListOf<String>()

        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep(
                    id = "makeindex",
                    configId = "s1",
                    process = first,
                    before = {
                        beforeCalls += "s1"
                        throw IllegalStateException("pre failed")
                    },
                ),
                TestInlineStep(
                    id = "file-cleanup",
                    configId = "s2",
                    run = {
                        cleanupRan = true
                        0
                    },
                ),
            ),
            context,
        )

        val events = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> events += "start:${event.index}"
                is StepLogEvent.StepOutput -> events += "out:${event.index}"
                is StepLogEvent.StepFinished -> events += "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> events += "run:${event.exitCode}"
            }
        }

        val error = try {
            handler.startNotify()
            null
        }
        catch (t: Throwable) {
            t
        }

        assertNotNull(error)
        assertTrue(error is IllegalStateException)
        assertTrue(error?.message?.contains("pre failed") == true)
        assertFalse(handler.isProcessTerminated)
        assertFalse(first.started)
        assertFalse(cleanupRan)
        assertEquals(listOf("s1"), beforeCalls)
        assertEquals("", handler.rawLog(0))
        assertEquals(
            listOf(
                "start:0",
            ),
            events
        )
    }

    fun testInlineFailureShortCircuitsRemainingSteps() {
        val context = createContext("inline-failure.tex")
        var firstRan = false
        var secondRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestInlineStep(
                    id = "file-cleanup",
                    configId = "s1",
                    run = {
                        firstRan = true
                        2
                    },
                ),
                TestInlineStep(
                    id = "file-cleanup",
                    configId = "s2",
                    run = {
                        secondRan = true
                        0
                    },
                ),
            ),
            context,
        )

        handler.startNotify()

        assertTrue(handler.isProcessTerminated)
        assertTrue(firstRan)
        assertFalse(secondRan)
        assertEquals("", handler.rawLog(1))
    }

    fun testAfterFinishReceivesExitCodeAndErrorsAreReportedWithoutChangingRunExitCode() {
        val context = createContext("after-finish-error.tex")
        val first = TestProcessHandler()
        var observedExit: Int? = null

        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep(
                    id = "latex-compile",
                    configId = "s1",
                    process = first,
                    after = { _, exitCode ->
                        observedExit = exitCode
                        throw IllegalStateException("post failed")
                    },
                ),
            ),
            context,
        )

        val events = mutableListOf<String>()
        handler.addStepLogListener { event ->
            when (event) {
                is StepLogEvent.StepStarted -> events += "start:${event.index}"
                is StepLogEvent.StepOutput -> events += "out:${event.index}"
                is StepLogEvent.StepFinished -> events += "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> events += "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        val error = try {
            first.finish(0)
            null
        }
        catch (t: Throwable) {
            t
        }

        assertNotNull(error)
        assertTrue(fullMessage(error!!).contains("post failed"))
        assertFalse(handler.isProcessTerminated)
        assertEquals(0, observedExit)
        assertEquals("", handler.rawLog(0))
        assertEquals(
            listOf(
                "start:0",
            ),
            events
        )
    }

    private fun fullMessage(throwable: Throwable): String {
        val parts = mutableListOf<String>()
        var current: Throwable? = throwable
        while (current != null) {
            current.message?.let(parts::add)
            current = current.cause
        }
        return parts.joinToString(" | ")
    }

    fun testDestroyProcessSkipsFollowingCleanupStep() {
        val context = createContext("destroy-short-circuit.tex")
        val compile = TestProcessHandler()
        var cleanupRan = false
        val handler = StepAwareSequentialProcessHandler(
            listOf(
                TestProcessStep("latex-compile", "s1", compile),
                TestInlineStep(
                    id = "file-cleanup",
                    configId = "s2",
                    run = {
                        cleanupRan = true
                        0
                    },
                ),
            ),
            context,
        )

        handler.startNotify()
        assertTrue(compile.started)

        handler.destroyProcess()

        assertTrue(handler.isProcessTerminated)
        assertTrue(compile.destroyed)
        assertFalse(cleanupRan)
    }

    private fun createContext(mainFileName: String): LatexRunStepContext {
        val mainFile = myFixture.addFileToProject(mainFileName, "\\documentclass{article}").virtualFile
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "test"
        )
        val environment = mockk<ExecutionEnvironment>(relaxed = true).also {
            every { it.project } returns project
        }
        val state = LatexRunSessionState(resolvedMainFile = mainFile)
        return LatexRunStepContext(runConfig, environment, state, mainFile)
    }

    private class TestProcessStep(
        override val id: String,
        override val configId: String,
        private val process: TestProcessHandler,
        private val before: (LatexRunStepContext) -> Unit = {},
        private val after: (LatexRunStepContext, Int) -> Unit = { _, _ -> },
    ) : ProcessLatexRunStep {

        override fun beforeStart(context: LatexRunStepContext) {
            before(context)
        }

        override fun createProcess(context: LatexRunStepContext): ProcessHandler = process

        override fun afterFinish(context: LatexRunStepContext, exitCode: Int) {
            after(context, exitCode)
        }
    }

    private class TestInlineStep(
        override val id: String,
        override val configId: String,
        private val before: (LatexRunStepContext) -> Unit = {},
        private val run: (LatexRunStepContext) -> Int = { 0 },
        private val after: (LatexRunStepContext, Int) -> Unit = { _, _ -> },
    ) : InlineLatexRunStep {

        override fun beforeStart(context: LatexRunStepContext) {
            before(context)
        }

        override fun runInline(context: LatexRunStepContext): Int = run(context)

        override fun afterFinish(context: LatexRunStepContext, exitCode: Int) {
            after(context, exitCode)
        }
    }

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
