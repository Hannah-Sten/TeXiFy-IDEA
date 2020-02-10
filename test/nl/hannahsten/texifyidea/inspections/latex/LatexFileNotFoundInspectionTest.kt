package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertFails

class LatexFileNotFoundInspectionTest : TexifyInspectionTestBase(LatexFileNotFoundInspection()) {
    var absoluteWorkingPath: String

    init {
        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString()
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

}