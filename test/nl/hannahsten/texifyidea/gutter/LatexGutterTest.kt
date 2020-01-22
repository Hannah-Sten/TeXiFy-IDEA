package nl.hannahsten.texifyidea.gutter

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LatexGutterTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/gutter"
    }

    @Test
    fun testShowCompileGutter() {
        val testName = getTestName(false)
        val gutters = myFixture.findAllGutters("$testName.tex")
        assertEquals(1, gutters.size)
        assertEquals("Compile document", gutters.first().tooltipText)
    }
}