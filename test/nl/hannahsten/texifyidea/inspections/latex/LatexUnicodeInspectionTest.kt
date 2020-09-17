package nl.hannahsten.texifyidea.inspections.latex

import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.settings.TexifyProjectSettings

class LatexUnicodeInspectionTest : TexifyInspectionTestBase(LatexUnicodeInspection()) {

    fun `test illegal unicode character`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, "<error descr=\"Unsupported non-ASCII character\">î</error>")
        myFixture.checkHighlighting()
    }

    fun `test with compiler compatibility`() {
        setUnicodeSupport()

        myFixture.configureByText(LatexFileType, "î")
        myFixture.checkHighlighting()
    }

    /**
     * Set the Texify Project Settings and the Latex Distribution in a way to ensure either unicode support, or no unicode support.
     */
    private fun setUnicodeSupport(enabled: Boolean = true) {
        if (enabled) {
            // Unicode is always supported in lualatex.
            TexifyProjectSettings.getInstance(myFixture.project).compilerCompatibility = LatexCompiler.LUALATEX
        }
        else {
            // Unicode is not supported on pdflatex on texlive <= 2017.
            TexifyProjectSettings.getInstance(myFixture.project).compilerCompatibility = LatexCompiler.PDFLATEX
            mockkObject(LatexDistribution)
            every { LatexDistribution.texliveVersion } returns 2017
        }
    }
}