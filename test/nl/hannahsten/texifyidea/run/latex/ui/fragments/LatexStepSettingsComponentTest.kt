package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor

class LatexStepSettingsComponentTest : BasePlatformTestCase() {

    fun testShowsCompileCardWhenLatexCompileStepIsSelected() {
        val runConfig = configWithSteps(LatexCompileStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("compile", component.currentCardId())
        }
    }

    fun testNoSelectionShowsOnlyUnsupportedMessage() {
        val runConfig = configWithSteps()
        withComponent(runConfig) { component, _ ->
            component.resetEditorFrom()

            assertEquals("unsupported", component.currentCardId())
            assertEquals("Select a step in Compile sequence to configure it.", component.currentUnsupportedMessageForTest())
        }
    }

    fun testShowsLatexmkCardWhenLatexmkCompileStepIsSelected() {
        val runConfig = configWithSteps(LatexmkCompileStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("latexmk", component.currentCardId())
        }
    }

    fun testShowsViewerCardWhenPdfViewerStepIsSelected() {
        val runConfig = configWithSteps(PdfViewerStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

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
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

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

    fun testShowsCompileCardWhenMultipleSameTypeStepsSelected() {
        val runConfig = configWithSteps(LatexCompileStepOptions(), LatexCompileStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(shadowSteps[0].id, shadowSteps[1].id),
                    primaryStepId = shadowSteps[1].id,
                )
            )

            assertEquals("compile", component.currentCardId())
        }
    }

    fun testShowsUnsupportedCardAndMessageForMixedTypeSelection() {
        val runConfig = configWithSteps(LatexCompileStepOptions(), PdfViewerStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(shadowSteps[0].id, shadowSteps[1].id),
                    primaryStepId = shadowSteps[1].id,
                )
            )

            assertEquals("unsupported", component.currentCardId())
            assertEquals(
                "Batch editing is available only when all selected steps have the same type.",
                component.currentUnsupportedMessageForTest()
            )
        }
    }

    fun testApplyCopiesPrimaryTemplateToAllExplicitlySelectedSteps() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
                selectedOptions.add(FragmentedSettings.Option(StepUiOptionIds.COMPILE_ARGS, true))
            },
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "-draftmode"
            },
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/xelatex"
                compilerArguments = "-synctex=1"
            }
        )
        withComponent(runConfig) { component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions
            val third = shadowSteps[2] as LatexCompileStepOptions
            val secondId = second.id

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id),
                    primaryStepId = first.id,
                )
            )
            component.setCompileEditorValuesForTest(
                compilerPath = "/custom/bin/latex",
                compilerArguments = "-file-line-error",
            )

            component.applyEditorTo()

            assertEquals("/custom/bin/latex", first.compilerPath)
            assertEquals("/custom/bin/latex", second.compilerPath)
            assertEquals("-file-line-error", first.compilerArguments)
            assertEquals("-file-line-error", second.compilerArguments)
            assertEquals(secondId, second.id)
            assertEquals(third.compilerPath, "/usr/bin/xelatex")
            assertEquals(third.compilerArguments, "-synctex=1")
            assertEquals(
                listOf(StepUiOptionIds.COMPILE_PATH, StepUiOptionIds.COMPILE_ARGS),
                second.selectedOptions.mapNotNull { it.name }
            )
            assertNotSame(first.selectedOptions, second.selectedOptions)
        }
    }

    fun testDeselectingStepFromBatchDoesNotOverwriteDeselectedStep() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
            },
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "-draftmode"
            },
        )
        withComponent(runConfig) { component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id),
                    primaryStepId = first.id,
                )
            )
            component.setCompileEditorValuesForTest(
                compilerPath = "/custom/bin/latex",
                compilerArguments = "-file-line-error",
            )

            component.onStepSelectionChanged(selectionOf(first.id))

            assertEquals("/custom/bin/latex", first.compilerPath)
            assertEquals("-file-line-error", first.compilerArguments)
            assertEquals("/usr/bin/lualatex", second.compilerPath)
            assertEquals("-draftmode", second.compilerArguments)
        }
    }

    fun testSingleSelectionApplyUpdatesOnlySelectedStep() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply { compilerArguments = "-shell-escape" },
            LatexCompileStepOptions().apply { compilerArguments = "-draftmode" },
        )
        withComponent(runConfig) { component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(first.id))
            component.setCompileEditorValuesForTest(
                compilerPath = "",
                compilerArguments = "-interaction=nonstopmode",
            )

            component.applyEditorTo()

            assertEquals("-interaction=nonstopmode", first.compilerArguments)
            assertEquals("-draftmode", second.compilerArguments)
        }
    }

    fun testOnStepsChangedUpdatesCardWhenSelectedStepTypeChanges() {
        val runConfig = configWithSteps(LatexCompileStepOptions())
        withComponent(runConfig) { component, shadowSteps ->
            val selected = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(selected.id))
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

    private fun selectionOf(
        stepIds: List<String>,
        primaryStepId: String? = stepIds.lastOrNull(),
    ): LatexStepSelectionState = LatexStepSelectionState(
        selectedStepIds = stepIds,
        primaryStepId = primaryStepId,
    )

    private fun selectionOf(stepId: String): LatexStepSelectionState = selectionOf(listOf(stepId), stepId)

    private fun assertCardForStep(expectedCardId: String, step: LatexStepRunConfigurationOptions) {
        val runConfig = configWithSteps(step)
        withComponent(runConfig) { component, shadowSteps ->
            val selected = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(selected.id))

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
