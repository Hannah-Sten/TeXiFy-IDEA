package nl.hannahsten.texifyidea.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexInlineDisplayToggleIntentionTest : BasePlatformTestCase() {

    fun testInlineToDisplayToggle() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                $\pi = 3<caret>$
            \end{document}
            """.trimIndent()
        )
        myFixture.findSingleIntention("Toggle inline/display math mode").invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                \[
                    \pi = 3
                \]
            \end{document}
            """.trimIndent()
        )
    }

    fun testDisplayToInline() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \[
                    \pi = 3<caret>
                \]
            \end{document}
            """.trimIndent()
        )
        myFixture.findSingleIntention("Toggle inline/display math mode").invoke(myFixture.project, myFixture.editor, myFixture.file)
        myFixture.checkResult(
            """
            \begin{document}
                $\pi = 3$
            \end{document}
            """.trimIndent()
        )
    }
}