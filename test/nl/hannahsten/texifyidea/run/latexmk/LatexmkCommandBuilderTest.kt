package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType

class LatexmkCommandBuilderTest : BasePlatformTestCase() {

    fun testBuildCommandAddsOutdirAndAuxdirWhenSeparated() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val outDir = myFixture.addFileToProject("out/.keep", "").virtualFile.parent
        val auxDir = myFixture.addFileToProject("aux/.keep", "").virtualFile.parent

        val runConfig = createRunConfig(mainFile.virtualFile, outDir, auxDir, LatexDistributionType.TEXLIVE)
        val command = LatexmkCommandBuilder.buildCommand(runConfig, project) ?: error("No command generated")

        assertTrue(command.any { it == "-outdir=${outDir.path}" })
        assertTrue(command.any { it == "-auxdir=${auxDir.path}" })
    }

    fun testBuildCommandOmitsAuxdirWhenEqualToOutdir() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val outDir = myFixture.addFileToProject("out/.keep", "").virtualFile.parent

        val runConfig = createRunConfig(mainFile.virtualFile, outDir, outDir, LatexDistributionType.TEXLIVE)
        val command = LatexmkCommandBuilder.buildCommand(runConfig, project) ?: error("No command generated")

        assertTrue(command.any { it == "-outdir=${outDir.path}" })
        assertFalse(command.any { it.startsWith("-auxdir=") })
    }

    fun testBuildCommandAddsAuxdirOnMiktexWhenSeparated() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val outDir = myFixture.addFileToProject("out/.keep", "").virtualFile.parent
        val auxDir = myFixture.addFileToProject("aux/.keep", "").virtualFile.parent

        val runConfig = createRunConfig(mainFile.virtualFile, outDir, auxDir, LatexDistributionType.MIKTEX)
        val command = LatexmkCommandBuilder.buildCommand(runConfig, project) ?: error("No command generated")

        assertTrue(command.any { it == "-outdir=${outDir.path}" })
        assertTrue(command.any { it == "-auxdir=${auxDir.path}" })
    }

    fun testBuildCleanCommandUsesOutdirAndAuxdirSmartly() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val outDir = myFixture.addFileToProject("out/.keep", "").virtualFile.parent
        val auxDir = myFixture.addFileToProject("aux/.keep", "").virtualFile.parent

        val runConfig = createRunConfig(mainFile.virtualFile, outDir, auxDir, LatexDistributionType.TEXLIVE)
        val clean = LatexmkCommandBuilder.buildCleanCommand(runConfig, cleanAll = false) ?: error("No clean command generated")
        val cleanAll = LatexmkCommandBuilder.buildCleanCommand(runConfig, cleanAll = true) ?: error("No clean-all command generated")

        assertTrue(clean.any { it == "-outdir=${outDir.path}" })
        assertTrue(clean.any { it == "-auxdir=${auxDir.path}" })
        assertTrue(clean.any { it == "-c" })

        assertTrue(cleanAll.any { it == "-outdir=${outDir.path}" })
        assertTrue(cleanAll.any { it == "-auxdir=${auxDir.path}" })
        assertTrue(cleanAll.any { it == "-C" })
    }

    private fun createRunConfig(
        mainFile: VirtualFile,
        outputDir: VirtualFile,
        auxDir: VirtualFile,
        distributionType: LatexDistributionType,
    ): LatexmkRunConfiguration = LatexmkRunConfiguration(
        project,
        LatexConfigurationFactory(LatexmkRunConfigurationType()),
        "Latexmk",
    ).apply {
        this.mainFile = mainFile
        this.outputPathRaw = outputDir.path
        this.auxilPathRaw = auxDir.path
        this.latexDistribution = distributionType
    }
}
