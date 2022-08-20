package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexPathCompletionWithCommandExpansion : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/path/commandexpansion"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    fun `test completion without expansion`() {
        myFixture.configureByText(LatexFileType, """\includegraphics{i<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test completion in main file of path that contains macro`() {
        myFixture.configureByFile("main.tex")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test completion in sub file of path that contains macro`() {
        myFixture.configureByFile("nest/sub.tex")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test expansion in path completion`() {
        // For this use case you would usually use \graphicspath, but just to test that it also works for other use cases
        // than subfiles.
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\img}{images}
            \includegraphics{\img/p<caret>}
            """.trimIndent()
        )

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/pepper.jpg" })
    }
}