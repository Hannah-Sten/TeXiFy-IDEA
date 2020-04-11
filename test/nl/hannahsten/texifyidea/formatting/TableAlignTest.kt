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

    fun testIssue() {
        """
            \begin{tabular}{l c r}
                Date & In tree: & Raining? \\ \hline
                April 26 & Yes & Yes \\
                June 7 & Yes & No \\
                Juli 20 & Yes & No \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{l c r}
                Date     & In tree: & Raining? \\ \hline
                April 26 & Yes      & Yes      \\
                June 7   & Yes      & No       \\
                Juli 20  & Yes      & No       \\
            \end{tabular}
        """.trimIndent()
    }

    fun testThisIsCorrect() {
        """
            \begin{tabular}{l c r}
                Date     & In tree: & Raining? \\ \hline
                April 26 & Yes      & Yes      \\
                June 7   & Yes      & No       \\
                Juli 20  & Yes      & No       \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{l c r}
                Date     & In tree: & Raining? \\ \hline
                April 26 & Yes      & Yes      \\
                June 7   & Yes      & No       \\
                Juli 20  & Yes      & No       \\
            \end{tabular}
        """.trimIndent()
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
                cccc & d & \\
                \hrule
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                \hrule
                a    & b & \\
                cccc & d & \\
                \hrule
            \end{tabular}
        """.trimIndent()
    }

    fun testHorizontalRules() {
        """
            \begin{tabular}{ccc}
                \toprule
                a & b & \\
                \midrule
                cccc & d & \\
                cccc & d & \\
                \bottomrule
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                \toprule
                a    & b & \\
                \midrule
                cccc & d & \\
                cccc & d & \\
                \bottomrule
            \end{tabular}
        """.trimIndent()
    }

    fun testNewlines() {
        """
            \begin{tabular}{cccc}
                aaa & b &
                a & d \\
                c & d &
                aaaaa & d \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{cccc}
                aaa & b &
                a     & d \\
                c   & d &
                aaaaa & d \\
            \end{tabular}
        """.trimIndent()
    }

    fun testNotAllNewlines() {
        """
            \begin{tabular}{cccc}
                a & b & ccccc & d \\
                aaa & b &
                a & d \\
                c & d &
                aaaaa & d \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{cccc}
                a   & b & ccccc & d \\
                aaa & b &
                a     & d \\
                c   & d &
                aaaaa & d \\
            \end{tabular}
        """.trimIndent()
    }

    fun testWithWords() {
        """
            \begin{tabular}{ccc}
                als ik naar de winkel fiets & b & \\
                c & d & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                als ik naar de winkel fiets & b & \\
                c                           & d & \\
            \end{tabular}
        """.trimIndent()
    }


    fun testMultiLine() {
        """
            \begin{tabular}{ccc}
                a hallo & b & \\
                cccc    & d & \\
                Ik &
                meer &
                regels \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                a hallo & b & \\
                cccc    & d & \\
                Ik &
                meer &
                regels \\
            \end{tabular}
        """.trimIndent()
    }

    fun ignoreLineStartWithAmpersand() {
        """
            \begin{tabular}{ccc}
                a    & b & \\
                cccc & d & \\
                & aaa & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                a    & b & \\
                cccc & d & \\
                & aaa    & \\
            \end{tabular}
        """.trimIndent()
    }

    fun testBacklashAlign() {
        """
            \begin{tabular}{ll}
                aaaa & b \\
                c    & d \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ll}
                aaaa & b \\
                c    & d \\
            \end{tabular}
        """.trimIndent()
    }

    fun testNoSpaces() {
        """
            \begin{tabular}{ll}
                aa&b\\
                c&d\\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ll}
                aa & b \\
                c  & d \\
            \end{tabular}
        """.trimIndent()
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(LatexFileType, this)
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }
}