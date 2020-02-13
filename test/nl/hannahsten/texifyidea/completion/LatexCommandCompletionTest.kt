package nl.hannahsten.texifyidea.completion

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.Test

/**
 * Add completion of commands.
 */
class LatexCommandCompletionTest : BasePlatformTestCase() {
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    override fun getTestDataPath(): String {
        return "test/resources/completion/commands/requiredparameters"
    }

    @Test
    fun testNoParameters() {
        testCompletion("""\app<caret>""", """\appendix<caret>""")
    }

    @Test
    fun testOneParameter() {
        testCompletion("""\cap<caret>""", """\caption{<caret>}""")
    }

    @Test
    fun testOneParameterWithBrackets() {
        testCompletion("""\cap<caret>{}""", """\caption{<caret>}""")
    }

    @Test
    fun testTwoParameters() {
        testCompletion("""\par<caret>""", """\parbox{<caret>}{}""")
    }

    private fun testCompletion(before: String, after: String) {
        // given before and after
        val finalCaretOffset = when(val index = after.indexOf("<caret>")) {
            -1 -> return  // <caret> was not found in after.
            else -> index
        }

        // when
        myFixture.configureByText(LatexFileType, before)
        myFixture.completeBasic()

        // then
        assert(myFixture.file.text == after.removeRange(finalCaretOffset, finalCaretOffset + 7))
        assert(myFixture.caretOffset == finalCaretOffset)
    }
}