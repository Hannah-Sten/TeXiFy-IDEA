package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexGlossaryReferenceTest : BasePlatformTestCase() {

    fun `test rename glossary label from label`() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{glossaries}
            \newacronym{aslr<caret>}{ASLR}{Address Space Layout Randomization}
            \begin{document}
                \gls{aslr}
            \end{document}
            """.trimIndent()
        )

        // when
        myFixture.renameElementAtCaret("renamed")

        // then
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{glossaries}
            \newacronym{renamed<caret>}{ASLR}{Address Space Layout Randomization}
            \begin{document}
                \gls{renamed}
            \end{document}
            """.trimIndent()
        )
    }

    private fun testGlossaryReferenceRename(command: String) {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{glossaries}
            \newacronym{aslr}{ASLR}{Address Space Layout Randomization}
            \begin{document}
                \$command{aslr<caret>}
            \end{document}
            """.trimIndent()
        )

        // when
        myFixture.renameElementAtCaret("renamed")

        // then
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{glossaries}
            \newacronym{renamed}{ASLR}{Address Space Layout Randomization}
            \begin{document}
                \$command{renamed<caret>}
            \end{document}
            """.trimIndent()
        )
    }

    fun `test rename glossary label from reference`() {
        testGlossaryReferenceRename("gls")
        testGlossaryReferenceRename("Gls")
        testGlossaryReferenceRename("glspl")
        testGlossaryReferenceRename("Gls")
    }
}