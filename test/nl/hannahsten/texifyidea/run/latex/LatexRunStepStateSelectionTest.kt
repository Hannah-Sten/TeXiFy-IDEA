package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState

class LatexRunStepStateSelectionTest : BasePlatformTestCase() {

    fun testGetStateUsesStepRunStateWhenSchemaParsedAndSupported() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
        runConfig.stepSchemaTypes = listOf("compile-latex")

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }

    fun testGetStateKeepsStepPipelineWhenSchemaParsedButUnsupported() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
        runConfig.stepSchemaTypes = listOf("unsupported-step")

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }

    fun testGetStateUsesStepRunStateForNonLatexmkWhenNoLegacyBridgeStepIsPresent() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
        runConfig.stepSchemaTypes = listOf("compile-latex")

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }

    fun testGetStateUsesStepRunStateWhenPlanContainsLegacyBridgeStep() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.stepSchemaStatus = StepSchemaReadStatus.PARSED
        runConfig.stepSchemaTypes = listOf("compile-latex", "legacy-bibtex")

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project
        val executor = mockk<Executor>(relaxed = true)

        val state = runConfig.getState(executor, environment)

        assertTrue(state is LatexStepRunState)
    }
}
