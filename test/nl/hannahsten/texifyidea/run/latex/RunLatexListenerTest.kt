package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify

class RunLatexListenerTest : BasePlatformTestCase() {

    override fun tearDown() {
        runCatching {
            unmockkAll()
        }
        super.tearDown()
    }

    fun testCompileTwiceRerunUsesAuxChainAndResetsStateAfterwards() {
        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val settings = runManager.createConfiguration("LaTeX", LatexConfigurationFactory(latexRunConfigurationType()))
        runManager.addConfiguration(settings)
        val runConfig = settings.configuration as LatexRunConfiguration
        val executionState = LatexRunExecutionState(isInitialized = true)
        val processEvent = mockk<ProcessEvent>()
        every { processEvent.exitCode } returns 0

        mockkStatic(RunConfigurationBeforeRunProvider::class)
        every { RunConfigurationBeforeRunProvider.doExecuteTask(environment, settings, null) } answers {
            assertFalse(executionState.isFirstRunConfig)
            assertTrue(executionState.isLastRunConfig)
            true
        }

        RunLatexListener(runConfig, environment, executionState).processTerminated(processEvent)

        assertTrue(executionState.isFirstRunConfig)
        assertFalse(executionState.isLastRunConfig)
        assertFalse(executionState.isInitialized)
        verify(exactly = 1) { RunConfigurationBeforeRunProvider.doExecuteTask(environment, settings, null) }
    }

    fun testCompileTwiceRerunResetsStateWhenTaskThrows() {
        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val settings = runManager.createConfiguration("LaTeX", LatexConfigurationFactory(latexRunConfigurationType()))
        runManager.addConfiguration(settings)
        val runConfig = settings.configuration as LatexRunConfiguration
        val executionState = LatexRunExecutionState(isInitialized = true)
        val processEvent = mockk<ProcessEvent>()
        every { processEvent.exitCode } returns 0

        mockkStatic(RunConfigurationBeforeRunProvider::class)
        every { RunConfigurationBeforeRunProvider.doExecuteTask(environment, settings, null) } throws RuntimeException("boom")

        try {
            RunLatexListener(runConfig, environment, executionState).processTerminated(processEvent)
            fail("Expected RuntimeException from rerun task")
        }
        catch (_: RuntimeException) {
        }

        assertTrue(executionState.isFirstRunConfig)
        assertFalse(executionState.isLastRunConfig)
        assertFalse(executionState.isInitialized)
    }
}
