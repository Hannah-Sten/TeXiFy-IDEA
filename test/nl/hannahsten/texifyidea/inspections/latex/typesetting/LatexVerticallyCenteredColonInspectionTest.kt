package nl.hannahsten.texifyidea.inspections.latex.typesetting

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexVerticallyCenteredColonInspectionTest : TexifyInspectionTestBase(LatexVerticallyCenteredColonInspection()) {

    fun `test warning`() = testHighlighting("""\[ a <warning descr="Colon is vertically uncentered">:=</warning> b \]""")

    fun `test no warning with setting enabled`() = testHighlighting("""
            \mathtoolsset{centercolon=true}
            \[ a := b \]
        """.trimIndent())

    fun `test warning lookahead`() = testHighlighting("""\[ a <warning descr="Colon is vertically uncentered">:\approx</warning>\infty \]""")

    fun `test no warning lookahead`() = testHighlighting("""\[ a :\approximate b \]""")

    fun `test warning spaces`() = testHighlighting("""\[ a <warning descr="Colon is vertically uncentered">: :  =</warning> b \]""")

    fun `test no warning newline`() = testHighlighting("""
            \[
                a : 
                 = b
            \]
        """.trimIndent())

    fun `test no warning outside math mode`() = testHighlighting("""a := b \[ a : b \]""")

    fun `test quick fix without inserting space`() = testQuickFix(
        before = """\[ a :=\infty \]""",
        after = """\usepackage{mathtools}\[ a \coloneqq\infty \]"""
    )

    fun `test quick fix with inserting space`() = testQuickFix(
        before = """\[ a :=b \]""",
        after = """\usepackage{mathtools}\[ a \coloneqq b \]"""
    )
}
