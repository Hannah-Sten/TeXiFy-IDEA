package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import java.nio.file.Files

class LatexmkRcFileFinderTest : BasePlatformTestCase() {

    fun testHasLatexmkRcFindsLocalRcInWorkingDirectory() {
        val tempDir = Files.createTempDirectory("texify-latexmkrc")
        Files.createFile(tempDir.resolve(".latexmkrc"))

        val hasRc = LatexmkRcFileFinder.hasLatexmkRc(compilerArguments = null, workingDirectory = tempDir)
        assertTrue(hasRc)
    }

    fun testHasLatexmkRcReturnsFalseWithoutRc() {
        val tempDir = Files.createTempDirectory("texify-latexmkrc-empty")

        val hasRc = LatexmkRcFileFinder.hasLatexmkRc(compilerArguments = null, workingDirectory = tempDir)
        assertFalse(hasRc)
    }

    fun testLocalLatexmkRcPathForRunConfigUsesConfiguredWorkingDirectory() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val workingDirectory = Files.createTempDirectory("texify-latexmkrc-working-dir")
        val expectedRcPath = Files.createFile(workingDirectory.resolve(".latexmkrc"))
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.workingDirectory = workingDirectory

        val actualRcPath = LatexmkRcFileFinder.localLatexmkRcPathForRunConfig(runConfig)
        assertEquals(expectedRcPath, actualRcPath)
    }
}
