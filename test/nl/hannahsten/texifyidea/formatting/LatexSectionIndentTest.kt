package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexSectionIndentTest : BasePlatformTestCase() {

    fun testSectionIndent() {
        val text = """
            \section{test}
            Text.
            $\xi$
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        val expected = """
            \section{test}
                Text.
                $\xi$
        """.trimIndent()
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }

    fun testSectionIndent2() {
        val text = """
            \section{test}
            This $\xi$ block does not
            start on a newline.
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        val expected = """
            \section{test}
                This ${'$'}\xi${'$'} block does not
                start on a newline.
        """.trimIndent()
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }

    fun testNoSectionIndent() {
        val text = """
            \section{test}
            Text.
            $\xi$
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        val expected = """
            \section{test}
            Text.
            $\xi$
        """.trimIndent()
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }

    fun testSectionIndentExtended() {
        val text = """
            \section{test}
            Text.
            $\xi$
            \subsection{Nope}
            More text.
            \section{Two}
            No more text.
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        val expected = """
            \section{test}
                Text.
                $\xi$

                \subsection{Nope}
                    More text.


            \section{Two}
                No more text.
        """.trimIndent()
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }

    fun testEnterHandlerSectionIndent() {
        val file = myFixture.configureByText(
            LatexFileType, """
            \section{test}
                Isn't<caret>
        """.trimIndent()
        )
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        myFixture.type('\n')
        myFixture.checkResult(
            """
            \section{test}
                Isn't
                <caret>
        """.trimIndent()
        )
    }

    fun testEnterHandlerSectionIndentInMiddle() {
        val file = myFixture.configureByText(
            LatexFileType, """
            \section{test}
                Text.
                Isn't<caret>
            \section{two}
        """.trimIndent()
        )
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        myFixture.type('\n')
        myFixture.checkResult(
            """
            \section{test}
                Text.
                Isn't
                <caret>
            \section{two}
        """.trimIndent()
        )
    }

    fun testEnterHandlerSubsection() {
        val file = myFixture.configureByText(
            LatexFileType, """
            \section{test}
                \subsection{sub}
                    Isn't $\xi$<caret>
            \section{two}
        """.trimIndent()
        )
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_SECTIONS = true
        myFixture.type('\n')
        myFixture.checkResult(
            """
            \section{test}
                \subsection{sub}
                    Isn't $\xi$
                    <caret>
            \section{two}
        """.trimIndent()
        )
    }
}