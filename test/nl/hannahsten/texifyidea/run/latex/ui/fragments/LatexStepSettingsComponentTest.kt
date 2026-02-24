package nl.hannahsten.texifyidea.run.latex.ui.fragments

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
            val runConfig = configWithSteps(LatexCompileStepConfig())
            val step = runConfig.model.steps.first()

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
            val runConfig = configWithSteps(LatexmkCompileStepConfig())
            val step = runConfig.model.steps.first()

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
            val runConfig = configWithSteps(PdfViewerStepConfig())
            val step = runConfig.model.steps.first()

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
            val runConfig = configWithSteps(BibtexStepConfig())
            val step = runConfig.model.steps.first()

            component.resetEditorFrom(runConfig)
            component.onStepSelectionChanged(0, step.id, step.type)

            assertEquals("unsupported", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testResetApplyRoundTripPreservesCompileAndViewerSettings() {
        val source = configWithSteps(
            LatexmkCompileStepConfig(),
            PdfViewerStepConfig()
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
            stepUiOptionIdsByType = mutableMapOf(
                StepUiOptionIds.LATEXMK_COMPILE to mutableSetOf(
                    StepUiOptionIds.COMPILE_PATH,
                    StepUiOptionIds.LATEXMK_MODE,
                ),
                StepUiOptionIds.PDF_VIEWER to mutableSetOf(
                    StepUiOptionIds.VIEWER_COMMAND
                )
            )
        }
        val target = configWithSteps(
            LatexmkCompileStepConfig(),
            PdfViewerStepConfig()
        )

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            val selected = source.model.steps.first { it.type == LatexStepType.LATEXMK_COMPILE }
            val viewer = source.model.steps.first { it.type == LatexStepType.PDF_VIEWER }
            component.resetEditorFrom(source)
            component.onStepsChanged(source.model.steps)
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

    private fun configWithSteps(vararg steps: LatexStepConfig): LatexRunConfiguration = LatexRunConfiguration(
        project,
        LatexRunConfigurationProducer().configurationFactory,
        "run config"
    ).apply {
        model = model.copy(steps = steps.toMutableList())
    }
}
