package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.step.BibtexRunStep
import nl.hannahsten.texifyidea.run.latex.step.ExternalToolRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexCompileRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.step.MakeindexRunStep
import nl.hannahsten.texifyidea.run.latex.step.PdfViewerRunStep
import org.jdom.Element

class LatexRunStepsMigrationPolicyTest : BasePlatformTestCase() {

    fun testChooseExecutionPipelineUsesLegacyWhenSchemaMissing() {
        val mode = LatexRunStepsMigrationPolicy.chooseExecutionPipeline(stepSchemaStatus = StepSchemaReadStatus.MISSING)

        assertEquals(ExecutionPipelineMode.LEGACY, mode)
    }

    fun testChooseExecutionPipelineUsesStepsWhenSchemaParsed() {
        val mode = LatexRunStepsMigrationPolicy.chooseExecutionPipeline(stepSchemaStatus = StepSchemaReadStatus.PARSED)

        assertEquals(ExecutionPipelineMode.STEPS, mode)
    }

    fun testChooseExecutionPipelineFallsBackToLegacyWhenSchemaInvalid() {
        val mode = LatexRunStepsMigrationPolicy.chooseExecutionPipeline(stepSchemaStatus = StepSchemaReadStatus.INVALID)

        assertEquals(ExecutionPipelineMode.LEGACY, mode)
    }

    fun testProbeStepSchemaReturnsMissingWhenContainerAbsent() {
        val parent = Element("texify")

        assertEquals(StepSchemaReadStatus.MISSING, LatexRunConfigurationSerializer.probeStepSchema(parent))
    }

    fun testProbeStepSchemaReturnsParsedForTypeAttribute() {
        val parent = Element("texify")
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step").setAttribute("type", "latex-compile"))
        parent.addContent(steps)

        assertEquals(StepSchemaReadStatus.PARSED, LatexRunConfigurationSerializer.probeStepSchema(parent))
    }

    fun testProbeStepSchemaReturnsParsedForLegacyStepNameAttribute() {
        val parent = Element("texify")
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step").setAttribute("step-name", "compile-latex"))
        parent.addContent(steps)

        assertEquals(StepSchemaReadStatus.PARSED, LatexRunConfigurationSerializer.probeStepSchema(parent))
    }

    fun testProbeStepSchemaReturnsInvalidForUnnamedStep() {
        val parent = Element("texify")
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step"))
        parent.addContent(steps)

        assertEquals(StepSchemaReadStatus.INVALID, LatexRunConfigurationSerializer.probeStepSchema(parent))
    }

    fun testReadExternalStoresStepSchemaStatus() {
        val runConfig = LatexRunConfiguration(
            myFixture.project,
            LatexRunConfigurationProducer().configurationFactory,
            "Test run config"
        )
        val root = Element("configuration")
        val parent = Element("texify")
        parent.addContent(Element("compiler").setText("PDFLATEX"))
        parent.addContent(Element("main-file").setText(""))
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step").setAttribute("type", "latex-compile"))
        parent.addContent(steps)
        root.addContent(parent)

        runConfig.readExternal(root)

        assertEquals(StepSchemaReadStatus.PARSED, runConfig.stepSchemaStatus)
    }

    fun testReadStepTypesPrefersTypeThenStepName() {
        val parent = Element("texify")
        val steps = Element("compile-steps")
        steps.addContent(Element("compile-step").setAttribute("type", "latex-compile"))
        steps.addContent(Element("compile-step").setAttribute("step-name", "pdf-viewer"))
        parent.addContent(steps)

        assertEquals(listOf("latex-compile", "pdf-viewer"), LatexRunConfigurationSerializer.readStepTypes(parent))
    }

    fun testStepPlanBuilderMapsKnownTypesAndKeepsOrder() {
        val plan = LatexRunStepPlanBuilder.build(listOf(LatexCompileStepConfig(), PdfViewerStepConfig()))

        assertEquals(2, plan.steps.size)
        assertTrue(plan.steps[0] is LatexCompileRunStep)
        assertTrue(plan.steps[1] is PdfViewerRunStep)
        assertTrue(plan.unsupportedTypes.isEmpty())
    }

    fun testStepPlanBuilderMapsLatexmkCompileType() {
        val plan = LatexRunStepPlanBuilder.build(listOf(LatexmkCompileStepConfig(), PdfViewerStepConfig()))

        assertEquals(2, plan.steps.size)
        assertEquals("latexmk-compile", plan.steps[0].id)
        assertTrue(plan.steps[1] is PdfViewerRunStep)
        assertTrue(plan.unsupportedTypes.isEmpty())
    }

    fun testStepPlanBuilderDoesNotCollectUnsupportedForStrongTypedConfigs() {
        val plan = LatexRunStepPlanBuilder.build(
            listOf(
                LatexCompileStepConfig(),
                PdfViewerStepConfig(),
            )
        )

        assertEquals(2, plan.steps.size)
        assertTrue(plan.unsupportedTypes.isEmpty())
    }

    fun testStepPlanBuilderMapsLegacyBridgeStepAliases() {
        val plan = LatexRunStepPlanBuilder.build(
            listOf(
                BibtexStepConfig(),
                MakeindexStepConfig(),
                ExternalToolStepConfig(commandLine = "echo hi"),
            )
        )

        assertEquals(3, plan.steps.size)
        assertTrue(plan.steps[0] is BibtexRunStep)
        assertTrue(plan.steps[1] is MakeindexRunStep)
        assertTrue(plan.steps[2] is ExternalToolRunStep)
    }

    fun testStepPlanBuilderMapsTemplateStepAliases() {
        val plan = LatexRunStepPlanBuilder.build(
            listOf(
                PythontexStepConfig(),
                MakeglossariesStepConfig(),
                XindyStepConfig(),
            )
        )

        assertEquals(3, plan.steps.size)
        assertEquals("pythontex", plan.steps[0].id)
        assertEquals("makeglossaries", plan.steps[1].id)
        assertEquals("xindy", plan.steps[2].id)
    }

    fun testStepProviderRegistryFindsAliasesCaseInsensitive() {
        assertNotNull(LatexRunStepProviders.find("compile-latex"))
        assertNotNull(LatexRunStepProviders.find("LATEX-COMPILE"))
        assertNotNull(LatexRunStepProviders.find("Open-PDF"))
        assertNotNull(LatexRunStepProviders.find("latexmk-compile"))
        assertNotNull(LatexRunStepProviders.find("PYTHONTEX"))
        assertNotNull(LatexRunStepProviders.find("makeglossaries"))
        assertNotNull(LatexRunStepProviders.find("TexIndy"))
    }
}
