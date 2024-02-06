package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexEnvironmentIndentTest : BasePlatformTestCase() {

    fun `test indent environments true, document false`() {
        val text = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
            Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT = false
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS = true
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }

        val expected = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
                Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        myFixture.checkResult(expected)
    }

    fun `test indent environments true, document true`() {
        val text = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
            Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT = true
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS = true
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }

        val expected2 = """
            \begin{document}
                Don't indent this if turned off.
                \begin{some-env}
                    Indent this.
                \end{some-env}
            \end{document}
        """.trimIndent()
        myFixture.checkResult(expected2)
    }

    fun `test indent environments false, document false`() {
        val text = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
            Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT = false
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS = false
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }

        val expected3 = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
            Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        myFixture.checkResult(expected3)
    }

    fun `test indent environments false, document true`() {
        val text = """
            \begin{document}
            Don't indent this if turned off.
            \begin{some-env}
            Indent this.
            \end{some-env}
            \end{document}
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)

        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT = true
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_ENVIRONMENTS = false
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }

        val expected4 = """
            \begin{document}
                Don't indent this if turned off.
                \begin{some-env}
                Indent this.
                \end{some-env}
            \end{document}
        """.trimIndent()
        myFixture.checkResult(expected4)
    }

    fun `test enter handler for unindented document`() {
        val text = """
            \begin{document}
            Don't indent this if turned off.<caret>
            \end{document}
        """.trimIndent()
        val file = myFixture.configureByText(LatexFileType, text)
        CodeStyle.getCustomSettings(file, LatexCodeStyleSettings::class.java).INDENT_DOCUMENT_ENVIRONMENT = false
        myFixture.type("\nDon't indent this either")

        val expected = """
            \begin{document}
            Don't indent this if turned off.
            Don't indent this either<caret>
            \end{document}
        """.trimIndent()
        myFixture.checkResult(expected)
    }
}