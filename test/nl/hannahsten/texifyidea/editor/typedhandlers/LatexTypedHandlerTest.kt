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

    fun testLeftRight() {
        myFixture.configureByText(
            LatexFileType,
            """
            \[
                \left(
                    a+b<caret>
                    \xi
                \right)
            \]
            """.trimIndent()
        )
        myFixture.type("\n+c")
        myFixture.checkResult(
            """
            \[
                \left(
                    a+b
                    +c<caret>
                    \xi
                \right)
            \]
            """.trimIndent()
        )
    }

    fun testBracesCompletion() {
        myFixture.configureByText(LatexFileType, """\mycommand<caret>""")
        myFixture.type("{")
        myFixture.checkResult("""\mycommand{<caret>}""")
    }

    fun testStartOfLineBracesCompletion() {
        myFixture.configureByText(LatexFileType, """<caret>""")
        myFixture.type("{")
        myFixture.checkResult("""{<caret>}""")
    }

    fun testStartOfLineEscapedBracesCompletion() {
        myFixture.configureByText(LatexFileType, """<caret>""")
        myFixture.type("\\{")
        myFixture.checkResult("""\{<caret>\}""")
    }

    fun testEscapedBracesCompletion() {
        myFixture.configureByText(LatexFileType, """Hello World <caret>""")
        myFixture.type("\\{")
        myFixture.checkResult("""Hello World \{<caret>\}""")
    }

    fun testCorrectPairedParenthesis() {
        myFixture.configureByText(LatexFileType, """$\left<caret>$""")
        myFixture.type('(')
        myFixture.checkResult("""$\left(<caret>\right)$""")
    }

    fun testCorrectPairedExistingParenthesis() {
        myFixture.configureByText(LatexFileType, """$\left<caret>)$""")
        myFixture.type('(')
        myFixture.checkResult("""$\left(<caret>\right)$""")
    }

    fun testCorrectPairedExistingRightParenthesis() {
        myFixture.configureByText(LatexFileType, """$\left<caret>\right)$""")
        myFixture.type('(')
        myFixture.checkResult("""$\left(<caret>\right)$""")
    }

    fun testCorrectPairedBraces() {
        myFixture.configureByText(LatexFileType, """$\left<caret>$""")
        myFixture.type('{')
        myFixture.checkResult("""$\left{<caret>\right}$""")
    }

    fun testCorrectPairedBrackets() {
        myFixture.configureByText(LatexFileType, """$\left<caret>$""")
        myFixture.type('[')
        myFixture.checkResult("""$\left[<caret>\right]$""")
    }
}