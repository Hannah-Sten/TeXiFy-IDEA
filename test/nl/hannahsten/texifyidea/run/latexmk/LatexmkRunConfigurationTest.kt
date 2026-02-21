package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import com.intellij.execution.configurations.RuntimeConfigurationError
import org.jdom.Element
import org.jdom.Namespace
import java.nio.file.Files
import java.nio.file.Path

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

    fun testCompileModeContainsAuto() {
        assertTrue(LatexmkCompileMode.entries.contains(LatexmkCompileMode.AUTO))
        assertEquals("AUTO", LatexmkCompileMode.AUTO.toString())
    }

    fun testWriteRead() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        runConfig.compileMode = LatexmkCompileMode.XELATEX_XDV
        runConfig.customEngineCommand = "xelatex %O %S"
        runConfig.citationTool = LatexmkCitationTool.BIBER
        runConfig.extraArguments = "-silent -halt-on-error"
        runConfig.outputPathRaw = "{mainFileParent}"
        runConfig.auxilPathRaw = "{projectDir}/aux"

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)

        val restored = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )
        restored.readExternal(element)

        assertEquals(LatexmkCompileMode.XELATEX_XDV, restored.compileMode)
        assertEquals("xelatex %O %S", restored.customEngineCommand)
        assertEquals(LatexmkCitationTool.BIBER, restored.citationTool)
        assertEquals("-silent -halt-on-error", restored.extraArguments)
        assertEquals("{mainFileParent}", restored.outputPathRaw)
        assertEquals("{projectDir}/aux", restored.auxilPathRaw)
    }

    fun testStructuredArgsAndExtraArgs() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        runConfig.compileMode = LatexmkCompileMode.LATEX_DVI
        runConfig.citationTool = LatexmkCitationTool.DISABLED
        runConfig.extraArguments = "-pdf -interaction=nonstopmode"

        val arguments = runConfig.buildLatexmkArguments()

        assertTrue(arguments.contains("-latex"))
        assertTrue(arguments.contains("-dvi"))
        assertTrue(arguments.contains("-bibtex-"))
        assertTrue(arguments.contains("-pdf"))
        assertTrue(arguments.contains("-interaction=nonstopmode"))
    }

    fun testDefaultExtraArgumentsAreSynctex() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        assertEquals("-synctex=1", runConfig.extraArguments)
    }

    fun testDefaultAuxDirectoryIsEmpty() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        assertEquals("", runConfig.auxilPathRaw)
    }

    fun testDefaultCompileModeIsPdfLatexPdf() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        assertEquals(LatexmkCompileMode.PDFLATEX_PDF, runConfig.compileMode)
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

    fun testPreferredModeFromPackagesUsesXeLatexForCtexBeamerWhenClassComesFromFilesetLibrary() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.fromFileName("ctexbeamer.cls")))
        assertEquals(LatexmkCompileMode.XELATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesUsesLuaLatexForLuaTexJa() {
        val preferred = preferredCompileModeForPackages(setOf(LatexLib.Package("luatexja")))
        assertEquals(LatexmkCompileMode.LUALATEX_PDF, preferred)
    }

    fun testPreferredModeFromPackagesPrefersXeLatexWhenBothXeAndLuaLibrariesArePresent() {
        val preferred = preferredCompileModeForPackages(
            setOf(
                LatexLib.FONTSPEC,
                LatexLib.Package("xecjk"),
            )
        )
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

    fun testLuaLatexEngineDoesNotInjectPdfFlag() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )
        runConfig.compileMode = LatexmkCompileMode.LUALATEX_PDF
        runConfig.citationTool = LatexmkCitationTool.AUTO
        runConfig.extraArguments = null

        val arguments = runConfig.buildLatexmkArguments()
        assertTrue(arguments.contains("-lualatex"))
        assertFalse(arguments.contains("-pdf"))
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

        val arguments = runConfig.buildLatexmkArguments()
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
        runConfig.mainFile = psi.virtualFile
        runConfig.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        val arguments = runConfig.buildLatexmkArguments()
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
        runConfig.mainFile = psi.virtualFile
        runConfig.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        assertEquals(LatexmkCompileMode.LUALATEX_PDF, runConfig.effectiveLatexmkCompileMode())
        assertTrue(runConfig.buildLatexmkArguments().contains("-lualatex"))
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
        runConfig.mainFile = psi.virtualFile
        runConfig.psiFile = psi.createSmartPointer()
        runConfig.latexmkCompileMode = LatexmkCompileMode.AUTO
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null

        assertEquals(LatexmkCompileMode.PDFLATEX_PDF, runConfig.effectiveLatexmkCompileMode())
        assertTrue(runConfig.buildLatexmkArguments().contains("-pdf"))
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
        runConfig.mainFile = mainFile.virtualFile
        runConfig.latexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF
        runConfig.latexmkCitationTool = LatexmkCitationTool.AUTO
        runConfig.latexmkExtraArguments = null
        runConfig.compilerArguments = runConfig.buildLatexmkArguments()

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
        runConfig.mainFile = mainFile.virtualFile
        runConfig.compilerArguments = runConfig.buildLatexmkArguments()

        val command = LatexCompiler.LATEXMK.getCommand(runConfig, project) ?: error("No command generated")
        assertEquals(1, command.count { it == "-synctex=1" })
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

    fun testSetMainFileOnEdtStoresPathWithoutResolving() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        )

        ApplicationManager.getApplication().invokeAndWait {
            runConfig.setMainFile("src/main.tex")
        }

        assertNull(runConfig.mainFile)
        assertEquals("src/main.tex", runConfig.getMainFilePath())
    }

    fun testResolveMainFileIfNeededResolvesFromStoredPath() {
        val file = myFixture.addFileToProject("src/main.tex", "\\documentclass{article}").virtualFile
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        )

        ApplicationManager.getApplication().invokeAndWait {
            runConfig.setMainFile("src/main.tex")
        }

        assertNull(runConfig.mainFile)
        assertEquals(file.path, runConfig.resolveMainFileIfNeeded()?.path)
    }

    fun testWriteExternalKeepsStoredMainFilePathWhenUnresolved() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        )
        ApplicationManager.getApplication().invokeAndWait {
            runConfig.setMainFile("src/main.tex")
        }

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)

        val parent = element.getChild("texify-latexmk")
        assertEquals("src/main.tex", parent.getChildText("main-file"))
    }

    fun testCheckConfigurationAcceptsStoredTexPathWhenUnresolved() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        )
        ApplicationManager.getApplication().invokeAndWait {
            runConfig.setMainFile("src/main.tex")
        }

        runConfig.checkConfiguration()
    }

    fun testCheckConfigurationRejectsStoredNonTexPathWhenUnresolved() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk",
        )
        ApplicationManager.getApplication().invokeAndWait {
            runConfig.setMainFile("src/main.txt")
        }

        assertThrows(RuntimeConfigurationError::class.java) {
            runConfig.checkConfiguration()
        }
    }
}
