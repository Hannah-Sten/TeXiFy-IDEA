package nl.hannahsten.texifyidea.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LatexMathHighlightingText : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/highlighting"
    }

    @Test
    fun testInlineMath() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        val hls = myFixture.doHighlighting()
        hls.forEach { println(it.text + ": " + it.problemGroup) }
//        myFixture.checkHighlighting(false, true, false ,false)
    }

    @Test
    fun testBracketMath() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        val hls = myFixture.doHighlighting()
        hls.forEach { println(it.text + ": " + it.problemGroup) }
//        println(hls.joinToString("\n"))
//        myFixture.checkHighlighting(false, true, false ,false)
    }
}