package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexQuoteInspectionTest : TexifyInspectionTestBase(LatexQuoteInspection()) {

    fun `test ascii double quotes triggers warning in normal text`() {
        val original = """Lorem ipsum "dolor" sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum ``dolor'' sit amet, consectetur adipiscing elit."""
        val warning =
            """Lorem ipsum <warning descr="\" is not a valid set of LaTex quotes">"</warning>dolor<warning descr="\" is not a valid set of LaTex quotes">"</warning> sit amet, consectetur adipiscing elit."""

        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
//        testQuickFix(original, fixed, numberOfFixes = 2)
    }

    fun `test ascii single quotes triggers warning in normal text`() {
        val original = """Lorem ipsum 'dolor' sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum `dolor' sit amet, consectetur adipiscing elit."""
        val warning =
            """Lorem ipsum <warning descr="Closing quote without opening quote">'</warning>dolor<warning descr="Closing quote without opening quote">'</warning> sit amet, consectetur adipiscing elit."""
        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
//        testQuickFix(original, fixed, numberOfFixes = 1)
    }

    fun `test two sets of ascii double quotes triggers two warnings`() {
        val original = """Lorem ipsum 'dolor' sit amet, "consectetur" adipiscing elit."""
        val fixed = """Lorem ipsum `dolor' sit amet, ``consectetur'' adipiscing elit."""
        val warning =
            """Lorem ipsum <warning descr="Closing quote without opening quote">'</warning>dolor<warning descr="Closing quote without opening quote">'</warning> sit amet, <warning descr="\" is not a valid set of LaTex quotes">"</warning>consectetur<warning descr="\" is not a valid set of LaTex quotes">"</warning> adipiscing elit."""
        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
//        testQuickFixAll(original, fixed, numberOfFixes = 2, quickFixName = "Incorrect quotation")
    }

    fun `test multiple quotes one line`() {
        val original = """Think about `procedure', `second', day and also atmospheres."""
        myFixture.configureByText(LatexFileType, original)
        myFixture.checkHighlighting(true, false, false, false)
    }


    fun `test imperial measurements`() {
        val original = """This is a length of $2'11''$ in the imperial measurement system"""
        myFixture.configureByText(
            LatexFileType, original
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test csquotes`() {
        val original = """
            \usepackage{csquotes}
            \MakeOuterQuote{"}
            Specifying of "algorithms"
            """.trimIndent()
        myFixture.configureByText(
            LatexFileType, original
        )
        myFixture.checkHighlighting(true, false, false, false)
    }
}
