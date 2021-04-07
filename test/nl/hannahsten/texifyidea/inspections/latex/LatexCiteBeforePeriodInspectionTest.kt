package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.typesetting.LatexCiteBeforePeriodInspection
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexCiteBeforePeriodInspectionTest : TexifyInspectionTestBase(LatexCiteBeforePeriodInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            I cite.~<weak_warning descr="\cite is placed after interpunction">\cite{</weak_warning>knuth1990}
            However, e.g.~\cite{knuth1990} does not end a sentence, neither does Goossens et al.~\cite{goossens}.
            This e.g. is.~<weak_warning descr="\cite is placed after interpunction">\cite{</weak_warning>knuth1990}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testQuickfix() {
        myFixture.configureByText(
            LatexFileType,
            """
            I cite.~\cite{knuth1990}
            """.trimIndent()
        )

        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals(1, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(
            """
            I cite~\cite{knuth1990}.
            """.trimIndent()
        )
    }
}