package nl.hannahsten.texifyidea.editor

import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.parser.firstChildOfType

class LatexMoveElementLeftRightHandlerTest : BasePlatformTestCase() {

    fun `test command with single required argument`() {
        commandWithSubElements("""\fbox{text}""", "{text}")
    }

    fun `test command with three required arguments`() {
        commandWithSubElements("""\bloop{a}{b}{c}""", "{a}", "{b}", "{c}")
    }

    fun `test command with single optional argument`() {
        commandWithSubElements("""\bloop[a]""", "[a]")
    }

    fun `test command with three optional arguments`() {
        commandWithSubElements("""\bloop[a][b][c]""", "[a]", "[b]", "[c]")
    }

    fun `test with mixed arguments`() {
        commandWithSubElements("""\bloop{a}[A]{b}""", "{a}", "[A]", "{b}")
    }

    private fun commandWithSubElements(command: String, vararg expectedSubElements: String) {
        val subElements = LatexMoveElementLeftRightHandler()
            .getMovableSubElements(command.toPsi().firstChildOfType(LatexCommands::class) as PsiElement)
            .map { it.text }
        assertEquals(expectedSubElements.toList(), subElements)
    }

    private fun String.toPsi() = LatexPsiHelper(myFixture.project).createFromText(this)
}
