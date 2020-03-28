package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParserTest : BasePlatformTestCase() {

    fun testArrayPreambleOptions() {
        myFixture.configureByText(LatexFileType, """
            \begin{tabular}{l >{$}l<{$}}
                some text & y = x (or any math) \\
                more text & z = 2 (or any math) \\
            \end{tabular}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}