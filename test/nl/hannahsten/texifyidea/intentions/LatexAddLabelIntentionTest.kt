package nl.hannahsten.texifyidea.intentions

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexAddLabelIntentionTest : BasePlatformTestCase() {
    fun testMissingChapterLabel() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{Chapter<caret> without label}
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{Chapter without label}\label{ch:chapter-without-label}<caret>
            \end{document}
            """.trimIndent()
        )
    }

    fun testMissingChapterLabelAtEnd() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \chapter{Chapter without label}<caret>
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \chapter{Chapter without label}\label{ch:chapter-without-label}<caret>
            \end{document}
            """.trimIndent()
        )
    }

    fun testMissingSectionLabelWithComma() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \section{Section about A, B and C}<caret>
            \end{document}
            """.trimIndent()
        )
        val intentions = myFixture.availableIntentions
        writeCommand(myFixture.project) {
            intentions.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResult(
            """
            \begin{document}
                \section{Section about A, B and C}\label{sec:section-about-a-b-and-c}<caret>
            \end{document}
            """.trimIndent()
        )
    }
}