package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class LatexPathCompletionWithCommandExpansion : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/path/commandexpansion"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    fun `test completion without expansion`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{graphicx}
            \includegraphics{i<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test completion in main file of path that contains macro`() {
        myFixture.configureByFile("main.tex")
        myFixture.updateCommandDef()

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test completion in sub file of path that contains macro`() {
        myFixture.configureByFile("nest/sub.tex")
        myFixture.updateCommandDef()
        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/" })
    }

    fun `test expansion in path completion`() {
        // For this use case you would usually use \graphicspath, but just to test that it also works for other use cases
        // than subfiles.
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{graphicx}
            \newcommand{\img}{images}
            \includegraphics{\img/p<caret>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "images/pepper.jpg" })
    }
}