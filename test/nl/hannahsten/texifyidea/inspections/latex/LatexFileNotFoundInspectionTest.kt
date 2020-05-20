package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertFails

class LatexFileNotFoundInspectionTest : TexifyInspectionTestBase(LatexFileNotFoundInspection()) {
    private var absoluteWorkingPath: String

    init {
        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString().replace(File.separatorChar, '/')
    }

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/filenotfound"
    }

    @Test
    fun testInvalidAbsolutePath() {
        myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/myPicture.myinvalidextension</error>}""")
        myFixture.checkHighlighting()
    }

    @Test
    fun testValidAbsolutePath() {
        myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/myPicture.png</error>}""")

        assertFails {
            myFixture.checkHighlighting()
        }
    }

    @Test
    fun testBackActionAbsolute() {
        myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/../path/../path/myPicture.png</error>}""")

        assertFails {
            myFixture.checkHighlighting()
        }
    }

    @Test
    fun testCurrDirActionAbsolute() {
        myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/./resources/./././completion/path/././myPicture.png</error>}""")

        assertFails {
            myFixture.checkHighlighting()
        }
    }

    @Test
    fun testAbsoluteGraphicsDirWithInclude() {
        myFixture.configureByText(LatexFileType, """
            \graphicspath{{$absoluteWorkingPath/test/resources/completion/path/}}
            \includegraphics{myPicture.png}
            """.trimIndent())

        myFixture.checkHighlighting()
    }

    @Test
    fun testDefaultExtensionCompletion() {
        myFixture.configureByText(LatexFileType, """\includegraphics{<error>$absoluteWorkingPath/test/resources/completion/path/myPicture</error>}""")

        assertFails {
            myFixture.checkHighlighting()
        }
    }

    // Test isn't working
//    @Test
//    fun testImportAbsolutePath() {
//        myFixture.configureByFiles("ImportPackageAbsolutePath.tex", "chapters/included.tex")
//        myFixture.type("$absoluteWorkingPath/chapters/")
//        myFixture.checkHighlighting()
//    }

    @Test
    fun testInvalidImportAbsolutePath() {
        myFixture.copyFileToProject("chapters/included.tex")
        myFixture.configureByText(LatexFileType, """\import{/does/not/exist}{<error>included</error>}""")
        myFixture.checkHighlighting()
    }

    @Test
    fun testImportAbsolutePathIncludedFile() {
        val files = myFixture.configureByFiles("ImportPackageAbsolutePath.tex", "chapters/included.tex", "chapters/included2.tex")
        myFixture.type("$absoluteWorkingPath/chapters/")
        myFixture.openFileInEditor(files[1].virtualFile)
        myFixture.checkHighlighting()
    }

    @Test
    fun testImportRelativePathIncludedFile() {
        myFixture.configureByFiles("chapters/included.tex", "ImportPackageRelativePath.tex", "chapters/included2.tex")
        myFixture.checkHighlighting()
    }

    @Test
    fun testInvalidImportRelativePathIncludedFile() {
        myFixture.configureByFiles("chapters/notincluded.tex", "ImportPackageRelativePathInvalid.tex", "chapters/included2.tex")
        myFixture.checkHighlighting()
    }
}