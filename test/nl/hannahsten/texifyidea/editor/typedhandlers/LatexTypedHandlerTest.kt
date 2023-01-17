package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexTypedHandlerTest : BasePlatformTestCase() {

    fun testInlineMath() {
        myFixture.configureByText(LatexFileType, "")
        myFixture.type("$")
        myFixture.checkResult("$$")
    }

    fun testInlineMath2() {
        myFixture.configureByText(LatexFileType, "$\\xi<caret>$")
        myFixture.type("$")
        myFixture.checkResult("$\\xi$<caret>")
    }

    fun testRobustInlineMath() {
        myFixture.configureByText(LatexFileType, "")
        myFixture.type("\\(")
        myFixture.checkResult("\\(\\)")
    }

    fun testVerbatim() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{verbatim}
                <caret>
            \end{verbatim}
            """.trimIndent()
        )
        myFixture.type("$")
        myFixture.checkResult(
            """
            \begin{verbatim}
                $<caret>
            \end{verbatim}
            """.trimIndent()
        )
    }

    fun testDisplayMath() {
        myFixture.configureByText(LatexFileType, "")
        myFixture.type("\\[")
        myFixture.checkResult("\\[\\]")
    }

    fun testDisplayMath2() {
        myFixture.configureByText(LatexFileType, "\\[<caret>\\]")
        myFixture.type("\n")
        myFixture.checkResult(
            """
            \[
                <caret>
            \]
            """.trimIndent()
        )
    }
}