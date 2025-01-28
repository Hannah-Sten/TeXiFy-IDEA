package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class OutsideMathLatexUnicodeInspectionTest : LatexUnicodeInspectionTest() {

    fun `test illegal unicode character`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, "<error descr=\"Unsupported non-ASCII character\">î</error>")
        myFixture.checkHighlighting()
    }

    fun `test support by loaded packages`() {
        setUnicodeSupport(false)

        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage[utf8]{inputenc}
            \usepackage[T1]{fontenc}
            î
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test legal unicode character with compiler compatibility`() {
        setUnicodeSupport()

        myFixture.configureByText(LatexFileType, "î")
        myFixture.checkHighlighting()
    }
}

class InsideMathLatexUnicodeInspectionTest : LatexUnicodeInspectionTest() {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    fun `test without support`() {
        setUnicodeSupport(false)

        myFixture.configureByText(LatexFileType, "\$<error descr=\"Unsupported non-ASCII character\">î</error>\$")
        myFixture.checkHighlighting()
    }

    fun `test with loaded packages`() {
        setUnicodeSupport()

        myFixture.configureByText(
            LatexFileType,
            "\\usepackage[utf8]{inputenc}\n" +
                "            \\usepackage[T1]{fontenc}\n" +
                "\$<error descr=\"Unsupported non-ASCII character\">î</error>\$"
        )
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

        testNamedQuickFix(
            "\nî",
            """
            \usepackage[utf8]{inputenc}
            \usepackage[T1]{fontenc}
            î
            """.trimIndent(),
            "Include Unicode support packages", 2
        )
    }

    @Suppress("NonAsciiCharacters")
    fun `test escape unicode quick fix é`() {
        setUnicodeSupport(false)

        testNamedQuickFix("é", "\\'e", "Escape Unicode character", 2)
    }

    fun `test mu`() {
        setUnicodeSupport(false)

        testNamedQuickFix("$ µ$", "$ \\micro$", "Escape Unicode character", 1)
        testNamedQuickFix("$ μ$", "$ \\mu$", "Escape Unicode character", 1)
    }

    @Suppress("NonAsciiCharacters")
    fun `test escape unicode quick fix î`() {
        setUnicodeSupport(false)

        testNamedQuickFix("î", "\\^{\\i}", "Escape Unicode character", 2)
    }

    fun `test escape unicode quick fix regular command`() {
        setUnicodeSupport(false)

        testNamedQuickFix("å", "\\aa", "Escape Unicode character", 2)
    }

    fun `test escape unicode quick fix known math command`() {
        setUnicodeSupport(false)

        testNamedQuickFix("\$α\$", "\$\\alpha\$", "Escape Unicode character", 1)
    }

    fun `test escape unicode quick fix math command`() {
        setUnicodeSupport(false)

        // ℂ cannot be converted.
        testNamedQuickFix("\$ℂ\$", "\$ℂ\$", "Escape Unicode character", 1)
    }
}

abstract class LatexUnicodeInspectionTest : TexifyInspectionTestBase(LatexUnicodeInspection()) {

    fun setUnicodeSupport(enabled: Boolean = true) = nl.hannahsten.texifyidea.testutils.setUnicodeSupport(myFixture.project, enabled)
}