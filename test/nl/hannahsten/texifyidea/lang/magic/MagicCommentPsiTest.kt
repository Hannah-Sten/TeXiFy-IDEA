package nl.hannahsten.texifyidea.lang.magic

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import org.junit.jupiter.api.Assertions

class MagicCommentPsiTest : BasePlatformTestCase() {

    fun `test get magic comment for command`() {
        myFixture.configureByText(LatexFileType, "%! suppress = FileNotFound\n\\documentclass{article}")
        val command = NewCommandsIndex.getByName("\\documentclass", myFixture.file).first()
        val magic = command.magicComment()
        Assertions.assertEquals(arrayListOf("suppress = FileNotFound"), magic.toCommentString())
    }

    fun `test get magic comment for file`() {
        myFixture.configureByText(
            LatexFileType,
            """
            %!compiler = xelatex
            \documentclass{article}

            %! suppress = Ellipsis
            \begin{document}
                ...
            \end{document}
            """.trimIndent()
        )
        val magic = myFixture.file.magicComment()
        assertEquals(arrayListOf("compiler = xelatex"), magic.toCommentString())
    }

    fun `test get multiple magic comments for file`() {
        myFixture.configureByText(
            LatexFileType,
            """
            %! suppress = NonBreakingSpace
            %! suppress = UnresolvedReference
            I \ref{nolabel}
            """.trimIndent()
        )
        val magic = myFixture.file.magicComment()
        assertEquals(arrayListOf("suppress = NonBreakingSpace", "suppress = UnresolvedReference"), magic.toCommentString())
    }
}