package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParserTest : BasePlatformTestCase() {

    fun testNestedInlineMath() {
        myFixture.configureByText(LatexFileType, """
            $ math \text{ text $\xi$ text } math$
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}