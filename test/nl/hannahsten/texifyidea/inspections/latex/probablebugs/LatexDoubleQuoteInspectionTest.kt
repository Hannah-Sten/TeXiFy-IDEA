package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexDoubleQuoteInspectionTest : TexifyInspectionTestBase(LatexDoubleQuoteInspection()) {

    fun `test ascii double quotes triggers warning in normal text`() {
        val original = """Lorem ipsum "dolor" sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum ``dolor'' sit amet, consectetur adipiscing elit."""
        val warning = """Lorem ipsum <warning descr="\"ASCII quotes\" were used instead of ``LaTeX quotes''">"dolor"</warning> sit amet, consectetur adipiscing elit."""

        myFixture.configureByText(
                LatexFileType,
                warning
        )
        myFixture.checkHighlighting(true, false, false, false)
        testQuickFix(original, fixed, numberOfFixes = 1)
    }

    fun `test ascii single quotes triggers warning in normal text`() {
        val original = """Lorem ipsum 'dolor' sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum `dolor' sit amet, consectetur adipiscing elit."""
        val warning = """Lorem ipsum <warning descr="\"ASCII quotes\" were used instead of ``LaTeX quotes''">'dolor'</warning> sit amet, consectetur adipiscing elit."""
        myFixture.configureByText(
                LatexFileType,
                warning
        )
        myFixture.checkHighlighting(true, false, false, false)
        testQuickFix(original, fixed, numberOfFixes = 1)
    }

    fun `test two sets of ascii double quotes triggers two warnings`() {
        val original = """Lorem ipsum 'dolor' sit amet, "consectetur" adipiscing elit."""
        val fixed = """Lorem ipsum `dolor' sit amet, ``consectetur'' adipiscing elit."""
        val warning = """Lorem ipsum <warning descr="\"ASCII quotes\" were used instead of ``LaTeX quotes''">'dolor'</warning> sit amet, <warning descr="\"ASCII quotes\" were used instead of ``LaTeX quotes''">"consectetur"</warning> adipiscing elit."""
        myFixture.configureByText(
                LatexFileType,
                warning
        )
        myFixture.checkHighlighting(true, false, false, false)
        testQuickFixAll(original, fixed, numberOfFixes = 2, quickFixName = "Incorrect quotation")
    }
}
