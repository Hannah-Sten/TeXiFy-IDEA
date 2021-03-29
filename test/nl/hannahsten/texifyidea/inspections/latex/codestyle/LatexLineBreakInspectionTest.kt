package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexLineBreakInspectionTest : TexifyInspectionTestBase(LatexLineBreakInspection()) {

    fun testWarning() = testHighlighting(
        """
            Not this, b<weak_warning descr="Sentence does not start on a new line">ut. This starts a new line.</weak_warning>
This e<weak_warning descr="Sentence does not start on a new line">tc. is missing a normal space, but i.e. this etc.</weak_warning>\ is not.
            % not. in. comments
        """.trimIndent()
    )

    fun testNoWarning() = testHighlighting(
        """
            First sentence.
            Second sentence.
        """.trimIndent()
    )

    fun `test no warning in comment`() = testHighlighting(
        """
            This is an abbreviation (ABC). % commemt
            More text here.
        """.trimIndent()
    )

    fun `test no warning in math mode`() = testHighlighting("""\[ Why. would. you. do. this. \]""")

    fun `test no warning in magic comment on own line`() = testHighlighting("""%! suppress = CiteBeforePeriod""")

    fun `test no warning in magic comment on line with text`() = testHighlighting("""This is a sentence. %! suppress = CiteBeforePeriod""")

    fun `test quick fix`() = testQuickFix(
        before = """I end. a sentence.""",
        after = """
                I end.
                a sentence.
            """.trimIndent(),
        numberOfFixes = 2,
        selectedFix = 1
    )

    fun `test quick fix abbreviation`() = testQuickFix(
        before = """Ich sehe ihn bzw. seinen Schatten.""",
        after = """Ich sehe ihn bzw.\ seinen Schatten.""",
        numberOfFixes = 2,
        selectedFix = 2
    )

    fun `test quick fix non-abbreviation`() = testQuickFix(
        before = """This! cannot have been an abbreviation.""",
        after = """
            This!
            cannot have been an abbreviation.
        """.trimIndent(),
        numberOfFixes = 1,
    )
}