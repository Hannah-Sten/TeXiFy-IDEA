package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMissingDocumentEnvironmentInspectionTest : TexifyInspectionTestBase(LatexMissingDocumentEnvironmentInspection()) {

    fun `test missing document environment`() = testHighlighting(
        """
        <error descr="Document doesn't contain a document environment.">\documentclass{article}</error>
        """.trimIndent()
    )

    fun `test 'wrong' environment`() = testHighlighting(
        """
        <error descr="Document doesn't contain a document environment.">
        \documentclass{article}
        
        \begin{center}
            hallo
        \end{center}
        </error>
        """.trimIndent()
    )

    fun `test no warning when document environment is present`() = testHighlighting(
        """
        \documentclass{article}
        
        \begin{document}
            hallo
        \end{document}
        """.trimIndent()
    )

    fun `test no warning on sty file`() {
        myFixture.configureByText(
            "package.sty",
            """
            \ProvidesPackage{package}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() = testQuickFix(
        """\documentclass{article}""",
        """
        \documentclass{article}
        \begin{document}
        
        \end{document}
        """.trimIndent()
    )
}