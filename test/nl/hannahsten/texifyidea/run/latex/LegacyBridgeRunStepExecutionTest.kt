package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import nl.hannahsten.texifyidea.run.latex.step.LegacyAuxRunConfigurationsStep
import nl.hannahsten.texifyidea.run.latex.step.LegacyBibtexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LegacyBridgeRunStepExecutionTest : BasePlatformTestCase() {

    override fun tearDown() {
        runCatching { unmockkAll() }
        super.tearDown()
    }

    fun testLegacyBibtexRunStepExecutesAuxConfigurationsInline() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}").virtualFile
        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        val executor = mockk<Executor>(relaxed = true)
        val runner = mockk<ProgramRunner<*>>(relaxed = true)
        every { environment.project } returns project
        every { environment.executor } returns executor
        every { environment.runner } returns runner

        val childRunConfiguration = mockk<RunConfiguration>(relaxed = true)
        every { childRunConfiguration.getState(executor, environment) } returns TestCommandLineState(environment, "bib-output\n", 0)
        val bibSetting = mockk<RunnerAndConfigurationSettings>(relaxed = true)
        every { bibSetting.name } returns "bib"
        every { bibSetting.configuration } returns childRunConfiguration

        val context = LatexRunStepContext(
            runConfig = runConfig,
            environment = environment,
            executionState = runConfig.executionState,
            mainFile = mainFile
        )

        val process = object : LegacyBibtexRunStep() {
            override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> = setOf(bibSetting)
        }.createProcess(context)
        val completion = CountDownLatch(1)
        val output = StringBuilder()
        var exitCode = Int.MIN_VALUE
        process.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: com.intellij.openapi.util.Key<*>) {
                output.append(event.text)
            }

            override fun processTerminated(event: ProcessEvent) {
                exitCode = event.exitCode
                completion.countDown()
            }
        })
        process.startNotify()

        assertTrue(completion.await(2, TimeUnit.SECONDS))
        assertTrue(process.isProcessTerminated)
        assertEquals(0, exitCode)
        assertTrue(output.toString().contains("bib-output"))
        verify(exactly = 1) { childRunConfiguration.getState(executor, environment) }
    }

    fun testInlineAuxStepStopsOnFirstFailure() {
        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        val executor = mockk<Executor>(relaxed = true)
        val runner = mockk<ProgramRunner<*>>(relaxed = true)
        every { environment.project } returns project
        every { environment.executor } returns executor
        every { environment.runner } returns runner

        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val firstRunConfiguration = mockk<RunConfiguration>(relaxed = true)
        val secondRunConfiguration = mockk<RunConfiguration>(relaxed = true)
        every { firstRunConfiguration.getState(executor, environment) } returns TestCommandLineState(environment, "first\n", 1)
        every { secondRunConfiguration.getState(executor, environment) } returns TestCommandLineState(environment, "second\n", 0)

        val first = mockk<RunnerAndConfigurationSettings>(relaxed = true)
        val second = mockk<RunnerAndConfigurationSettings>(relaxed = true)
        every { first.name } returns "first"
        every { second.name } returns "second"
        every { first.configuration } returns firstRunConfiguration
        every { second.configuration } returns secondRunConfiguration

        val context = LatexRunStepContext(
            runConfig = runConfig,
            environment = environment,
            executionState = runConfig.executionState,
            mainFile = myFixture.addFileToProject("main2.tex", "\\documentclass{article}").virtualFile
        )

        val step = object : LegacyAuxRunConfigurationsStep() {
            override val id: String = "test-aux"

            override fun resolveRunConfigurations(context: LatexRunStepContext): Set<RunnerAndConfigurationSettings> = setOf(first, second)
        }
        val process = step.createProcess(context)

        val completion = CountDownLatch(1)
        val output = StringBuilder()
        var exitCode = Int.MIN_VALUE
        process.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: com.intellij.openapi.util.Key<*>) {
                output.append(event.text)
            }

            override fun processTerminated(event: ProcessEvent) {
                exitCode = event.exitCode
                completion.countDown()
            }
        })
        process.startNotify()

        assertTrue(completion.await(2, TimeUnit.SECONDS))
        assertEquals(1, exitCode)
        assertTrue(output.toString().contains("first\n"))
        assertFalse(output.toString().contains("second\n"))
        verify(exactly = 1) { firstRunConfiguration.getState(executor, environment) }
        verify(exactly = 0) { secondRunConfiguration.getState(executor, environment) }
    }

    private class TestCommandLineState(
        environment: ExecutionEnvironment,
        private val text: String,
        private val exitCode: Int,
    ) : CommandLineState(environment) {

        override fun startProcess(): ProcessHandler = TestProcessHandler(text, exitCode)
    }

    private class TestProcessHandler(
        private val text: String,
        private val exitCode: Int,
    ) : ProcessHandler() {

        override fun destroyProcessImpl() {
            notifyProcessTerminated(-1)
        }

        override fun detachProcessImpl() {
            notifyProcessDetached()
        }

        override fun detachIsDefault(): Boolean = false

        override fun getProcessInput(): OutputStream? = null

        override fun startNotify() {
            super.startNotify()
            notifyTextAvailable(text, ProcessOutputTypes.STDOUT)
            notifyProcessTerminated(exitCode)
        }
    }
}
