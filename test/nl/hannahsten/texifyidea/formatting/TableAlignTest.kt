package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
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
                a & d \\
                c & d &
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
                a & b & ccccc & d \\
                aaa & b &
                a & d \\
                c & d &
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

//    fun testLineStartWithAmpersand() {
//        """
//            \begin{tabular}{ccc}
//                a    & b & \\
//                cccc & d & \\
//                & aaa & \\
//            \end{tabular}
//        """.trimIndent() `should be reformatted to` """
//            \begin{tabular}{ccc}
//                a    & b & \\
//                cccc & d & \\
//                & aaa    & \\
//            \end{tabular}
//        """.trimIndent()
//    }

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

    fun testMultiColumn() {
        """
            \begin{tabular}{lcr}
                Storyline & Number of Books & User rating \\ \hline
                City Watch & 10 & 80 \\
                Witches & 6 & 75 \\
                Rincewind & \multicolumn{2}{c}{n.a.} \\
                Death & 5 & 70 \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{lcr}
                Storyline  & Number of Books & User rating \\ \hline
                City Watch & 10              & 80          \\
                Witches    & 6               & 75          \\
                Rincewind & \multicolumn{2}{c}{n.a.} \\
                Death      & 5               & 70          \\
            \end{tabular}
        """.trimIndent()
    }

    fun testInlineMath() {
        """
            \begin{tabular}{ll}
                aaaa & b \\
                a b $ c $    & de \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ll}
                aaaa      & b  \\
                a b $ c $ & de \\
            \end{tabular}
        """.trimIndent()
    }

    fun testWithCommands() {
        """
            \begin{tabular}{ll}
                aaaa & b \\
                a b \hi &\bye de \\
                a &\h \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ll}
                aaaa    & b       \\
                a b \hi & \bye de \\
                a       & \h      \\
            \end{tabular}
        """.trimIndent()
    }

    fun testHalfEmptyCells() {
        """
            \begin{tabular}{ccc}
                aaaa & aaa & aa \\
                b b & & b \\
                c & c & \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{ccc}
                aaaa & aaa & aa \\
                b b  &     & b  \\
                c    & c   &    \\
            \end{tabular}
        """.trimIndent()
    }

    fun testLineWithoutAmpersands() {
        """
            \begin{tabular}{cc}
                a & b \\
                cc & d \\
                \hline
                \\
                f & g \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{cc}
                a  & b \\
                cc & d \\
                \hline
                \\
                f  & g \\
            \end{tabular}
        """.trimIndent()
    }

    fun testEscapedAmpersands() {
        """
            \begin{tabular}{cc}
                a & b \\
                c\&e & d \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{cc}
                a    & b \\
                c\&e & d \\
            \end{tabular}
        """.trimIndent()
    }

    fun testEscapedAmpersands2() {
        """
            \begin{tabular}{lll}
                Lorem ipsum & Dolor sit           & Consectetur   \\
                Ut \& & Dapibus placerat \& lacus & Ac vestibulum \\
            \end{tabular}
        """.trimIndent() `should be reformatted to` """
            \begin{tabular}{lll}
                Lorem ipsum & Dolor sit                 & Consectetur   \\
                Ut \&       & Dapibus placerat \& lacus & Ac vestibulum \\
            \end{tabular}
        """.trimIndent()
    }

    fun testAmpersandsInUrl() {
        """
            \documentclass{article}
            \usepackage{hyperref}
            \begin{document}
                \begin{tabular}{ll}
                    a & \url{https://youtu.be/dQw4w9WgXcQ?si=SHq6ADlwRNZ1hwOB&t=18} & c \\
                    be & \url{https://youtu.be/dQw4w9WgXcQ?si=SHq6ADlwRNZ1hwOB&t=18} & d
                \end{tabular}
            \end{document}
        """.trimIndent() `should be reformatted to` """
            \documentclass{article}
            \usepackage{hyperref}
            \begin{document}
                \begin{tabular}{ll}
                    a  & \url{https://youtu.be/dQw4w9WgXcQ?si=SHq6ADlwRNZ1hwOB&t=18} & c \\
                    be & \url{https://youtu.be/dQw4w9WgXcQ?si=SHq6ADlwRNZ1hwOB&t=18} & d
                \end{tabular}
            \end{document}
        """.trimIndent()
    }

    fun testVeryWideTable() {
        val start = """
\documentclass[11pt]{article}
\begin{document}
    \begin{tabular}{ccccc}
        Quisque euismod est eu auctor venenatis. Aenean pulvinar eu dolor vitae tempus. Fusce mattis magna et nulla euismod euismod. Proin tempor ligula non nibh consectetur egestas. & In pretium ante non mattis placerat. Proin elementum nisi ut purus pellentesque, ut laoreet velit lobortis. Sed commodo ut urna in ornare. Proin quis dui non dolor auctor condimentum. & Ut consectetur sem nec tempor hendrerit. Fusce eget erat pulvinar, sodales ipsum porttitor, mattis lacus. Curabitur & eget ipsum in sapien venenatis interdum. & Duis vestibulum sem at euismod placerat. Cras nulla nibh, accumsan nec interdum in, dapibus sit amet orci. \\
        Nullam dapibus, lacus a & condimentum dignissim, turpis libero pretium magna, non maximus diam massa vitae ligula. & Vivamus a diam cursus, vehicula quam sit amet, condimentum metus. Sed venenatis pulvinar risus et condimentum. Integer pharetra ornare elit. & Maecenas placerat ut justo et mollis. Nullam in felis eu tellus varius semper. Nullam iaculis nulla & ligula, at sodales mi malesuada ac. \\
        In & nec & mi & id magna & tempus lobortis ac ac elit. \\
        In rutrum, & ligula & non & placerat & imperdiet. \\
    \end{tabular}
\end{document}
        """.trimIndent()
        val expected = """
\documentclass[11pt]{article}
\begin{document}
    \begin{tabular}{ccccc}
        Quisque euismod est eu auctor venenatis. Aenean pulvinar eu dolor vitae tempus. Fusce mattis magna et nulla euismod euismod. Proin tempor ligula non nibh consectetur egestas.
        & In pretium ante non mattis placerat. Proin elementum nisi ut purus pellentesque, ut laoreet velit lobortis. Sed commodo ut urna in ornare. Proin quis dui non dolor auctor condimentum.
        & Ut consectetur sem nec tempor hendrerit. Fusce eget erat pulvinar, sodales ipsum porttitor, mattis lacus. Curabitur
        & eget ipsum in sapien venenatis interdum.
        & Duis vestibulum sem at euismod placerat. Cras nulla nibh, accumsan nec interdum in, dapibus sit amet orci.
        \\
        Nullam dapibus, lacus a & condimentum dignissim, turpis libero pretium magna, non maximus diam massa vitae ligula.
        & Vivamus a diam cursus, vehicula quam sit amet, condimentum metus. Sed venenatis pulvinar risus et condimentum. Integer pharetra ornare elit.
        & Maecenas placerat ut justo et mollis. Nullam in felis eu tellus varius semper. Nullam iaculis nulla
        & ligula, at sodales mi malesuada ac.
        \\
        In         & nec    & mi  & id magna & tempus lobortis ac ac elit. \\
        In rutrum, & ligula & non & placerat & imperdiet.                  \\
    \end{tabular}
\end{document}
        """.trimIndent()
        myFixture.configureByText(LatexFileType, start)
        CodeStyle.getLanguageSettings(myFixture.editor)?.WRAP_ON_TYPING = 1
        writeCommand(project) {
            // That's a bug.
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        myFixture.checkResult(expected)
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(LatexFileType, this)
        writeCommand(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        myFixture.checkResult(expected)
    }
}