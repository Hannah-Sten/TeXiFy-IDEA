package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.compiler.LatexCompilePrograms
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.step.LatexmkCompileRunStep
import nl.hannahsten.texifyidea.updateFilesets
import java.nio.file.Files
import java.nio.file.Path

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

    private fun latexmkStep(runConfig: LatexRunConfiguration): LatexmkCompileStepOptions = runConfig.ensurePrimaryCompileStepLatexmk()

    private fun initializeSessionState(runConfig: LatexRunConfiguration): LatexRunSessionState =
        LatexSessionInitializer.initializeForModel(runConfig)

    private fun latexmkCommand(runConfig: LatexRunConfiguration, session: LatexRunSessionState): List<String> {
        val step = latexmkStep(runConfig)
        val effectiveMode = LatexmkCompileRunStep.effectiveCompileMode(runConfig, session, step)
        val effectiveArguments = LatexmkCompileRunStep.buildArguments(runConfig, session, step, effectiveMode)
        return LatexmkCompileRunStep(step).buildCommand(runConfig, session, step, effectiveArguments)
    }

    fun testCompileModeContainsAuto() {
        assertTrue(LatexmkCompileMode.entries.contains(LatexmkCompileMode.AUTO))
        assertEquals("AUTO", LatexmkCompileMode.AUTO.toString())
    }

    fun testPreferredModeFromPackagesFallsBackToLuaLatexPdf() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.FONTSPEC))
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesUsesXeLatexForXeCjk() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.Package("xecjk")))
        assertEquals(LatexmkCompileMode.XELATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesUsesXeLatexForCtexBeamerClass() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.Class("ctexbeamer")))
        assertEquals(LatexmkCompileMode.XELATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesUsesLuaLatexForLuaTexJa() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.Package("luatexja")))
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesPrefersXeLatexWhenBothXeAndLuaLibrariesArePresent() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.FONTSPEC, LatexLib.Package("xecjk")))
        assertEquals(LatexmkCompileMode.XELATEX_PDF, preferred)
    }

    fun testMagicCompilerOverridesWithRecognizedMode() {
        val mode = compileModeFromMagicCommand("xelatex -synctex=1")
        assertEquals(LatexmkCompileMode.XELATEX_PDF, mode)
    }

    fun testMagicLatexmkXelatexXdvMapsToXelatexXdv() {
        val mode = compileModeFromMagicCommand("latexmk -xelatex -xdv")
        assertEquals(LatexmkCompileMode.XELATEX_XDV, mode)
    }

    fun testMagicLatexmkLatexDviMapsToLatexDvi() {
        val mode = compileModeFromMagicCommand("latexmk -latex -dvi")
        assertEquals(LatexmkCompileMode.LATEX_DVI, mode)
    }

    fun testMagicLatexmkLatexPsMapsToLatexPs() {
        val mode = compileModeFromMagicCommand("latexmk -latex -ps")
        assertEquals(LatexmkCompileMode.LATEX_PS, mode)
    }

    fun testMagicLatexmkLualatexMapsToLuaLatexPdf() {
        val mode = compileModeFromMagicCommand("latexmk -lualatex")
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, mode)
    }

    fun testMagicLatexmkPdflatexCustomMapsToCustom() {
        val mode = compileModeFromMagicCommand("latexmk -pdflatex=\"lualatex %O %S\"")
        assertEquals(LatexmkCompileMode.CUSTOM, mode)
    }

    fun testMagicTectonicAndAraraAreNotForcedIntoLatexmkMode() {
        assertNull(compileModeFromMagicCommand("tectonic --synctex"))
        assertNull(compileModeFromMagicCommand("arara"))
    }

    fun testUnicodeEngineCompatibilityUsesLatexmkCompileMode() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)

        step.latexmkCompileMode = LatexmkCompileMode.LUALATEX_PDF
        assertEquals(true, unicodeEngineCompatibility(runConfig))

        step.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        assertEquals(false, unicodeEngineCompatibility(runConfig))

        step.latexmkCompileMode = LatexmkCompileMode.CUSTOM
        assertEquals(null, unicodeEngineCompatibility(runConfig))
    }

    fun testLatexRunConfigurationLatexmkArgumentsIgnoreOutputFormat() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.mainFilePath = mainFile.virtualFile.name
        step.latexmkCompileMode = LatexmkCompileMode.LUALATEX_PDF
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null

        val session = initializeSessionState(runConfig)
        val arguments = LatexmkCompileRunStep.buildArguments(runConfig, session, step)
        assertTrue(arguments.contains("-lualatex"))
        assertFalse(arguments.contains("-output-format"))
    }

    fun testLatexRunConfigurationAutoModeFollowsMagicXdv() {
        val psi = myFixture.addFileToProject(
            "main.tex",
            """
            % !TeX program = latexmk -xelatex -xdv
            \documentclass{article}
            \begin{document}
            hi
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.mainFilePath = psi.virtualFile.name
        step.latexmkCompileMode = LatexmkCompileMode.AUTO
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null

        val session = initializeSessionState(runConfig)
        val effectiveMode = LatexmkCompileRunStep.effectiveCompileMode(runConfig, session, step)
        val arguments = LatexmkCompileRunStep.buildArguments(runConfig, session, step, effectiveMode)

        assertTrue(arguments.contains("-xelatex"))
        assertTrue(arguments.contains("-xdv"))
        assertTrue(LatexmkCompileRunStep.outputFilePath(session, effectiveMode).endsWith(".xdv"))
    }

    fun testLatexRunConfigurationAutoModeUsesPackageHeuristics() {
        val psi = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \usepackage{fontspec}
            \begin{document}
            hi
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.mainFilePath = psi.virtualFile.name
        step.latexmkCompileMode = LatexmkCompileMode.AUTO
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null

        val session = initializeSessionState(runConfig)
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, LatexmkCompileRunStep.effectiveCompileMode(runConfig, session, step))
        assertTrue(LatexmkCompileRunStep.buildArguments(runConfig, session, step).contains("-lualatex"))
    }

    fun testLatexRunConfigurationAutoModeFallsBackToPdfLatex() {
        val psi = myFixture.addFileToProject(
            "main.tex",
            """
            \documentclass{article}
            \begin{document}
            hi
            \end{document}
            """.trimIndent()
        )
        myFixture.updateFilesets()

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.mainFilePath = psi.virtualFile.name
        step.latexmkCompileMode = LatexmkCompileMode.AUTO
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null

        val session = initializeSessionState(runConfig)
        assertEquals(LatexmkCompileMode.PDFLATEX_PDF, LatexmkCompileRunStep.effectiveCompileMode(runConfig, session, step))
        assertTrue(LatexmkCompileRunStep.buildArguments(runConfig, session, step).contains("-pdf"))
    }

    fun testLatexmkCompilerProgramIsStillOffered() {
        assertTrue(LatexCompilePrograms.allExecutableNames.contains("latexmk"))
    }

    fun testLatexRunConfigurationLatexmkCommandUsesOutdirAuxdirAndNoDuplicatePdf() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.mainFilePath = mainFile.virtualFile.name
        step.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null
        val session = initializeSessionState(runConfig)

        val command = latexmkCommand(runConfig, session)

        assertTrue(command.any { it == "-interaction=nonstopmode" })
        assertTrue(command.any { it == "-file-line-error" })
        assertTrue(command.any { it.startsWith("-outdir=") })
        val hasAuxDir = command.any { it.startsWith("-auxdir=") }
        if (hasAuxDir) {
            assertFalse(command.any { it.startsWith("-aux-directory=") })
        }
        assertFalse(command.any { it.startsWith("-output-directory=") })
        assertFalse(command.any { it.startsWith("-output-format=") })
        assertEquals(1, command.count { it == "-pdf" })
    }

    fun testLatexRunConfigurationLatexmkCommandHasSingleSynctexFlag() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        latexmkStep(runConfig)
        runConfig.mainFilePath = mainFile.virtualFile.name
        val session = initializeSessionState(runConfig)

        val command = latexmkCommand(runConfig, session)
        assertEquals(1, command.count { it == "-synctex=1" })
    }

    fun testLatexmkOnTexliveUsesAuxdirWhenAuxPathDiffersFromOutdir() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val outputDir = Files.createTempDirectory("texify-latexmk-out")
        val auxDir = Files.createTempDirectory("texify-latexmk-aux")

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.outputPath = outputDir
        runConfig.auxilPath = auxDir
        step.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        val session = initializeSessionState(runConfig)

        val command = latexmkCommand(runConfig, session)
        assertTrue("Expected -outdir in command: $command", command.any { it.startsWith("-outdir=") })
        assertTrue("Expected -auxdir in command: $command", command.any { it.startsWith("-auxdir=") })
    }

    fun testLatexmkOnTexliveDoesNotFallbackWhenOutOrAuxDirDoesNotExist() {
        val mainFile = myFixture.addFileToProject("main.tex", "\\documentclass{article}")
        val root = Files.createTempDirectory("texify-latexmk-missing")
        val outputDir = root.resolve("missing-out")
        val auxDir = root.resolve("missing-aux")

        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.outputPath = outputDir
        runConfig.auxilPath = auxDir
        step.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        val session = initializeSessionState(runConfig)

        val command = latexmkCommand(runConfig, session)
        assertTrue(command.contains("-outdir=$outputDir"))
        assertTrue(command.contains("-auxdir=$auxDir"))
    }

    fun testRunConfigurationsXmlRegistersOnlyLatexProducer() {
        val xml = Files.readString(Path.of("resources/META-INF/extensions/run-configurations.xml"))
        val latexmkProducer = "nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfigurationProducer"
        val latexProducer = "nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer"
        val latexConfigurationType = "nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationType"

        assertFalse(xml.contains(latexmkProducer))
        assertTrue(xml.contains(latexProducer))
        assertTrue(xml.contains(latexConfigurationType))
    }
}
