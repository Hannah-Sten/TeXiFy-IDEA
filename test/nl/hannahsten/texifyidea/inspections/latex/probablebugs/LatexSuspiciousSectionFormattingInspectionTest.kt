package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

internal class LatexSuspiciousSectionFormattingInspectionTest : TexifyInspectionTestBase(LatexSuspiciousSectionFormattingInspection()) {

    fun `test ~ warning`() {
        myFixture.configureByText(
            LatexFileType,
            "<warning descr=\"Suspicious formatting in \\section\">\\section{You should not use~in the title of a section}</warning>"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

//    fun `test ~ warning`() {
//        myFixture.configureByText(
//            LatexFileType,
//            "\\section{You should not use<warning descr=\"Suspicious formatting in \\section\">~</warning>in the title of a section}"
//        )
//        myFixture.checkHighlighting(true, false, true, false)
//    }

    fun `test no warning when optional argument is present`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section[Table of contents long title]{Title with explicit \\\\ formatting}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }
}
