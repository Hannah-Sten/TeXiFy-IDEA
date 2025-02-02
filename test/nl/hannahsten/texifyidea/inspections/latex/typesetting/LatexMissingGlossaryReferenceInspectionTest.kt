package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMissingGlossaryReferenceInspectionTest : TexifyInspectionTestBase(LatexMissingGlossaryReferenceInspection()) {

    fun testMissingReference() {
        myFixture.configureByText(LatexFileType, """\newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} \Glslink{sample} <warning descr="Missing glossary reference">sample</warning>""")
        myFixture.checkHighlighting()
    }

    fun testAddGls() {
        testQuickFix(
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} sample text
            """.trimIndent(),
            """
                \newglossaryentry{sample}{name={sample},description={an example}} \gls{sample} \gls{sample} text
            """.trimIndent()
        )
    }

    fun testNewCommand() {
        myFixture.configureByText(LatexFileType, """\newcommand{\mygls}{\newabbreviation{name}{\ensuremath{#1}}{long}} Some text""")
        myFixture.checkHighlighting()
    }
}