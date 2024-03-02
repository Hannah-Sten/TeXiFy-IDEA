package nl.hannahsten.texifyidea.editor

import com.intellij.application.options.CodeStyle
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParagraphFillHandlerTest : BasePlatformTestCase() {

    fun `test single sentence`() {
        testFillParagraph("This is a nor<caret>mal sentence.", "This is a normal sentence.")
    }

    fun `test single sentence with inline math`() {
        testFillParagraph("This is a nor<caret>mal \$a + \\alpha\$ sentence.", "This is a normal \$a + \\alpha\$ sentence.")
    }

    fun `test at start of environment`() {
        testFillParagraph(
            """
            \begin{document}
                Lorem ipsum dolor sit <caret>amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                Lorem ipsum dolor sit amet, consectetur 
                adipiscing elit, sed do eiusmod tempor 
                incididunt ut labore et dolore magna aliqua. 
            \end{document}
            """.trimIndent()
        )
    }

    fun `test after begin section with new line`() {
        testFillParagraph(
            """
            \section{hallo}
            Lorem <caret>ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            """.trimIndent(),
            """
            \section{hallo}
            Lorem ipsum dolor sit amet, consectetur 
            adipiscing elit, sed do eiusmod tempor 
            incididunt ut labore et dolore magna aliqua. 
            """.trimIndent()
        )
    }

    fun `test after begin section without new line`() {
        testFillParagraph(
            """
            \section{hallo} Lorem <caret>ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            """.trimIndent(),
            """
            \section{hallo} Lorem ipsum dolor sit amet, 
            consectetur adipiscing elit, sed do eiusmod 
            tempor incididunt ut labore et dolore magna 
            aliqua. 
            """.trimIndent()
        )
    }

    fun `test lorem ipsum paragraph`() {
        testFillParagraph(
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            Duis aute iru<caret>re dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 
            Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            """.trimIndent(),
            """
            Lorem ipsum dolor sit amet, consectetur
            adipiscing elit, sed do eiusmod tempor
            incididunt ut labore et dolore magna aliqua.
            Ut enim ad minim veniam, quis nostrud
            exercitation ullamco laboris nisi ut aliquip
            ex ea commodo consequat. Duis aute irure dolor
            in reprehenderit in voluptate velit esse
            cillum dolore eu fugiat nulla pariatur.
            Excepteur sint occaecat cupidatat non
            proident, sunt in culpa qui officia deserunt
            mollit anim id est laborum.
            """.trimIndent()
        )
    }

    fun `test paragraph that was good`() {
        testFillParagraph(
            """
            Lorem ipsum dolor sit amet, consectetur
            adipiscing elit, sed do eiusmod tempor NEW WORDS HERE
            incididunt ut labore et dolore magna aliqua. Ut 
            enim ad minim veniam, quis nostrud exercitation 
            ullamco laboris nisi ut aliquip ex ea commodo 
            consequat. Duis aute irure dolor in 
            reprehenderit in voluptate velit esse cillum 
            dolore eu fugiat nulla pariatur. Excepteur sint 
            occaecat cupidatat non proident, sunt in culpa 
            qui officia deserunt mollit anim id est laborum.
            """.trimIndent(),
            """
            Lorem ipsum dolor sit amet, consectetur
            adipiscing elit, sed do eiusmod tempor NEW
            WORDS HERE incididunt ut labore et dolore
            magna aliqua. Ut enim ad minim veniam, quis
            nostrud exercitation ullamco laboris nisi ut
            aliquip ex ea commodo consequat. Duis aute
            irure dolor in reprehenderit in voluptate
            velit esse cillum dolore eu fugiat nulla
            pariatur. Excepteur sint occaecat cupidatat
            non proident, sunt in culpa qui officia
            deserunt mollit anim id est laborum.
            """.trimIndent()
        )
    }

    fun `test paragraph separated by blank lines`() {
        testFillParagraph(
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore ma<caret>gna aliqua. 

            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent(),
            """
            Lorem ipsum dolor sit amet, consectetur
            adipiscing elit, sed do eiusmod tempor 
            incididunt ut labore et dolore magna aliqua. 

            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent()
        )
    }

    fun `test paragraph separated by environment`() {
        testFillParagraph(
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore ma<caret>gna aliqua. 
            \begin{center}
                bla
            \end{center}
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent(),
            """
            Lorem ipsum dolor sit amet, consectetur 
            adipiscing elit, sed do eiusmod tempor 
            incididunt ut labore et dolore magna aliqua.
            \begin{center}
                bla
            \end{center}
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent()
        )
    }

    fun `test paragraph separated by display math`() {
        testFillParagraph(
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore ma<caret>gna aliqua. 
            \[
                \sum_{i=1}^\infty \frac{e^i}{i!}
            \]
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent(),
            """
            Lorem ipsum dolor sit amet, consectetur
            adipiscing elit, sed do eiusmod tempor 
            incididunt ut labore et dolore magna aliqua. 
            \[
                \sum_{i=1}^\infty \frac{e^i}{i!}
            \]
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. 
            """.trimIndent()
        )
    }

    private fun testFillParagraph(input: String, expectedOutput: String) {
        myFixture.configureByText(LatexFileType, input)
        CodeStyle.getLanguageSettings(myFixture.file).RIGHT_MARGIN = 50
        myFixture.performEditorAction("FillParagraph")
        myFixture.checkResult(expectedOutput, true)
    }
}