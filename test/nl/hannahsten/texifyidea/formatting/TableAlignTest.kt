package nl.hannahsten.texifyidea.formatting

import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.testutils.writeCommand

class TableAlignTest : BasePlatformTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    fun testTooMuchSpaces() {
        """
        \begin{tabular}{ccc}
            a    & b       & \\
            cccc      & d & \\
        \end{tabular}
        """.trimIndent() `should be reformatted to` """
        \begin{tabular}{ccc}
            a    & b & \\
            cccc & d & \\
        \end{tabular}
        """.trimIndent()
    }

    fun testSpacesEverywhere() {
        """
            \begin{tabular}{ccc}
                a    &    b & \\
                cccc & d   & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                a    & b & \\
                cccc & d & \\
            \end{tabular}
        """.trimIndent()
    }

    fun testBeginSpaceRemoval() {
        """
            \begin{tabular}{ccc}
                a &   b & \\
                c & d & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                a & b & \\
                c & d & \\
            \end{tabular}
        """.trimIndent()
    }

    fun testNormalTable() {
        """
        \begin{tabular}{ccc}
            a & b & \\
            cccc & d & \\
        \end{tabular}
        """.trimIndent() `should be reformatted to` """
        \begin{tabular}{ccc}
            a    & b & \\
            cccc & d & \\
        \end{tabular}
        """.trimIndent()
    }

    fun testIgnoreHorizontalRules() {
        """
            \begin{tabular}{ccc}
                \hrule
                a & b & \\
                \hrule
                cccc & d & \\
                \hrule
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                \hrule
                a    & b & \\
                \hrule
                cccc & d & \\
                \hrule
            \end{tabular}
        """.trimIndent()
    }

    fun testLineStartWithAmpersand() {
        """
            \begin{tabular}{ccc}
                a    & b & \\
                cccc & d & \\
                & aaa & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                a    & b   & \\
                cccc & d   & \\
                     & aaa & \\
            \end{tabular}
        """.trimIndent()
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(LatexFileType, this)
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }
}