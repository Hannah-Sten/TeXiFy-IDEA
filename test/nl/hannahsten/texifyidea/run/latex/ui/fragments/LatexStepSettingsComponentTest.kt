package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.*
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

class LatexStepSettingsComponentTest : BasePlatformTestCase() {

    fun testShowsCompileCardWhenLatexCompileStepIsSelected() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val runConfig = configWithSteps(LatexCompileStepOptions())
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("compile", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testShowsLatexmkCardWhenLatexmkCompileStepIsSelected() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val runConfig = configWithSteps(LatexmkCompileStepOptions())
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("latexmk", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testShowsViewerCardWhenPdfViewerStepIsSelected() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val runConfig = configWithSteps(PdfViewerStepOptions())
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("viewer", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testShowsUnsupportedCardForUnconfiguredStepType() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val runConfig = configWithSteps(ExternalToolStepOptions().apply { type = "unsupported-step" })
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("unsupported", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
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

    fun testResetApplyRoundTripPreservesCompileAndViewerSettings() {
        val selectedOptions = mutableListOf(
            FragmentedSettings.Option(StepUiOptionIds.COMPILE_PATH, true),
            FragmentedSettings.Option(StepUiOptionIds.LATEXMK_MODE, true),
        )

        val source = configWithSteps(
            LatexmkCompileStepOptions().apply {
                this.selectedOptions.addAll(selectedOptions)
            },
            PdfViewerStepOptions().apply {
                this.selectedOptions.add(FragmentedSettings.Option(StepUiOptionIds.VIEWER_COMMAND, true))
            }
        ).also { config ->
            (config.configOptions.steps.first { it.type == LatexStepType.LATEXMK_COMPILE } as LatexmkCompileStepOptions).apply {
                compilerPath = "/tmp/latexmk"
                compilerArguments = "-shell-escape"
                latexmkCompileMode = LatexmkCompileMode.CUSTOM
                latexmkCustomEngineCommand = "lualatex"
                latexmkCitationTool = LatexmkCitationTool.BIBER
                latexmkExtraArguments = "-interaction=nonstopmode"
            }
            (config.configOptions.steps.first { it.type == LatexStepType.PDF_VIEWER } as PdfViewerStepOptions).apply {
                pdfViewerName = PdfViewer.firstAvailableViewer.name
                requireFocus = false
                customViewerCommand = "open {pdf}"
            }
        }
        val target = configWithSteps(
            LatexmkCompileStepOptions(),
            PdfViewerStepOptions()
        )

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val selected = source.configOptions.steps.first { it.type == LatexStepType.LATEXMK_COMPILE }
            val viewer = source.configOptions.steps.first { it.type == LatexStepType.PDF_VIEWER }
            component.resetEditorFrom(source)
            component.onStepsChanged(source.configOptions.steps)
            component.onStepSelectionChanged(0, selected.id, selected.type)
            component.onStepSelectionChanged(1, viewer.id, viewer.type)
            component.applyEditorTo(target)
        }
        finally {
            Disposer.dispose(disposable)
        }

        val targetLatexmk = target.configOptions.steps.first { it.type == LatexStepType.LATEXMK_COMPILE } as LatexmkCompileStepOptions
        val targetViewer = target.configOptions.steps.first { it.type == LatexStepType.PDF_VIEWER } as PdfViewerStepOptions
        assertEquals("/tmp/latexmk", targetLatexmk.compilerPath)
        assertEquals("-shell-escape", targetLatexmk.compilerArguments)
        assertEquals(LatexmkCompileMode.CUSTOM, targetLatexmk.latexmkCompileMode)
        assertEquals("lualatex", targetLatexmk.latexmkCustomEngineCommand)
        assertEquals(LatexmkCitationTool.BIBER, targetLatexmk.latexmkCitationTool)
        assertEquals("-interaction=nonstopmode", targetLatexmk.latexmkExtraArguments)
        assertEquals(PdfViewer.firstAvailableViewer.name, targetViewer.pdfViewerName)
        assertFalse(targetViewer.requireFocus)
        assertEquals("open {pdf}", targetViewer.customViewerCommand)
    }

    private fun configWithSteps(vararg steps: LatexStepRunConfigurationOptions): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "run config"
    ).apply {
        configOptions.steps = steps.map { it.deepCopy() }.toMutableList()
    }

    private fun assertCardForStep(expectedCardId: String, step: LatexStepRunConfigurationOptions) {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val runConfig = configWithSteps(step)
            val selected = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, selected.id, selected.type)

            assertEquals(expectedCardId, component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }
}
