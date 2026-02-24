package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import org.jdom.Element

class LatexRunStepSchemaPersistenceTest : BasePlatformTestCase() {

    fun testWriteExternalStoresCompileStepsFromStepSchemaTypes() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer
        runConfig.stepSchemaTypes = listOf("compile-latex", "pdf-viewer")

        val root = Element("configuration")
        runConfig.writeExternal(root)

        val parent = root.getChild("texify") ?: error("Missing texify node")
        val steps = parent.getChild("compile-steps") ?: error("Missing compile-steps node")
        val types = steps.getChildren("compile-step").mapNotNull { it.getAttributeValue("type") }

        assertEquals(listOf("compile-latex", "pdf-viewer"), types)
    }

    fun testWriteExternalInfersCompileStepsWhenSchemaTypesAreEmpty() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.LATEXMK
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer
        runConfig.stepSchemaTypes = emptyList()

        val root = Element("configuration")
        runConfig.writeExternal(root)

        val parent = root.getChild("texify") ?: error("Missing texify node")
        val steps = parent.getChild("compile-steps") ?: error("Missing compile-steps node")
        val types = steps.getChildren("compile-step").mapNotNull { it.getAttributeValue("type") }

        assertEquals(listOf("latexmk-compile", "pdf-viewer"), types)
    }

    fun testReadExternalInfersStepTypesFromLegacyWhenSchemaMissing() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val root = Element("configuration")
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText(LatexCompiler.LATEXMK.name))
        parent.addContent(Element("main-file").setText(""))
        root.addContent(parent)

        runConfig.readExternal(root)

        assertEquals(StepSchemaReadStatus.MISSING, runConfig.stepSchemaStatus)
        assertEquals(listOf("latexmk-compile", "pdf-viewer"), runConfig.stepSchemaTypes)
    }

    fun testReadExternalRetainsInvalidSchemaStatusAndFallsBackToLegacyInference() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val root = Element("configuration")
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText(LatexCompiler.LATEXMK.name))
        parent.addContent(Element("main-file").setText(""))
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step"))
        parent.addContent(steps)
        root.addContent(parent)

        runConfig.readExternal(root)

        assertEquals(StepSchemaReadStatus.INVALID, runConfig.stepSchemaStatus)
        assertEquals(listOf("latexmk-compile", "pdf-viewer"), runConfig.stepSchemaTypes)
    }

    fun testReadExternalUsesExplicitSchemaTypesWhenPresent() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val root = Element("configuration")
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText(LatexCompiler.LATEXMK.name))
        parent.addContent(Element("main-file").setText(""))
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step").setAttribute("type", "compile-latex"))
        parent.addContent(steps)
        root.addContent(parent)

        runConfig.readExternal(root)

        assertEquals(StepSchemaReadStatus.PARSED, runConfig.stepSchemaStatus)
        assertEquals(listOf("compile-latex"), runConfig.stepSchemaTypes)
    }

    fun testWriteExternalInfersLegacyBridgeStepsFromAuxRunConfigsAndCompileTwice() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.compiler = LatexCompiler.PDFLATEX
        runConfig.compileTwice = true
        runConfig.pdfViewer = PdfViewer.firstAvailableViewer

        val runManager = RunManagerImpl.getInstanceImpl(project)
        val bib = runManager.createConfiguration("bib", LatexConfigurationFactory(BibtexRunConfigurationType()))
        val makeindex = runManager.createConfiguration("makeindex", LatexConfigurationFactory(MakeindexRunConfigurationType()))
        val external = runManager.createConfiguration("external", LatexConfigurationFactory(ExternalToolRunConfigurationType()))
        runManager.addConfiguration(bib)
        runManager.addConfiguration(makeindex)
        runManager.addConfiguration(external)
        runConfig.bibRunConfigs = setOf(bib)
        runConfig.makeindexRunConfigs = setOf(makeindex)
        runConfig.externalToolRunConfigs = setOf(external)

        val root = Element("configuration")
        runConfig.writeExternal(root)

        val parent = root.getChild("texify") ?: error("Missing texify node")
        val steps = parent.getChild("compile-steps") ?: error("Missing compile-steps node")
        val types = steps.getChildren("compile-step").mapNotNull { it.getAttributeValue("type") }

        assertEquals(
            listOf("latex-compile", "legacy-external-tool", "legacy-makeindex", "legacy-bibtex", "latex-compile", "pdf-viewer"),
            types
        )
    }

    fun testWriteExternalStoresStepUiOptionsByType() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        runConfig.stepUiOptionIdsByType = mutableMapOf(
            StepUiOptionIds.LATEX_COMPILE to mutableSetOf(StepUiOptionIds.COMPILE_PATH, StepUiOptionIds.COMPILE_ARGS),
            StepUiOptionIds.LATEXMK_COMPILE to mutableSetOf(StepUiOptionIds.LATEXMK_MODE),
            StepUiOptionIds.PDF_VIEWER to mutableSetOf(StepUiOptionIds.VIEWER_COMMAND),
        )

        val root = Element("configuration")
        runConfig.writeExternal(root)

        val parent = root.getChild("texify") ?: error("Missing texify node")
        val uiOptions = parent.getChild("step-ui-options") ?: error("Missing step-ui-options node")
        val byType = uiOptions.getChildren("step").associate { step ->
            val type = step.getAttributeValue("type")
            val ids = step.getChildren("option").mapNotNull { it.getAttributeValue("id") }.toSet()
            type to ids
        }

        assertEquals(setOf(StepUiOptionIds.COMPILE_PATH, StepUiOptionIds.COMPILE_ARGS), byType[StepUiOptionIds.LATEX_COMPILE])
        assertEquals(setOf(StepUiOptionIds.LATEXMK_MODE), byType[StepUiOptionIds.LATEXMK_COMPILE])
        assertEquals(setOf(StepUiOptionIds.VIEWER_COMMAND), byType[StepUiOptionIds.PDF_VIEWER])
    }

    fun testReadExternalRestoresStepUiOptionsAndIgnoresInvalidEntries() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )

        val root = Element("configuration")
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText(LatexCompiler.PDFLATEX.name))
        parent.addContent(Element("main-file").setText("main.tex"))
        val uiOptions = Element("step-ui-options")
        uiOptions.addContent(
            Element("step")
                .setAttribute("type", StepUiOptionIds.LATEX_COMPILE)
                .apply {
                    addContent(Element("option").setAttribute("id", StepUiOptionIds.COMPILE_PATH))
                    addContent(Element("option").setAttribute("id", "invalid.option"))
                }
        )
        uiOptions.addContent(
            Element("step")
                .setAttribute("type", "unknown-step")
                .apply {
                    addContent(Element("option").setAttribute("id", StepUiOptionIds.COMPILE_ARGS))
                }
        )
        parent.addContent(uiOptions)
        root.addContent(parent)

        runConfig.readExternal(root)

        assertEquals(
            mutableSetOf(StepUiOptionIds.COMPILE_PATH),
            runConfig.stepUiOptionIdsByType[StepUiOptionIds.LATEX_COMPILE]
        )
        assertFalse(runConfig.stepUiOptionIdsByType.containsKey("unknown-step"))
    }
}
