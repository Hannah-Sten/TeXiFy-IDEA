package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

/**
 * @author Hannah Schellekens
 */
class LatexMathFunctionInspectionTest : TexifyInspectionTestBase(LatexNonBreakingSpaceInspection()) {

    fun testWarning() {
        MATH_FUNCTIONS.forEach {
            myFixture.configureByText(LatexFileType,
                    """
                        $<warning descr="Use math function instead of \text">\text{$it}</warning>(3, 4)
                        \(<warning descr="Use math function instead of \text">\text{$it   }</warning>(12)\)
                        \[<warning descr="Use math function instead of \text">\text{  $it}</warning>   text\]
                        \begin{equation}
                            <warning descr="Use math function instead of \text">\text{  $it    }</warning>
                        \end{equation}
                    """.trimIndent()
            )
            myFixture.checkHighlighting()
        }
    }

    fun testNoWarning() {
        MATH_FUNCTIONS.forEach {
            myFixture.configureByText(LatexFileType,
                    """
                        \text{$it}
                        \text{$it   }
                        \text{  $it}
                        \begin{nonmathenv}
                            \text{  $it    }
                        \end{nonmathenv}
                    """.trimIndent()
            )
            myFixture.checkHighlighting()
        }

        myFixture.configureByText(LatexFileType,
                """Just some random min text max or \[max(3,4)\] or even \[ \text{nomath} \]"""
        )
        myFixture.checkHighlighting()
    }

    fun testQuickFix() {
        MATH_FUNCTIONS.forEach {
            myFixture.configureByText(LatexFileType,
                    """$ Test \text{$it } (3, 4) $ text max"""
            )

            val quickFixes = myFixture.getAllQuickFixes()
            assertEquals(1, quickFixes.size)
            writeCommand(myFixture.project) {
                quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
            }

            myFixture.checkResult(
                    """$ Test \$it (3, 4) $ text max"""
            )
        }
    }

    companion object {

        /**
         * Reference [Unofficial LaTeX2e reference manual](https://latexref.xyz/Math-functions.html)
         */
        private val MATH_FUNCTIONS = listOf(
                "arccos",
                "arcsin",
                "arctan",
                "arg",
                "bmod",
                "cos",
                "cosh",
                "cot",
                "coth",
                "csc",
                "deg",
                "det",
                "dim",
                "exp",
                "gcd",
                "hom",
                "inf",
                "ker",
                "lg",
                "lim",
                "liminf",
                "limsup",
                "ln",
                "log",
                "max",
                "min",
                "pmod",
                "Pr",
                "projlim",
                "sec",
                "sin",
                "sinh",
                "sup",
                "tan",
                "tanh",
        )
    }
}