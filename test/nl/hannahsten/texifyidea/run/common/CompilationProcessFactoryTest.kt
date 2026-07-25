package nl.hannahsten.texifyidea.run.common

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer

class CompilationProcessFactoryTest : BasePlatformTestCase() {

    fun testExpandEnvironmentVariablesIncludesSourceRoots() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}").virtualFile
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test")

        val env = expandEnvironmentVariables(runConfig, mainFile, null)
        val texinputs = env["TEXINPUTS"]

        assertNotNull("TEXINPUTS should be set", texinputs)
        assertTrue("TEXINPUTS should contain main file parent", texinputs!!.contains(mainFile.parent.path))
    }

    fun testExpandEnvironmentVariablesAppendsToExistingTexinputs() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}").virtualFile
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test")
        runConfig.environmentVariables = runConfig.environmentVariables.with(mapOf("TEXINPUTS" to "existing_path"))

        val env = expandEnvironmentVariables(runConfig, mainFile, null)
        val texinputs = env["TEXINPUTS"]

        assertNotNull("TEXINPUTS should be set", texinputs)
        assertTrue("TEXINPUTS should contain existing path", texinputs!!.startsWith("existing_path"))
        assertTrue("TEXINPUTS should contain main file parent", texinputs.contains(mainFile.parent.path))
    }
}
