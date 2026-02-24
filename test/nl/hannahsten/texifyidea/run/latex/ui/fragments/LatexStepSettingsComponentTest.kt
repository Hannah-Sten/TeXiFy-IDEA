package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
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
            val runConfig = configWithSteps(BibtexStepOptions())
            val step = runConfig.configOptions.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("unsupported", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
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
        ).apply {
            compiler = LatexCompiler.LATEXMK
            compilerPath = "/tmp/latexmk"
            compilerArguments = "-shell-escape"
            outputFormat = LatexCompiler.Format.PDF
            latexmkCompileMode = LatexmkCompileMode.CUSTOM
            latexmkCustomEngineCommand = "lualatex"
            latexmkCitationTool = LatexmkCitationTool.BIBER
            latexmkExtraArguments = "-interaction=nonstopmode"
            pdfViewer = PdfViewer.firstAvailableViewer
            requireFocus = false
            viewerCommand = "open {pdf}"
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

        assertEquals(LatexCompiler.LATEXMK, target.compiler)
        assertEquals("/tmp/latexmk", target.compilerPath)
        assertEquals("-shell-escape", target.compilerArguments)
        assertEquals(LatexmkCompileMode.CUSTOM, target.latexmkCompileMode)
        assertEquals("lualatex", target.latexmkCustomEngineCommand)
        assertEquals(LatexmkCitationTool.BIBER, target.latexmkCitationTool)
        assertEquals("-interaction=nonstopmode", target.latexmkExtraArguments)
        assertEquals(PdfViewer.firstAvailableViewer, target.pdfViewer)
        assertFalse(target.requireFocus)
        assertEquals("open {pdf}", target.viewerCommand)
    }

    private fun configWithSteps(vararg steps: LatexStepRunConfigurationOptions): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "run config"
    ).apply {
        configOptions.steps = steps.map { it.deepCopy() }.toMutableList()
    }
}
