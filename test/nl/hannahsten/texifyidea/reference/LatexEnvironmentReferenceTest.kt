package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexEnvironmentReferenceTest : BasePlatformTestCase() {

    fun testEnvironmentRename() {
        myFixture.configureByText(LatexFileType, """
            \begin{document}
            \end{document<caret>}
        """.trimIndent())
        myFixture.renameElementAtCaret("nodocument")
        myFixture.checkResult("""
            \begin{nodocument}
            \end{nodocument<caret>}
        """.trimIndent())
    }
}