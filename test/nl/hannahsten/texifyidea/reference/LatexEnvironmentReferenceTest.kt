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

    fun testEnvironmentRenameBegin() {
        myFixture.configureByText(LatexFileType, """
            \begin{document<caret>}
            \end{document}
        """.trimIndent())
        myFixture.renameElementAtCaret("nodocument")
        myFixture.checkResult("""
            \begin{nodocument<caret>}
            \end{nodocument}
        """.trimIndent())
    }
}