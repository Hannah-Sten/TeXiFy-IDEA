package nl.hannahsten.texifyidea.highlighting

import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

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