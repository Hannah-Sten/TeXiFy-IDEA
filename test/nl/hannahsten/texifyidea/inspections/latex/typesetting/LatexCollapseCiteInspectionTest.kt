package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexCollapseCiteInspectionTest : TexifyInspectionTestBase(LatexCollapseCiteInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            <warning descr="Citations can be collapsed">\cite{knuth1990}</warning><warning descr="Citations can be collapsed">\cite{goossens1993}</warning>
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\citet}[1]{\citeauthor{#1} \shortcite{#1}}
            \begin{document}
                \citet{X}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test warning non breaking space`() {
        testHighlighting("<warning>\\cite{a}</warning>~<warning>\\cite{b}</warning>")
    }

    fun `test no warning when both arguments are optional`() {
        testHighlighting("\\cite[p. 1]{book1}\\cite[aardappel]{Groente}")
    }

    fun `test no warning when one argument is optional`() {
        testHighlighting("\\cite{book1}\\cite[aardappel]{Groente}")
    }

    fun `test warning for all cites without optional arguments`() {
        testHighlighting("<warning>\\cite{book1}</warning>\\cite[aardappel]{Groente}<warning>\\cite{Doei}</warning>")
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \cite{knuth1990}\cite{goossens1993}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            \cite{knuth1990,goossens1993}
            """.trimIndent()
        )
    }

    fun `test quick fix with some optional parameters, replace first`() {
        testQuickFix(
            """\ci<caret>te{a}\cite[b]{c}\cite{d}\cite[e]{f}""",
            """\cite{a,d}\cite[b]{c}\cite[e]{f}""",
            2
        )
    }

    fun `test quick fix with some optional parameters, replace second`() {
        testQuickFix(
            """\cite{a}\cite[b]{c}\cit<caret>e{d}\cite[e]{f}""",
            """\cite[b]{c}\cite{a,d}\cite[e]{f}""",
            2
        )
    }
}