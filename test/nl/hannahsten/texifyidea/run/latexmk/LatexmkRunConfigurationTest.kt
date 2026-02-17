package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import com.intellij.execution.configurations.RuntimeConfigurationError
import org.jdom.Element
import org.jdom.Namespace

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

    fun testMagicCompilerOverridesWithRecognizedMode() {
        val mode = compileModeFromMagicCommand("xelatex -synctex=1")
        assertEquals(LatexmkCompileMode.XELATEX_PDF, mode)
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
}
