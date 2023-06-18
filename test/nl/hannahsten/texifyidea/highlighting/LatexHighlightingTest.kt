package nl.hannahsten.texifyidea.highlighting

import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Highlighting is done both in [LatexSyntaxHighlighter] and [LatexAnnotator].
 * Here, we only test highlighting done by [LatexSyntaxHighlighter], for single tokens.
 * Also see https://plugins.jetbrains.com/docs/intellij/testing-highlighting.html#syntax-highlighting
 */
class LatexHighlightingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/highlighting"
    }

    fun testCommandsAndComments() {
        val testFile = myFixture.configureByFile("CommandsAndComments.tex")
        EditorTestUtil.testFileSyntaxHighlighting(testFile, "$testDataPath/CommandsAndCommentsOutput.tex", true)
    }

    fun testInlineMath() {
        val testFile = myFixture.configureByFile("InlineMath.tex")
        EditorTestUtil.testFileSyntaxHighlighting(testFile, "$testDataPath/InlineMathOutput.tex", true)
    }
}