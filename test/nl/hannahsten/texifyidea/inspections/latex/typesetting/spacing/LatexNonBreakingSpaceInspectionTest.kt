package nl.hannahsten.texifyidea.inspections.latex.typesetting.spacing

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexNonBreakingSpaceInspectionTest : TexifyInspectionTestBase(LatexNonBreakingSpaceInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            Reference<warning descr="Reference without a non-breaking space"> </warning>\ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testWarningNewline() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference<warning descr="Reference without a non-breaking space">
                </warning>\ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningNewlineSentence() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference.
                \ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningNewLineSentenceEnd() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference. \ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testWarningAfterMathContext() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference ${"$"}math$<warning descr="Reference without a non-breaking space"> </warning>\ref{fig}.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningAfterEnvironment() {
        myFixture.configureByText(
            LatexFileType,
            """
                \begin{center}
                    Something centered.
                \end{center}
                \ref{blub} Starts a new sentence.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarningAfterComment() {
        myFixture.configureByText(
            LatexFileType,
            """
                % Hello World
                \ref{blub} This is science.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testWarningAfterTextModifier() {
        myFixture.configureByText(
            LatexFileType,
            """
                \textit{Hello}<warning descr="Reference without a non-breaking space"> </warning>\ref{blub} world.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
                Reference~\ref{fig}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test no warning when reference in required parameter`() {
        myFixture.configureByText(
            LatexFileType,
            """
            abc \dummy{Reference~\ref{fig}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickFix() {
        myFixture.configureByText(
            LatexFileType,
            """
            Reference \ref{fig}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            Reference~\ref{fig}
            """.trimIndent()
        )
    }
}