package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import org.jdom.Element
import org.jdom.Namespace

class LatexmkRunConfigurationTest : BasePlatformTestCase() {

    fun testWriteRead() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        runConfig.engineMode = LatexmkEngineMode.XELATEX
        runConfig.customEngineCommand = "xelatex %O %S"
        runConfig.latexmkOutputFormat = LatexmkOutputFormat.PS
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

        assertEquals(LatexmkEngineMode.XELATEX, restored.engineMode)
        assertEquals("xelatex %O %S", restored.customEngineCommand)
        assertEquals(LatexmkOutputFormat.PS, restored.latexmkOutputFormat)
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

        runConfig.engineMode = LatexmkEngineMode.XELATEX
        runConfig.latexmkOutputFormat = LatexmkOutputFormat.DVI
        runConfig.citationTool = LatexmkCitationTool.DISABLED
        runConfig.extraArguments = "-pdf -interaction=nonstopmode"

        val arguments = runConfig.buildLatexmkArguments()

        assertTrue(arguments.contains("-xelatex"))
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

    fun testDefaultOutputFormatIsPdf() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        assertEquals(LatexmkOutputFormat.PDF, runConfig.latexmkOutputFormat)
    }

    fun testPreferredEngineFromPackagesFallsBackToLuaLatex() {
        val preferred = preferredEngineForPackages(setOf(LatexLib.FONTSPEC))
        assertEquals(LatexmkEngineMode.LUALATEX, preferred)
    }

    fun testMagicCompilerOverridesWithRecognizedEngine() {
        val engine = engineFromMagicCommand("xelatex -synctex=1")
        assertEquals(LatexmkEngineMode.XELATEX, engine)
    }

    fun testUnicodeEngineCompatibilityUsesLatexmkEngineMode() {
        val runConfig = LatexmkRunConfiguration(
            myFixture.project,
            LatexConfigurationFactory(LatexmkRunConfigurationType()),
            "Latexmk"
        )

        runConfig.engineMode = LatexmkEngineMode.LUALATEX
        assertEquals(true, unicodeEngineCompatibility(runConfig))

        runConfig.engineMode = LatexmkEngineMode.PDFLATEX
        assertEquals(false, unicodeEngineCompatibility(runConfig))

        runConfig.engineMode = LatexmkEngineMode.CUSTOM_COMMAND
        assertEquals(null, unicodeEngineCompatibility(runConfig))
    }
}
