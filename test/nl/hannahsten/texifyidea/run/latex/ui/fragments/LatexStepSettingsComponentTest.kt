package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor

class LatexStepSettingsComponentTest : BasePlatformTestCase() {

    fun testShowsCompileCardWhenLatexCompileStepIsSelected() {
        val runConfig = configWithSteps(LatexCompileStepOptions())
        withComponent(runConfig) { component, _ ->
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("compile", component.currentCardId())
        }
    }

    fun testShowsLatexmkCardWhenLatexmkCompileStepIsSelected() {
        val runConfig = configWithSteps(LatexmkCompileStepOptions())
        withComponent(runConfig) { component, _ ->
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("latexmk", component.currentCardId())
        }
    }

    fun testShowsViewerCardWhenPdfViewerStepIsSelected() {
        val runConfig = configWithSteps(PdfViewerStepOptions())
        withComponent(runConfig) { component, _ ->
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("viewer", component.currentCardId())
        }
    }

    fun testShowsUnsupportedCardForUnconfiguredStepType() {
        val runConfig = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "run config"
        ).apply {
            configOptions.steps = mutableListOf(ExternalToolStepOptions().apply { type = "unsupported-step" })
        }
        withComponent(runConfig) { component, _ ->
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("unsupported", component.currentCardId())
        }
    }

    fun testShowsBibtexCardWhenBibtexStepIsSelected() {
        assertCardForStep("bibtex", BibtexStepOptions())
    }

    fun testShowsMakeindexCardWhenMakeindexStepIsSelected() {
        assertCardForStep("makeindex", MakeindexStepOptions())
    }

    fun testShowsExternalToolCardWhenExternalToolStepIsSelected() {
        assertCardForStep("externalTool", ExternalToolStepOptions())
    }

    fun testShowsPythontexCardWhenPythontexStepIsSelected() {
        assertCardForStep("pythontex", PythontexStepOptions())
    }

    fun testShowsMakeglossariesCardWhenMakeglossariesStepIsSelected() {
        assertCardForStep("makeglossaries", MakeglossariesStepOptions())
    }

    fun testShowsXindyCardWhenXindyStepIsSelected() {
        assertCardForStep("xindy", XindyStepOptions())
    }

    fun testShowsFileCleanupCardWhenCleanupStepIsSelected() {
        assertCardForStep("fileCleanup", FileCleanupStepOptions())
    }

    fun testOnStepsChangedUpdatesCardWhenSelectedStepTypeChanges() {
        val runConfig = configWithSteps(LatexCompileStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            val selected = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, selected.id, selected.type)
            assertEquals("compile", component.currentCardId())

            val replacement = PdfViewerStepOptions().apply {
                id = selected.id
                selectedOptions = selected.selectedOptions
            }
            shadowSteps[0] = replacement
            component.onStepsChanged()

            assertEquals("viewer", component.currentCardId())
        }
    }

    private fun configWithSteps(vararg steps: LatexStepRunConfigurationOptions): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "run config"
    ).apply {
        configOptions.steps = steps.map { it.deepCopy() }.toMutableList()
    }

    private fun assertCardForStep(expectedCardId: String, step: LatexStepRunConfigurationOptions) {
        val runConfig = configWithSteps(step)
        withComponent(runConfig) { component, shadowSteps ->
            val selected = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(0, selected.id, selected.type)

            assertEquals(expectedCardId, component.currentCardId())
        }
    }

    private fun withComponent(
        runConfig: LatexRunConfiguration,
        action: (LatexStepSettingsComponent, MutableList<LatexStepRunConfigurationOptions>) -> Unit,
    ) {
        val editor = LatexSettingsEditor(runConfig)
        editor.shadowSteps.clear()
        editor.shadowSteps.addAll(runConfig.copyStepsForUi())
        action(editor.stepSettingsComponent, editor.shadowSteps)
    }
}
