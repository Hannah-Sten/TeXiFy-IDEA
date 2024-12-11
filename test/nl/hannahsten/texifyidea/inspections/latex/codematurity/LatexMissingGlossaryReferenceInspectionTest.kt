package nl.hannahsten.texifyidea.inspections.latex.codematurity

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMissingGlossaryReferenceInspectionTest : TexifyInspectionTestBase(LatexMissingGlossaryReferenceInspection()) {

    fun testMissingReference() {
        myFixture.configureByText(LatexFileType, """\newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} <warning descr="Missing glossary reference">sample</warning>""")
        myFixture.checkHighlighting()
    }

    fun testAddGls() {
        testQuickFix(
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} sample
            """.trimIndent(),
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} \gls{sample}
            """.trimIndent()
        )
    }
}