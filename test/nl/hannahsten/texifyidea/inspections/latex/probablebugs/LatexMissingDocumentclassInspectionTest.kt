package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMissingDocumentclassInspectionTest : TexifyInspectionTestBase(LatexMissingDocumentclassInspection()) {

    fun `test no warning`() = testHighlighting("""\documentclass{article}""")

    fun `test no warning in sty file`() {
        myFixture.configureByText("package.sty", """\newcommand{\help}{help}""")
        myFixture.checkHighlighting()
    }

    fun `test warning`() = testHighlighting("""<error descr="Document doesn't contain a \documentclass command.">\usepackage{amsmath}</error>""")

    fun `test quick fix`() = testQuickFix(
            before = """
                \usepackage{amsmath}
            """.trimIndent(),
            after = """
                \documentclass{article}
                \usepackage{amsmath}
            """.trimIndent()
    )
}