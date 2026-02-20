package nl.hannahsten.texifyidea.highlighting

import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

/**
 * Highlighting is done both in [LatexSyntaxHighlighter] and [LatexAnnotator].
 * Here, we only test highlighting done by [LatexSyntaxHighlighter], for single tokens.
 * Also see https://plugins.jetbrains.com/docs/intellij/testing-highlighting.html#syntax-highlighting
 */
class LatexHighlightingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "test/resources/highlighting"

    fun testCommandsAndComments() {
        val testFile = myFixture.configureByFile("CommandsAndComments.tex")
        EditorTestUtil.testFileSyntaxHighlighting(testFile, "$testDataPath/CommandsAndCommentsOutput.tex", true)
    }

    fun testInlineMath() {
        val testFile = myFixture.configureByFile("InlineMath.tex")
        EditorTestUtil.testFileSyntaxHighlighting(testFile, "$testDataPath/InlineMathOutput.tex", true)
    }

    fun testLabelDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \providecommand{\Example}[1]{#1\label{<info descr="null" textAttributesKey="LATEX_LABEL_DEFINITION">#1</info>}}
            \Example{\emph{something} text} % Should also have highlighting here, but it doesn't show up in this test
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting(true, true, true, true)
    }
}