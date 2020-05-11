package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.Test

class LatexCompletionTest : BasePlatformTestCase() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCompleteLatexReferences() {
        // given
        myFixture.configureByText(LatexFileType, """\ap<caret>""")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertTrue("LaTeX autocompletion should be available", result.any { it.lookupString == "appendix" })
    }

    @Test
    fun testCompleteCustomCommandReferences() {
        // given
        myFixture.configureByText(LatexFileType, """
            \newcommand{\hi}{hi}
            \h<caret>
            """.trimIndent())

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertTrue("LaTeX autocompletion of custom commands should be available", result.any { it.lookupString == "hi" })
    }

    fun testCompleteCustomColorDefinitions() {
        myFixture.configureByText(LatexFileType, """
            \colorlet{fadedred}{red!70!}
            \color{r<caret>}
        """.trimIndent())

        val result = myFixture.complete(CompletionType.BASIC)

        assertTrue("fadedred should be available", result.any { it.lookupString == "fadedred" })
    }

    fun testCompletionInCustomArgument() {
        myFixture.configureByText(LatexFileType, """
            \newcommand{\hello}[1]{hello #1}
            \hello{\te<caret>}
        """.trimIndent())

        val result = myFixture.complete(CompletionType.BASIC)

        assertTrue(result.any { it.lookupString == "textbf" })
    }
}