package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

internal class LatexSuspiciousSectionFormattingInspectionTest : TexifyInspectionTestBase(LatexSuspiciousSectionFormattingInspection()) {

    fun `test ~ warning`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section{You should not use<warning descr=\"Suspicious formatting in \\section\">~</warning>in the title of a section}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test ~ warning for short section`() {
        myFixture.configureByText(
                LatexFileType,
                "\\section{a<warning descr=\"Suspicious formatting in \\section\">~</warning>b}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test backslash warning`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section{You should not use<warning descr=\"Suspicious formatting in \\section\">\\\\</warning>in the title of a section}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test multiple warnings in one section`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section{You should not use<warning descr=\"Suspicious formatting in \\section\">~</warning>in the title<warning descr=\"Suspicious formatting in \\section\">~</warning>of a section}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test no warning for ~ when optional argument is present`() {
        myFixture.configureByText(
                LatexFileType,
                "\\section[Table of contents long title]{Title with explicit~formatting}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test no warning for backslash when optional argument is present`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section[Table of contents long title]{Title with explicit \\\\ formatting}"
        )
        myFixture.checkHighlighting(true, false, true, false)
    }

    fun `test simple quickfix`() {
        myFixture.configureByText(
            LatexFileType,
            "\\section{You should not use~in the title}"
        )
        testQuickFix("\\section{You should not use~in the title}", "\\section[You should not use in the title]{You should not use~in the title}")
        myFixture.checkHighlighting(true, false, true, false)
    }
}
