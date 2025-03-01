package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexNonMatchingEnvironmentInspectionTest : TexifyInspectionTestBase(LatexNonMatchingEnvironmentInspection()) {

    fun `test no warning`() = testHighlighting(
        """
        \begin{center}
            bla
        \end{center}
        """.trimIndent()
    )

    fun `test warnings`() = testHighlighting(
        """
        <error descr="DefaultEnvironment name does not match with the name in \end.">\begin{center}</error>
            bla
        <error descr="DefaultEnvironment name does not match with the name in \begin.">\end{left}</error>
        <error descr="DefaultEnvironment name does not match with the name in \end.">\begin{center}</error>
            bla
        <error descr="DefaultEnvironment name does not match with the name in \begin.">\end{}</error>
        """.trimIndent()
    )

    fun `test begin quick fix`() = testNamedQuickFix(
        """
        \begin{center}
            bla
        \end{left}
        """.trimIndent(),
        """
        \begin{left}
            bla
        \end{left}
        """.trimIndent(),
        quickFixName = "Change \\begin environment to 'left'",
        numberOfFixes = 2
    )

    fun `test end quick fix`() = testNamedQuickFix(
        """
        \begin{center}
            bla
        \end{left}
        """.trimIndent(),
        """
        \begin{center}
            bla
        \end{center}
        """.trimIndent(),
        quickFixName = "Change \\end environment to 'center'",
        numberOfFixes = 2
    )
}