package nl.hannahsten.texifyidea.formatting

import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexFormattingTest : BasePlatformTestCase() {

    /**
     * When having "\\\n", i.e. backslash newline, if that is a valid latex command then the next newline will not start a new line,
     * thus confuses the formatter.
     * So most probably this was a mistake. If ever someone comes up with a valid reason for using backslash newline, we can rethink this.
     */
    fun testBacklashNewline() {
        """
            Text hallo i.e.\
            \[
                \alpha
            \]
        """.trimIndent() `should be reformatted to` """
            Text hallo i.e.
            \[
                \alpha
            \]
        """.trimIndent()
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(LatexFileType, this)
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }
}