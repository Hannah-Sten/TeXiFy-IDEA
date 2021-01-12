package nl.hannahsten.texifyidea.lang.magic

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import org.junit.jupiter.api.Assertions

class MagicCommentPsiTest : BasePlatformTestCase() {

    fun `test get magic comment for command`() {
        myFixture.configureByText(LatexFileType, "%! suppress = FileNotFound\n\\documentclass{article}")
        val command = LatexCommandsIndex.getCommandsByName("\\documentclass", myFixture.file).first()
        val magic = command.magicComment()
        Assertions.assertEquals(arrayListOf("suppress = FileNotFound"), magic.toCommentString())
    }
}