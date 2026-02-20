package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class LatexMathFunctionTextInspectionTest : TexifyInspectionTestBase(LatexMathFunctionTextInspection()) {

    fun testWarning() {
        MATH_FUNCTIONS.take(2).forEach {
            myFixture.configureByText(
                LatexFileType,
                """
                        $<warning descr="Use math function instead of \text">\text{$it}(3, 4)</warning>$
                        \(<warning descr="Use math function instead of \text">\text{$it   }(12)</warning>\)
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
        MATH_FUNCTIONS.take(2).forEach {
            myFixture.configureByText(
                LatexFileType,
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

        myFixture.configureByText(
            LatexFileType,
            """Just some random min text max or \[max(3,4)\] or even \[ \text{nomath} \]"""
        )
        myFixture.checkHighlighting()
    }

    fun testQuickFix() {
        MATH_FUNCTIONS.take(2).forEach {
            // it is slow to test all of them
            myFixture.configureByText(
                LatexFileType,
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

        private val MATH_FUNCTIONS = CommandMagic.mathOperators.map { it.name }
    }
}