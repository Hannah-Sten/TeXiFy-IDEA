package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexMagicCommentCompletionTest : BasePlatformTestCase() {

    fun `test magic comment key completion`() {
        myFixture.configureByText(LatexFileType, """
            %! comp<caret>
            Kaas
            """.trimIndent())
        val result = myFixture.complete(CompletionType.BASIC)
        assert(result.any { it.lookupString == "Compiler" })
    }

    fun `test magic comment value completion`() {
        myFixture.configureByText(LatexFileType, """
            %! begin preamble = t<caret>
            Frikandel
            """.trimIndent())
        val result = myFixture.complete(CompletionType.BASIC)
        assert(result.any { it.lookupString == "tikz" })
    }

    fun `test completion of fake without =`() {
        myFixture.configureByText(LatexFileType, """
            %! fak<caret>
            Kroket
            """.trimIndent())
        myFixture.complete(CompletionType.BASIC)
        myFixture.checkResult("""
            %! fake <caret>
            Kroket
            """.trimIndent())
    }

    fun `test completion of fake section`() {
        myFixture.configureByText(LatexFileType, """
            %! fake sect<caret>
            Kip
        """.trimMargin())
        val result = myFixture.complete(CompletionType.BASIC)
        kotlin.test.assertEquals(3, result.size)
        assert(result.any { it.lookupString == "section" })
    }
}