package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParameterTextUtilTest : BasePlatformTestCase() {

    fun testLabelRename() {
        myFixture.configureByText(
            LatexFileType,
            """
                \label{mylabel<caret>}{I redefined \label}
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("nolabel")
        myFixture.checkResult(
            """
            \label{nolabel<caret>}{I redefined \label}
            """.trimIndent()
        )
    }
}