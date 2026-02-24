package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import org.jdom.Element
import org.jdom.Namespace

class LatexRunConfigurationTest : BasePlatformTestCase() {

    fun testConfigurationEditorUsesFragmentedSettingsEditor() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val editor = runConfig.configurationEditor
        assertTrue(editor is LatexSettingsEditor)
    }

    fun testWriteReadRoundTripPreservesCommonAndSteps() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.mainFilePath = "main.tex"
        runConfig.workingDirectory = java.nio.file.Path.of("{mainFileParent}")
        runConfig.outputPath = java.nio.file.Path.of("{projectDir}/out")
        runConfig.auxilPath = java.nio.file.Path.of("{projectDir}/aux")
        runConfig.latexDistribution = LatexDistributionType.TEXLIVE

        runConfig.configOptions.steps = mutableListOf(
            LatexmkCompileStepOptions().apply {
                id = "compile-1"
                compilerPath = "/usr/bin/latexmk"
                compilerArguments = "-shell-escape"
                latexmkCompileMode = LatexmkCompileMode.CUSTOM
                latexmkCustomEngineCommand = "xelatex"
            },
            PdfViewerStepOptions().apply {
                id = "viewer-1"
                customViewerCommand = "open {pdf}"
                requireFocus = false
            }
        )

        val element = Element("configuration", Namespace.getNamespace("", ""))
        runConfig.writeExternal(element)

        val restored = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Restored")
        restored.readExternal(element)

        assertEquals("main.tex", restored.mainFilePath)
        assertEquals("{projectDir}/out", restored.outputPath.toString())
        assertEquals("{projectDir}/aux", restored.auxilPath.toString())
        assertEquals(LatexDistributionType.TEXLIVE, restored.latexDistribution)
        assertEquals(2, restored.configOptions.steps.size)
        assertEquals("latexmk-compile", restored.configOptions.steps[0].type)
        assertEquals("pdf-viewer", restored.configOptions.steps[1].type)
        assertEquals("compile-1", restored.configOptions.steps[0].id)
        assertEquals("viewer-1", restored.configOptions.steps[1].id)
    }

    fun testEmptyStepListFallsBackToDefaultSteps() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        runConfig.configOptions.steps = mutableListOf()

        runConfig.configOptions.ensureDefaultSteps()

        assertEquals(listOf(LatexStepType.LATEX_COMPILE, LatexStepType.PDF_VIEWER), runConfig.configOptions.steps.map { it.type })
    }

    fun testFragmentedEditorUsesCommonSequenceAndStepSettingsLayout() {
        val runConfig = LatexRunConfiguration(myFixture.project, LatexRunConfigurationProducer().configurationFactory, "Test run config")
        val editor = LatexSettingsEditor(runConfig)
        val createRunFragments = LatexSettingsEditor::class.java.getDeclaredMethod("createRunFragments")
        createRunFragments.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val fragments = createRunFragments.invoke(editor) as MutableList<com.intellij.execution.ui.SettingsEditorFragment<LatexRunConfiguration, *>>
        val ids = fragments.mapNotNull { fragment ->
            runCatching {
                fragment.javaClass.getMethod("getId").invoke(fragment) as? String
            }.getOrNull()
        }

        assertTrue(ids.contains("mainFile"))
        assertTrue(ids.contains("latexDistribution"))
        assertTrue(ids.contains("workingDirectory"))
        assertTrue(ids.contains("outputDirectory"))
        assertTrue(ids.contains("auxiliaryDirectory"))
        assertTrue(ids.contains("environmentVariables"))
        assertTrue(ids.contains("compileSequence"))
        assertTrue(ids.contains("stepSettings"))
        assertTrue(ids.indexOf("mainFile") < ids.indexOf("compileSequence"))
        assertTrue(ids.indexOf("compileSequence") < ids.indexOf("stepSettings"))
        assertFalse(ids.contains("legacyAdvancedOptions"))
    }
}
