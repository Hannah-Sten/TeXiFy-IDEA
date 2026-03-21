package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.MakeindexProgram
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

    fun testBibtexHintUsesAuxPathWhenIndependent() {
        val runConfig = configWithSteps(BibtexStepOptions()).apply {
            configOptions.outputPath = "{projectDir}/out"
            configOptions.auxilPath = "{projectDir}/aux"
        }
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("{projectDir}/aux", component.currentBibtexWorkingDirectoryHintForTest())
        }
    }

    fun testBibtexHintFallsBackToOutputWhenAuxMatchesOutput() {
        val runConfig = configWithSteps(BibtexStepOptions()).apply {
            configOptions.outputPath = "build/out"
            configOptions.auxilPath = "build/out"
        }
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("build/out", component.currentBibtexWorkingDirectoryHintForTest())
        }
    }

    fun testBibtexHintFallsBackToOutputWhenAuxIsUnset() {
        val runConfig = configWithSteps(BibtexStepOptions()).apply {
            configOptions.outputPath = "{projectDir}/out"
            configOptions.auxilPath = null
        }
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("{projectDir}/out", component.currentBibtexWorkingDirectoryHintForTest())
        }
    }

    fun testMakeindexHintUsesAuxPathWhenIndependent() {
        val runConfig = configWithSteps(MakeindexStepOptions()).apply {
            configOptions.outputPath = "\$PROJECT_DIR\$/out"
            configOptions.auxilPath = "\$PROJECT_DIR\$/aux"
        }
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals("\$PROJECT_DIR\$/aux", component.currentMakeindexWorkingDirectoryHintForTest())
        }
    }

    fun testMakeindexBib2glsHintUsesMainFileParentPlaceholder() {
        val runConfig = configWithSteps(
            MakeindexStepOptions().apply {
                program = MakeindexProgram.BIB2GLS
            }
        ).apply {
            configOptions.outputPath = "{projectDir}/out"
            configOptions.auxilPath = "{projectDir}/aux"
        }
        withComponent(runConfig) { component, shadowSteps ->
            val step = shadowSteps.first()

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(step.id))

            assertEquals(LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER, component.currentMakeindexWorkingDirectoryHintForTest())
        }
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

    fun testEditingDuringSameTypeMultiSelectionUpdatesOnlyPrimaryStep() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
            },
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "-draftmode"
            }
        )
        withComponent(runConfig) { component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id),
                    primaryStepId = second.id,
                )
            )
            component.setCompileEditorValuesForTest(
                compilerPath = "/custom/bin/latex",
                compilerArguments = "-file-line-error",
            )
            component.flushCurrentStep()

            assertEquals("/usr/bin/pdflatex", first.compilerPath)
            assertEquals("-shell-escape", first.compilerArguments)
            assertEquals("/custom/bin/latex", second.compilerPath)
            assertEquals("-file-line-error", second.compilerArguments)
        }
    }

    fun testOnStepSettingsChangedFlushesOnlyPrimaryStep() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
            },
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "-draftmode"
            }
        )
        withEditor(runConfig) { editor, component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id),
                    primaryStepId = second.id,
                )
            )
            component.setCompileEditorValuesForTest(
                compilerPath = "/custom/bin/latex",
                compilerArguments = "-file-line-error",
            )

            editor.onStepSettingsChanged()

            assertEquals("/usr/bin/pdflatex", first.compilerPath)
            assertEquals("-shell-escape", first.compilerArguments)
            assertEquals("/custom/bin/latex", second.compilerPath)
            assertEquals("-file-line-error", second.compilerArguments)
        }
    }

    fun testSelectingAdditionalSameTypeStepResetsCardFromNewPrimaryWithoutWritingPreviousCard() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
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
            component.flushCurrentStep()

            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id, third.id),
                    primaryStepId = third.id,
                )
            )

            assertEquals("/custom/bin/latex", first.compilerPath)
            assertEquals("-file-line-error", first.compilerArguments)
            assertEquals("/usr/bin/lualatex", second.compilerPath)
            assertEquals("-draftmode", second.compilerArguments)
            assertEquals("/usr/bin/xelatex", third.compilerPath)
            assertEquals("-synctex=1", third.compilerArguments)
            assertEquals(
                third.compilerPath.orEmpty() to third.compilerArguments.orEmpty(),
                component.currentCompileEditorValuesForTest()
            )
        }
    }

    fun testSelectionChangeFromMultiToSingleDoesNotBroadcastCurrentCard() {
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
            component.flushCurrentStep()

            component.onStepSelectionChanged(selectionOf(first.id))

            assertEquals("/custom/bin/latex", first.compilerPath)
            assertEquals("-file-line-error", first.compilerArguments)
            assertEquals("/usr/bin/lualatex", second.compilerPath)
            assertEquals("-draftmode", second.compilerArguments)
        }
    }

    fun testSelectionChangeWithDifferentCompilersDoesNotOverwriteClickedStepDuringReset() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compiler = LatexCompiler.LUALATEX
                compilerPath = "/usr/bin/lualatex"
                compilerArguments = "--lua"
            },
            LatexCompileStepOptions().apply {
                compiler = LatexCompiler.PDFLATEX
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "--pdf"
            }
        )
        withComponent(runConfig) { component, shadowSteps ->
            val lualatex = shadowSteps[0] as LatexCompileStepOptions
            val pdflatex = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(selectionOf(lualatex.id))
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(lualatex.id, pdflatex.id),
                    primaryStepId = pdflatex.id,
                )
            )
            component.onStepSelectionChanged(selectionOf(lualatex.id))

            assertEquals(LatexCompiler.LUALATEX, lualatex.compiler)
            assertEquals("/usr/bin/lualatex", lualatex.compilerPath)
            assertEquals("--lua", lualatex.compilerArguments)
            assertEquals(LatexCompiler.PDFLATEX, pdflatex.compiler)
            assertEquals("/usr/bin/pdflatex", pdflatex.compilerPath)
            assertEquals("--pdf", pdflatex.compilerArguments)
        }
    }

    fun testApplyEditorToDoesNothingForMixedTypeSelection() {
        val runConfig = configWithSteps(
            LatexCompileStepOptions().apply {
                compilerPath = "/usr/bin/pdflatex"
                compilerArguments = "-shell-escape"
            },
            PdfViewerStepOptions().apply {
                pdfViewerName = "Custom Viewer"
                customViewerCommand = "open {pdf}"
            }
        )
        withComponent(runConfig) { component, shadowSteps ->
            val compile = shadowSteps[0] as LatexCompileStepOptions
            val viewer = shadowSteps[1] as PdfViewerStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(compile.id, viewer.id),
                    primaryStepId = viewer.id,
                )
            )
            component.applyEditorTo()

            assertEquals("/usr/bin/pdflatex", compile.compilerPath)
            assertEquals("-shell-escape", compile.compilerArguments)
            assertEquals("Custom Viewer", viewer.pdfViewerName)
            assertEquals("open {pdf}", viewer.customViewerCommand)
        }
    }

    fun testBeforeSequenceStructureChangeFlushesOnlyPrimaryStep() {
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
        withEditor(runConfig) { editor, component, shadowSteps ->
            val first = shadowSteps[0] as LatexCompileStepOptions
            val second = shadowSteps[1] as LatexCompileStepOptions

            component.resetEditorFrom()
            component.onStepSelectionChanged(
                selectionOf(
                    listOf(first.id, second.id),
                    primaryStepId = second.id,
                )
            )
            component.setCompileEditorValuesForTest(
                compilerPath = "/custom/bin/latex",
                compilerArguments = "-file-line-error",
            )

            editor.beforeSequenceStructureChange()

            assertEquals("/usr/bin/pdflatex", first.compilerPath)
            assertEquals("-shell-escape", first.compilerArguments)
            assertEquals("/custom/bin/latex", second.compilerPath)
            assertEquals("-file-line-error", second.compilerArguments)
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
        withEditor(runConfig) { _, component, shadowSteps ->
            action(component, shadowSteps)
        }
    }

    private fun withEditor(
        runConfig: LatexRunConfiguration,
        action: (LatexSettingsEditor, LatexStepSettingsComponent, MutableList<LatexStepRunConfigurationOptions>) -> Unit,
    ) {
        val editor = LatexSettingsEditor(runConfig)
        editor.stepSettingsComponent.changeListener = {
            editor.onStepSettingsChanged()
        }
        editor.shadowSteps.clear()
        editor.shadowSteps.addAll(runConfig.copyStepsForUi())
        action(editor, editor.stepSettingsComponent, editor.shadowSteps)
    }
}
