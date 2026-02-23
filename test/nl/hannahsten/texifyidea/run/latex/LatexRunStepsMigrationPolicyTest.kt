package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.step.LatexCompileRunStep
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.step.LegacyBibtexRunStep
import nl.hannahsten.texifyidea.run.latex.step.LegacyExternalToolRunStep
import nl.hannahsten.texifyidea.run.latex.step.LegacyMakeindexRunStep
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
        val plan = LatexRunStepPlanBuilder.build(listOf("compile-latex", "pdf-viewer"))

        assertEquals(2, plan.steps.size)
        assertTrue(plan.steps[0] is LatexCompileRunStep)
        assertTrue(plan.steps[1] is PdfViewerRunStep)
        assertTrue(plan.unsupportedTypes.isEmpty())
    }

    fun testStepPlanBuilderCollectsUnsupportedTypes() {
        val plan = LatexRunStepPlanBuilder.build(listOf("compile-latex", "unknown-step"))

        assertEquals(1, plan.steps.size)
        assertEquals(listOf("unknown-step"), plan.unsupportedTypes)
    }

    fun testStepPlanBuilderMapsLegacyBridgeStepAliases() {
        val plan = LatexRunStepPlanBuilder.build(listOf("bibliography", "makeindex", "external-tool"))

        assertEquals(3, plan.steps.size)
        assertTrue(plan.steps[0] is LegacyBibtexRunStep)
        assertTrue(plan.steps[1] is LegacyMakeindexRunStep)
        assertTrue(plan.steps[2] is LegacyExternalToolRunStep)
    }

    fun testStepPlanBuilderMapsTemplateStepAliases() {
        val plan = LatexRunStepPlanBuilder.build(listOf("pythontex", "makeglossaries", "xindy"))

        assertEquals(3, plan.steps.size)
        assertEquals("pythontex-command", plan.steps[0].id)
        assertEquals("makeglossaries-command", plan.steps[1].id)
        assertEquals("xindy-command", plan.steps[2].id)
    }

    fun testStepProviderRegistryFindsAliasesCaseInsensitive() {
        assertNotNull(LatexRunStepProviders.find("compile-latex"))
        assertNotNull(LatexRunStepProviders.find("LATEX-COMPILE"))
        assertNotNull(LatexRunStepProviders.find("Open-PDF"))
        assertNotNull(LatexRunStepProviders.find("PYTHONTEX"))
        assertNotNull(LatexRunStepProviders.find("makeglossaries"))
        assertNotNull(LatexRunStepProviders.find("TexIndy"))
    }
}
