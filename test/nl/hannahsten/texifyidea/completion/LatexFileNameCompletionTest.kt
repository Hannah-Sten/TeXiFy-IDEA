package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.Test

class LatexFileNameCompletionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/"
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCompleteRelativePath(){
        myFixture.configureByFile("includedfile.tex")
        myFixture.configureByText(LatexFileType,"""\input{<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.isNotEmpty())
    }
}