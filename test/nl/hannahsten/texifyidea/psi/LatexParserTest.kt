package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.jupiter.api.Test

class LatexParserTest : BasePlatformTestCase() {

    @Test
    fun testIfnextchar() {
        myFixture.configureByText(LatexFileType, """
            \newcommand{\xyz}{\@ifnextchar[{\@xyz}{\@xyz[default]}}
            \def\@xyz[#1]#2{do something with #1 and #2}
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}