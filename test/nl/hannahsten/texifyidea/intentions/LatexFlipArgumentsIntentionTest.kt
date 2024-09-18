package nl.hannahsten.texifyidea.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexFlipArgumentsIntentionTest : BasePlatformTestCase() {

    fun testInMathEnvironment() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                $\frac{a<caret>}{b}$
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Swap the two arguments of a command" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                $\frac{b<caret>}{a}$
            \end{document}
            """.trimIndent()
        )
    }

    fun testOnFunctionTOken() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                $\fr<caret>ac{a}{b}$
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first { i -> i.text == "Swap the two arguments of a command" }.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                $\fr<caret>ac{b}{a}$
            \end{document}
            """.trimIndent()
        )
    }
}