package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.util.SystemInfo
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

        val result = myFixture.complete(CompletionType.BASIC)

        // only ./ and ../ should be shown
        assert(result.size == 2)
    }

    @Test
    fun testAbsoluteFileCompletion() {
        myFixture.configureByText(LatexFileType, """\input{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assert(result.isNotEmpty())
    }

    @Test
    fun testSupportedPictureExtensions() {
        myFixture.configureByText(LatexFileType, """\includegraphics{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // is only allowed to show ./ ../ and the png file
        assert(result.size == 3)
    }

    @Test
    fun testSupportedInputExtensions() {
        myFixture.configureByText(LatexFileType, """\input{$absoluteWorkingPath/test/resources/completion/path/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // is only allowed to show ./ ../ and the tex file
        assert(result.size == 3)
    }

    @Test
    fun testNonAbsolutePathSupport() {
        myFixture.configureByText(LatexFileType, """\include{$absoluteWorkingPath/test/resources/completion/cite/<caret>}""")

        val result = myFixture.complete(CompletionType.BASIC)

        // no absolute paths allowed with \include
        assert(result.isEmpty())
    }
}