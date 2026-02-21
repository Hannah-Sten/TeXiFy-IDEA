package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.psi.createSmartPointer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexPathResolver
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.run.latex.LatexmkModeService
import java.nio.file.Files
import java.nio.file.Path

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

    private fun initializeExecutionState(runConfig: LatexRunConfiguration) {
        val mainFile = runConfig.executionState.resolvedMainFile ?: LatexRunConfigurationStaticSupport.resolveMainFile(runConfig) ?: return
        runConfig.executionState.resolvedMainFile = mainFile
        runConfig.executionState.resolvedOutputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile)
        runConfig.executionState.resolvedAuxDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile)
        runConfig.executionState.effectiveLatexmkCompileMode = LatexmkModeService.effectiveCompileMode(runConfig)
        runConfig.executionState.effectiveCompilerArguments = LatexmkModeService.buildArguments(runConfig, runConfig.executionState.effectiveLatexmkCompileMode)
        runConfig.executionState.isInitialized = true
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
        runConfig.compiler = LatexCompiler.LATEXMK

        runConfig.latexmkCompileMode = LatexmkCompileMode.LUALATEX_PDF
        assertEquals(true, unicodeEngineCompatibility(runConfig))

        runConfig.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        assertEquals(false, unicodeEngineCompatibility(runConfig))

        runConfig.latexmkCompileMode = LatexmkCompileMode.CUSTOM
        assertEquals(null, unicodeEngineCompatibility(runConfig))
    }

    fun testLatexRunConfigurationLatexmkArgumentsIgnoreOutputFormat() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "LaTeX"
        )
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexmkCompileMode = LatexmkCompileMode.LUALATEX_PDF
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null
        runConfig.outputFormat = Format.DVI

        val arguments = LatexmkModeService.buildArguments(runConfig)
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = psi.virtualFile.name
        runConfig.executionState.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        initializeExecutionState(runConfig)
        val arguments = LatexmkModeService.buildArguments(runConfig)
        assertTrue(arguments.contains("-xelatex"))
        assertTrue(arguments.contains("-xdv"))
        assertTrue(runConfig.getOutputFilePath().endsWith(".xdv"))
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = psi.virtualFile.name
        runConfig.executionState.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        assertEquals(LatexmkCompileMode.LUALATEX_PDF, LatexmkModeService.effectiveCompileMode(runConfig))
        assertTrue(LatexmkModeService.buildArguments(runConfig).contains("-lualatex"))
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = psi.virtualFile.name
        runConfig.executionState.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        assertEquals(LatexmkCompileMode.PDFLATEX_PDF, LatexmkModeService.effectiveCompileMode(runConfig))
        assertTrue(LatexmkModeService.buildArguments(runConfig).contains("-pdf"))
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null
        runConfig.compilerArguments = LatexmkModeService.buildArguments(runConfig)
        initializeExecutionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project) ?: error("No command generated")

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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.compilerArguments = LatexmkModeService.buildArguments(runConfig)
        initializeExecutionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project) ?: error("No command generated")
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.outputPath = outputDir
        runConfig.auxilPath = auxDir
        runConfig.compilerArguments = LatexmkModeService.buildArguments(runConfig)
        initializeExecutionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project) ?: error("No command generated")
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
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE
        runConfig.mainFilePath = mainFile.virtualFile.name
        runConfig.outputPath = outputDir
        runConfig.auxilPath = auxDir
        runConfig.compilerArguments = LatexmkModeService.buildArguments(runConfig)
        initializeExecutionState(runConfig)

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project) ?: error("No command generated")
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
