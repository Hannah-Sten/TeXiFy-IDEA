package nl.hannahsten.texifyidea.inspections.latex.redundancy

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.setUnicodeSupport

class LatexRedundantEscapeInspectionTest : TexifyInspectionTestBase(LatexRedundantEscapeInspection()) {

    fun `test no warning when no unicode support`() {
        setUnicodeSupport(myFixture.project, false)

        myFixture.configureByText(LatexFileType, "\\'e")
        myFixture.checkHighlighting()
    }

    fun `test warning with unicode support`() {
        setUnicodeSupport(myFixture.project, true)

        myFixture.configureByText(LatexFileType, "<weak_warning descr=\"Redundant diacritic escape\">\\'</weak_warning>e")
        myFixture.checkHighlighting()
    }

    fun `test untypable diacritic`() {
        setUnicodeSupport(myFixture.project, true)

        myFixture.configureByText(LatexFileType, "\\H{a}")
        myFixture.checkHighlighting()
    }

    fun `test no warning in math mode`() {
        setUnicodeSupport(myFixture.project, true)

        myFixture.configureByText(LatexFileType, "$\\alpha$")
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() {
        setUnicodeSupport(myFixture.project, true)

        testQuickFix("\\'e", "é")
    }

    fun `test quick fix with required parameter`() {
        setUnicodeSupport(myFixture.project, true)

        testQuickFix("\\'{e}", "é")
    }

    fun `test quick fix with too long base`() {
        setUnicodeSupport(myFixture.project, true)

        testQuickFix("\\'{ee}", "ée")
    }

    fun `test quick fix with dotless i`() {
        setUnicodeSupport(myFixture.project, true)

        testQuickFix("\\^{\\i}", "î")
    }
}