package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class LatexAbsolutePathCompletionTest : BasePlatformTestCase() {

    private lateinit var absoluteWorkingPath: String

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString().replace(File.separatorChar, '/')
    }

    @Test
    fun testAbsoluteFolderCompletion() {
        myFixture.configureByText(LatexFileType, """"\graphicspath{{$absoluteWorkingPath/test/resources/completion/path/<caret>}}""")

        val result: Array<LookupElement> = myFixture.complete(CompletionType.BASIC)

        // Should only show folders
        assert(result.removeFolderEntries().isEmpty())
    }

    @Test
    fun testAbsoluteFileCompletion() {
        myFixture.configureByText(LatexFileType, """\input{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        assert(result.isNotEmpty())
    }

    @Test
    fun testSupportedPictureExtensions() {
        myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // is only allowed to show folders and the png file
        assert(result.removeFolderEntries().size == 1)
    }

    @Test
    fun testSupportedInputExtensions() {
        myFixture.configureByText(LatexFileType, """\input{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // is only allowed to show folders and the tex file
        assert(result.removeFolderEntries().size == 1)
    }

    @Test
    fun testNonAbsolutePathSupport() {
        myFixture.configureByText(LatexFileType, """\include{$absoluteWorkingPath/test/resources/completion/cite/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // no absolute paths allowed with \include
        assert(result.isEmpty())
    }

    fun Array<LookupElement>.removeFolderEntries() = filterNot { it.lookupString.endsWith("/") }
}