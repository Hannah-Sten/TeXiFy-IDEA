package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationProducer
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

class LatexStepSettingsComponentTest : BasePlatformTestCase() {

    fun testShowsCompileCardWhenLatexCompileStepIsSelected() {
        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)

            component.onStepSelectionChanged(0, "latex-compile")

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

            component.onStepSelectionChanged(0, "latexmk-compile")

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

            component.onStepSelectionChanged(0, "pdf-viewer")

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

            component.onStepSelectionChanged(0, "legacy-bibtex")

            assertEquals("unsupported", component.currentCardId())
        }
        finally {
            Disposer.dispose(disposable)
        }
    }

    fun testResetApplyRoundTripPreservesCompileAndViewerSettings() {
        val source = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Source run config"
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
                nl.hannahsten.texifyidea.run.latex.StepUiOptionIds.LATEXMK_COMPILE to mutableSetOf(
                    nl.hannahsten.texifyidea.run.latex.StepUiOptionIds.COMPILE_PATH,
                    nl.hannahsten.texifyidea.run.latex.StepUiOptionIds.LATEXMK_MODE,
                ),
                nl.hannahsten.texifyidea.run.latex.StepUiOptionIds.PDF_VIEWER to mutableSetOf(
                    nl.hannahsten.texifyidea.run.latex.StepUiOptionIds.VIEWER_COMMAND
                )
            )
        }
        val target = LatexRunConfiguration(
            project,
            LatexRunConfigurationProducer().configurationFactory,
            "Target run config"
        )

        val disposable = Disposer.newDisposable()
        try {
            val component = LatexStepSettingsComponent(disposable, project)
            component.onStepSelectionChanged(0, "latexmk-compile")
            component.onStepTypesChanged(listOf("latexmk-compile", "pdf-viewer"))
            component.resetEditorFrom(source)
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
}
