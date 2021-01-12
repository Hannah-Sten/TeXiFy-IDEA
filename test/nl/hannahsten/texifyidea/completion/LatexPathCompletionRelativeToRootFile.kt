package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

/**
 * File inclusions in the test files in test/resources/completion/relativetoroot/
 *
 * - main.tex:
 *      \input{inputted.tex}
 *      \input{nest/bird2.tex}
 * - inputted.tex:
 * - nest/
 *     - birdmom.tex:
 *          \input{bird1.tex}
 *     - bird1.tex:
 *     - bird2.tex:
 *
 */
class LatexPathCompletionRelativeToRootFile : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/completion/path/relativetoroot"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    fun `test completion in directory of root file`() {
        myFixture.configureByText(LatexFileType, """\input{i<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "inputted.tex" })
    }

    fun `test completion in included file from subdirectory for files in main dir`() {
        myFixture.configureByFile("nest/bird2.tex")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "inputted.tex" })
    }

    fun `test completion in included file from subdirectory for directories in main dir`() {
        myFixture.configureByFile("nest/bird2.tex")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.any { it.lookupString == "nest/" })
    }
}