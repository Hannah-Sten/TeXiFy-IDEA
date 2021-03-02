package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexNonMatchingIfInspectionTest : TexifyInspectionTestBase(LatexNonMatchingIfInspection()) {

    fun `test if closed`() = testHighlighting(
        """
        \if
        \fi
        """.trimIndent()
    )

    fun `test if not closed`() = testHighlighting("<warning descr=\"If statement should probably be closed with \\fi\">\\if</warning>")

    fun `test fi not opened`() = testHighlighting("<warning descr=\"No matching \\if-command found\">\\fi</warning>")

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
        <warning descr="If statement should probably be closed with \fi">\ifpaper</warning>
        """.trimIndent()
    )

    fun `test newif not opened`() = testHighlighting(
        """
        \newif\ifpaper
        <warning descr="No matching \if-command found">\fi</warning>
        """.trimIndent()
    )
}