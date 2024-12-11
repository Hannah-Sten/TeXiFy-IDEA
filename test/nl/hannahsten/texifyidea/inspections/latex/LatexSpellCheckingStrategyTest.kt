package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.spellchecker.inspections.SpellCheckingInspection
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexSpellCheckingStrategyTest : TexifyInspectionTestBase(SpellCheckingInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
                \usepackage{aligned-overset}
                \section{Aligned <TYPO descr="Typo: In word 'overset'">overset</TYPO>}
                Aligned <TYPO descr="Typo: In word 'overset'">overset</TYPO>
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testOptionalParameterTypo() {
        myFixture.configureByText(
            LatexFileType,
            """
               \begin{description}
                  \item[<TYPO descr="Typo: In word 'Tpyo'">Tpyo</TYPO>]
                   <TYPO descr="Typo: In word 'Tpyo'">Tpyo</TYPO>
               \end{description}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}