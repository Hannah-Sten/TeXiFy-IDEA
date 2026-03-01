package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState
import kotlin.test.assertFailsWith

class LatexRunStepStateSelectionTest : BasePlatformTestCase() {

    fun testGetStateUsesStepRunStateWhenConfiguredStepsAreSupported() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(LatexmkCompileStepOptions(), PdfViewerStepOptions())

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }

    fun testGetStateKeepsStepPipelineWhenContainsUnsupportedType() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(
            ExternalToolStepOptions().apply { type = "unsupported-step" },
            PdfViewerStepOptions(),
        )

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }

    fun testGetStateInjectsDefaultStepsWhenEmpty() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf()

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
        assertEquals(listOf(LatexStepType.LATEXMK_COMPILE, LatexStepType.PDF_VIEWER), runConfig.configOptions.steps.map { it.type })
    }

    fun testGetStateFailsEarlyWhenAllConfiguredStepsUnsupported() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.configOptions.steps = mutableListOf(
            ExternalToolStepOptions().apply { type = "unsupported-a" },
            ExternalToolStepOptions().apply { type = "unsupported-b" },
        )

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val error = assertFailsWith<ExecutionException> {
            runConfig.getState(executor, environment)
        }

        assertEquals("No executable compile steps were configured.", error.message)
    }
}
