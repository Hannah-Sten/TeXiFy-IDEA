package nl.hannahsten.texifyidea.run.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.CustomPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceViewer
import nl.hannahsten.texifyidea.run.pdfviewer.NoViewer

class LatexStepRunConfigurationOptionsDisplayNameTest : BasePlatformTestCase() {

    fun testNonCustomizedStepDisplayNameUsesStaticPresentation() {
        assertEquals("Run bibliography", BibtexStepOptions().displayName())
    }

    fun testLatexCompileDisplayNameUsesCompilerName() {
        val step = LatexCompileStepOptions().apply {
            compiler = LatexCompiler.LUALATEX
        }

        assertEquals("Compile with LuaLaTeX", step.displayName())
    }

    fun testLatexmkDisplayNameUsesCompileMode() {
        val step = LatexmkCompileStepOptions().apply {
            latexmkCompileMode = LatexmkCompileMode.XELATEX_PDF
        }

        assertEquals("Compile with latexmk (XeLaTeX (PDF))", step.displayName())
    }

    fun testLatexmkDisplayNameUsesCustomEngineCommandWhenConfigured() {
        val step = LatexmkCompileStepOptions().apply {
            latexmkCompileMode = LatexmkCompileMode.CUSTOM
            latexmkCustomEngineCommand = "uplatex"
        }

        assertEquals("Compile with latexmk (uplatex)", step.displayName())
    }

    fun testPdfViewerDisplayNameUsesKnownViewerDisplayName() {
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = EvinceViewer.name
        }

        assertEquals("Open with Evince", step.displayName())
    }

    fun testPdfViewerDisplayNameUsesCustomViewerLabel() {
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = CustomPdfViewer.name
        }

        assertEquals("Open with Custom viewer", step.displayName())
    }

    fun testPdfViewerDisplayNameUsesCustomViewerLabelWhenCommandConfigured() {
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = NoViewer.name
            customViewerCommand = "open {pdf}"
        }

        assertEquals("Open with Custom viewer", step.displayName())
    }

    fun testPdfViewerDisplayNameFallsBackToConfiguredName() {
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = "my-viewer"
        }

        assertEquals("Open with my-viewer", step.displayName())
    }

    fun testPdfViewerDisplayNameFallsBackWhenViewerNameBlank() {
        val step = PdfViewerStepOptions().apply {
            pdfViewerName = "   "
        }

        assertTrue(step.displayName().startsWith("Open with "))
        assertTrue(step.displayName().length > "Open with ".length)
    }
}
