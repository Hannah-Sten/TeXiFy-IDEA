package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.latex.step.LegacyBibtexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext

class LegacyBridgeRunStepExecutionTest : BasePlatformTestCase() {

    override fun tearDown() {
        runCatching { unmockkAll() }
        super.tearDown()
    }

    fun testLegacyBibtexRunStepExecutesAuxConfigurationsThroughBeforeRunProvider() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val bib = runManager.createConfiguration("bib", LatexConfigurationFactory(BibtexRunConfigurationType()))
        runManager.addConfiguration(bib)
        runConfig.bibRunConfigs = setOf(bib)

        val environment = mockk<ExecutionEnvironment>(relaxed = true)
        every { environment.project } returns project

        mockkStatic(RunConfigurationBeforeRunProvider::class)
        every { RunConfigurationBeforeRunProvider.doExecuteTask(environment, bib, null) } returns true

        val context = LatexRunStepContext(
            runConfig = runConfig,
            environment = environment,
            executionState = runConfig.executionState,
            mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}").virtualFile
        )

        val process = LegacyBibtexRunStep().createProcess(context)
        process.startNotify()

        assertTrue(process.isProcessTerminated)
        verify(exactly = 1) { RunConfigurationBeforeRunProvider.doExecuteTask(environment, bib, null) }
    }
}
