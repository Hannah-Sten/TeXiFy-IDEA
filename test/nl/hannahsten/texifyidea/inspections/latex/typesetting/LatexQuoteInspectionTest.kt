package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexQuoteInspectionTest : TexifyInspectionTestBase(LatexQuoteInspection()) {

    fun `test ascii double quotes triggers warning in normal text`() {
        val original = """Lorem ipsum "dolor" sit amet, consectetur adipiscing elit."""
        val halfFixed = """Lorem ipsum ``dolor" sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum ``dolor'' sit amet, consectetur adipiscing elit."""
        val warning =
            """Lorem ipsum <warning descr="\" is not a valid set of LaTex quotes">"</warning>dolor<warning descr="\" is not a valid set of LaTex quotes">"</warning> sit amet, consectetur adipiscing elit."""

        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)

        testNamedQuickFix(original, halfFixed, "Replace with a LaTeX opening double quote", 10)
        testNamedQuickFix(halfFixed, fixed, "Replace with a LaTeX closing double quote", 5)
    }

    fun `test ascii single quotes triggers warning in normal text`() {
        val original = """Lorem ipsum 'dolor' sit amet, consectetur adipiscing elit."""
        val fixed = """Lorem ipsum `dolor' sit amet, consectetur adipiscing elit."""
        val warning =
            """Lorem ipsum <warning descr="Closing quote without opening quote">'</warning>dolor' sit amet, consectetur adipiscing elit."""
        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
        testNamedQuickFix(original, fixed, "Replace with a LaTeX opening single quote", 5)
    }

    fun `test two sets of ascii double quotes triggers two warnings`() {
        val warning =
            """Lorem ipsum <warning descr="Closing quote without opening quote">'</warning>dolor' sit amet, <warning descr="\" is not a valid set of LaTex quotes">"</warning>consectetur<warning descr="\" is not a valid set of LaTex quotes">"</warning> adipiscing elit."""
        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test multiple quotes one line`() {
        val original = """Think about `procedure', `second', day and also atmospheres."""
        myFixture.configureByText(LatexFileType, original)
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test sentence with math in it`() {
        val original = """It is joyfully not outstanding to monitor \(\Xi\) 'attentions'."""
        val fixedWithLatexQuote = """It is joyfully not outstanding to monitor \(\Xi\) `attentions'."""
        val partiallyFixedWithMathMode = """It is joyfully not outstanding to monitor \(\Xi\) \('\)attentions'."""
        val warning =
            """It is joyfully not outstanding to monitor \(\Xi\) <warning descr="Closing quote without opening quote">'</warning>attentions'."""
        myFixture.configureByText(
            LatexFileType,
            warning
        )
        myFixture.checkHighlighting(true, false, false, false)
        testNamedQuickFix(original, fixedWithLatexQuote, "Replace with a LaTeX opening single quote", 5)
        testNamedQuickFix(
            original,
            partiallyFixedWithMathMode,
            "Convert to inline maths environment, for typesetting feet, inches or other mathematical punctuation.",
            5
        )
    }

    fun `test imperial measurements in math mode are ignored`() {
        val original = """This is a length of $2'11''$ in the imperial measurement system"""
        myFixture.configureByText(
            LatexFileType, original
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test imperial measurements quickfix`() {
        val original = """This is a length of 2'11'' in the imperial measurement system"""
        val warning =
            """This is a length of 2'11<warning descr="Closing quote without opening quote">''</warning> in the imperial measurement system"""
        val fixed = """This is a length of \(2'11''\) in the imperial measurement system"""
        myFixture.configureByText(LatexFileType, warning)
        myFixture.checkHighlighting(true, false, false, false)

        testNamedQuickFix(
            original,
            fixed,
            "Convert to inline maths environment, for typesetting feet, inches or other mathematical punctuation.",
            5
        )
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

    fun `test apostrophe`() {
        val original = "Group III: Axiom of Parallels (Euclid's axiom)"
        myFixture.configureByText(LatexFileType, original)
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test apostrophe in quotes`() {
        val original = "I said ``Group III: Axiom of Parallels (Euclid's axiom)''"
        myFixture.configureByText(LatexFileType, original)
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test final apostrophe`() {
        val original = "the humans' planet"
        myFixture.configureByText(LatexFileType, original)
        myFixture.checkHighlighting(true, false, false, false)
    }
}
