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
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.step.InlineLatexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.ProcessLatexRunStep
import java.io.OutputStream
import java.nio.file.Path

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
            eventOrder += when (event) {
                is StepLogEvent.StepStarted -> "start:${event.index}"
                is StepLogEvent.StepOutput -> "out:${event.index}"
                is StepLogEvent.StepFinished -> "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> "run:${event.exitCode}"
            }
        }

        val forwardedText = mutableListOf<String>()
        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                forwardedText += event.text
            }
        })

        handler.startNotify()
        waitUntil { first.started }
        assertFalse(second.started)

        first.emit("first-step\n")
        first.finish(0)

        waitUntil { second.started }
        second.emit("second-step\n")
        second.finish(0)

        waitUntil { handler.isProcessTerminated }
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
            eventOrder += when (event) {
                is StepLogEvent.StepStarted -> "start:${event.index}"
                is StepLogEvent.StepOutput -> "out:${event.index}"
                is StepLogEvent.StepFinished -> "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        first.emit("boom\n")
        first.finish(2)

        waitUntil { handler.isProcessTerminated }
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
            events += when (event) {
                is StepLogEvent.StepStarted -> "start:${event.index}"
                is StepLogEvent.StepOutput -> "out:${event.index}"
                is StepLogEvent.StepFinished -> "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> "run:${event.exitCode}"
            }
        }

        handler.startNotify()

        waitUntil { handler.isProcessTerminated }
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
            events += when (event) {
                is StepLogEvent.StepStarted -> "start:${event.index}"
                is StepLogEvent.StepOutput -> "out:${event.index}"
                is StepLogEvent.StepFinished -> "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        waitUntil { handler.isProcessTerminated }

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

        waitUntil { handler.isProcessTerminated }
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
            events += when (event) {
                is StepLogEvent.StepStarted -> "start:${event.index}"
                is StepLogEvent.StepOutput -> "out:${event.index}"
                is StepLogEvent.StepFinished -> "finish:${event.index}:${event.exitCode}"
                is StepLogEvent.RunFinished -> "run:${event.exitCode}"
            }
        }

        handler.startNotify()
        waitUntil { first.started }
        val error = try {
            first.finish(0)
            null
        }
        catch (t: Throwable) {
            t
        }

        assertNull(error)
        waitUntil { handler.isProcessTerminated }
        assertTrue(handler.rawLog(0).contains("post failed"))
        assertEquals(0, observedExit)
        assertTrue(events.contains("start:0"))
        assertTrue(events.contains("out:0"))
        assertTrue(events.contains("finish:0:1"))
        assertTrue(events.contains("run:1"))
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
        waitUntil { compile.started }

        handler.destroyProcess()

        waitUntil { handler.isProcessTerminated }
        assertTrue(compile.destroyed)
        assertFalse(cleanupRan)
    }

    private fun waitUntil(timeoutMs: Long = 5_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition()) {
            if (System.currentTimeMillis() >= deadline) {
                fail("Condition was not met within ${timeoutMs}ms")
            }
            Thread.sleep(10)
        }
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
        val state = LatexRunSessionState(
            project = project,
            mainFile = mainFile,
            outputDir = mainFile.parent,
            workingDirectory = Path.of(mainFile.parent.path),
            distributionType = LatexDistributionType.TEXLIVE,
            usesDefaultWorkingDirectory = true,
            latexSdk = null,
            auxDir = mainFile.parent,
        )
        return LatexRunStepContext(runConfig, environment, state)
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
