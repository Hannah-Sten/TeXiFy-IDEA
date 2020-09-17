package nl.hannahsten.texifyidea.inspections.latex

import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.settings.TexifyProjectSettings

class OutsideMathLatexUnicodeInspectionTest : LatexUnicodeInspectionTest() {
    fun `test illegal unicode character`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, "<error descr=\"Unsupported non-ASCII character\">î</error>")
        myFixture.checkHighlighting()
    }

    fun `test support by loaded packages`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, """
            \usepackage[utf8]{inputenc}
            \usepackage[T1]{fontenc}
            î
""".trimIndent())
        myFixture.checkHighlighting()
    }

    fun `test legal unicode character with compiler compatibility`() {
        setUnicodeSupport()

        myFixture.configureByText(LatexFileType, "î")
        myFixture.checkHighlighting()
    }
}

class InsideMathLatexUnicodeInspectionTest : LatexUnicodeInspectionTest() {
    fun `test without support`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, "\$<error descr=\"Unsupported non-ASCII character\">î</error>\$")
        myFixture.checkHighlighting()
    }

    fun `test with loaded packages`() {
        setUnicodeSupport()

        myFixture.configureByText(LatexFileType, "\\usepackage[utf8]{inputenc}\n" +
                "            \\usepackage[T1]{fontenc}\n" +
                "\$<error descr=\"Unsupported non-ASCII character\">î</error>\$")
        myFixture.checkHighlighting()
    }

    fun `test with compiler compatibility`() {
        setUnicodeSupport()

        myFixture.configureByText(LatexFileType, "\$<error descr=\"Unsupported non-ASCII character\">î</error>\$")
        myFixture.checkHighlighting()
    }
}

class LatexUnicodeInspectionQuickFix : LatexUnicodeInspectionTest() {
    fun `test include packages quick fix`() {
        setUnicodeSupport(false)

        testNamedQuickFix("\nî", """
            \usepackage[utf8]{inputenc}
            \usepackage[T1]{fontenc}
            î""".trimIndent(),
                "Include Unicode support packages", 3)
    }

    @Suppress("NonAsciiCharacters")
    fun `test escape unicode quick fix é`() {
        setUnicodeSupport(false)

        testNamedQuickFix("é", "\\'e", "Escape Unicode character", 3)
    }

    @Suppress("NonAsciiCharacters")
    fun `test escape unicode quick fix î`() {
        setUnicodeSupport(false)

        testNamedQuickFix("î", "\\^{\\i}", "Escape Unicode character", 3)
    }

    fun `test escape unicode quick fix regular command`() {
        setUnicodeSupport(false)

        testNamedQuickFix("å", "\\aa", "Escape Unicode character", 3)
    }

    fun `test escape unicode quick fix known math command`() {
        setUnicodeSupport(false)

        testNamedQuickFix("\$α\$", "\$\\alpha\$", "Escape Unicode character", 2)
    }

    fun `test escape unicode quick fix math command`() {
        setUnicodeSupport(false)

        // ℂ cannot be converted.
        testNamedQuickFix("\$ℂ\$", "\$ℂ\$", "Escape Unicode character", 2)
    }
}

open class LatexUnicodeInspectionTest : TexifyInspectionTestBase(LatexUnicodeInspection()) {

    /**
     * Set the TeXiFy Project Settings and the Latex Distribution in a way to ensure either unicode support, or no unicode support.
     */
    fun setUnicodeSupport(enabled: Boolean = true) {
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