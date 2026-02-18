package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import com.intellij.execution.configurations.RuntimeConfigurationError
import org.jdom.Element
import org.jdom.Namespace
import java.nio.file.Files
import java.nio.file.Path

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

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
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        runConfig.compileMode = LatexmkCompileMode.LUALATEX_PDF
        assertEquals(true, unicodeEngineCompatibility(runConfig))

        runConfig.compileMode = LatexmkCompileMode.PDFLATEX_PDF
        assertEquals(false, unicodeEngineCompatibility(runConfig))

        runConfig.compileMode = LatexmkCompileMode.CUSTOM
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

    fun testLegacyMappingMapsSupportedCombination() {
        val element = Element("configuration", Namespace.getNamespace("", ""))
        val parent = Element("texify-latexmk")
        parent.addContent(Element("main-file").also { it.text = "" })
        parent.addContent(Element("engine-mode").also { it.text = "LUALATEX" })
        parent.addContent(Element("latexmk-output-format").also { it.text = "PDF" })
        element.addContent(parent)

        val restored = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )
        restored.readExternal(element)

        assertEquals(LatexmkCompileMode.LUALATEX_PDF, restored.compileMode)
    }

    fun testLegacyMappingRejectsUnsupportedCombination() {
        val element = Element("configuration", Namespace.getNamespace("", ""))
        val parent = Element("texify-latexmk")
        parent.addContent(Element("main-file").also { it.text = "" })
        parent.addContent(Element("engine-mode").also { it.text = "LUALATEX" })
        parent.addContent(Element("latexmk-output-format").also { it.text = "DVI" })
        element.addContent(parent)

        val restored = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )
        restored.readExternal(element)

        assertThrows(RuntimeConfigurationError::class.java) {
            restored.checkConfiguration()
        }
    }

    fun testRunConfigurationsXmlRegistersLatexAndLatexmkProducers() {
        val xml = Files.readString(Path.of("resources/META-INF/extensions/run-configurations.xml"))
        val latexmkProducer = "nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfigurationProducer"
        val latexProducer = "nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer"
        val latexConfigurationType = "nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationType"

        assertTrue(xml.contains(latexmkProducer))
        assertTrue(xml.contains(latexProducer))
        assertTrue(xml.contains(latexConfigurationType))
    }
}
