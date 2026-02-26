package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.psi.createSmartPointer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexRunSessionState
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService
import java.nio.file.Files
import java.nio.file.Path

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

    private fun latexmkStep(runConfig: LatexRunConfiguration): LatexmkCompileStepOptions = runConfig.ensurePrimaryCompileStepLatexmk()

    private fun initializeSessionState(runConfig: LatexRunConfiguration): LatexRunSessionState {
        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig) ?: return LatexRunSessionState()
        val step = latexmkStep(runConfig)
        val session = LatexRunSessionState(
            resolvedMainFile = mainFile,
            resolvedWorkingDirectory = LatexPathResolver.resolve(runConfig.workingDirectory, mainFile, runConfig.project),
            resolvedOutputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile),
            resolvedAuxDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile),
        )
        session.effectiveLatexmkCompileMode = LatexmkModeService.effectiveCompileMode(runConfig, session, step)
        session.effectiveCompilerArguments = LatexmkModeService.buildArguments(runConfig, session, step, session.effectiveLatexmkCompileMode)
        val extension = (session.effectiveLatexmkCompileMode ?: LatexmkCompileMode.PDFLATEX_PDF).extension.lowercase()
        session.resolvedOutputFilePath = "${session.resolvedOutputDir?.path}/${mainFile.nameWithoutExtension}.$extension"
        return session
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
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        val step = latexmkStep(runConfig)
        step.latexmkCompileMode = LatexmkCompileMode.LUALATEX_PDF
        step.latexmkCitationTool = LatexmkCitationTool.AUTO
        step.latexmkExtraArguments = null

        val session = LatexRunSessionState()
        val arguments = LatexmkModeService.buildArguments(runConfig, session, step)
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

        val session = initializeSessionState(runConfig).apply { psiFile = psi.createSmartPointer() }
        session.effectiveLatexmkCompileMode = LatexmkModeService.effectiveCompileMode(runConfig, session, step)
        session.effectiveCompilerArguments = LatexmkModeService.buildArguments(runConfig, session, step, session.effectiveLatexmkCompileMode)
        val arguments = session.effectiveCompilerArguments.orEmpty()
        assertTrue(arguments.contains("-xelatex"))
        assertTrue(arguments.contains("-xdv"))
        assertTrue(session.resolvedOutputFilePath?.endsWith(".xdv") == true)
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

        val session = initializeSessionState(runConfig).apply { psiFile = psi.createSmartPointer() }
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, LatexmkModeService.effectiveCompileMode(runConfig, session, step))
        assertTrue(LatexmkModeService.buildArguments(runConfig, session, step).contains("-lualatex"))
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

        val session = initializeSessionState(runConfig).apply { psiFile = psi.createSmartPointer() }
        assertEquals(LatexmkCompileMode.PDFLATEX_PDF, LatexmkModeService.effectiveCompileMode(runConfig, session, step))
        assertTrue(LatexmkModeService.buildArguments(runConfig, session, step).contains("-pdf"))
    }

    fun testLatexmkCompilerExposesNoOutputFormatSwitch() {
        assertEquals(listOf(Format.PDF), LatexCompiler.LATEXMK.outputFormats.toList())
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

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project, session) ?: error("No command generated")

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

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project, session) ?: error("No command generated")
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
        val session = initializeSessionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project, session) ?: error("No command generated")
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
        val session = initializeSessionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project, session) ?: error("No command generated")
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
