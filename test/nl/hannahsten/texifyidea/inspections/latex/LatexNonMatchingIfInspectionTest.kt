package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexNonMatchingIfInspectionTest : TexifyInspectionTestBase(LatexNonMatchingIfInspection()) {
    fun `test if closed`() = testHighlighting(
        """
        \if
        \fi
        """.trimIndent()
    )

    fun `test if not closed`() = testHighlighting("<error descr=\"If statement is not closed\">\\if</error>")

    fun `test fi not opened`() = testHighlighting("<error descr=\"No matching \\if-command found\">\\fi</error>")

    fun `test closed newif`() = testHighlighting(
        """
        \newif\ifpaper
        \ifpaper
        \fi
        """.trimIndent()
    )

    fun `test newif not closed`() = testHighlighting(
        """
        \newif\ifpaper
        <error descr="If statement is not closed">\ifpaper</error>
        """.trimIndent()
    )

    fun `test newfi not opened`() = testHighlighting(
        """
        \newif\ifpaper
        <error descr="No matching \if-command found">\fi</error>
        """.trimIndent()
    )
}