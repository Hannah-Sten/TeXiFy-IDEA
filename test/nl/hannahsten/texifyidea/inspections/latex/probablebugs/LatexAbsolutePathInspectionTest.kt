package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class LatexAbsolutePathInspectionTest : TexifyInspectionTestBase(LatexAbsolutePathInspection()) {

    var absoluteWorkingPath: String

    init {
        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString()
    }

    @Test
    fun testNotSupportedAbsolutePath() {
        myFixture.configureByText(LatexFileType, """\include{<error descr="No absolute path allowed here">$absoluteWorkingPath/test/resources/completion/path/testfile.tex</error>}""")
        myFixture.checkHighlighting()
    }

    @Test
    fun testNotSupportedAbsolutePathAsSecondParameter() {
        myFixture.configureByText(LatexFileType, """\import{/absolute/path/to/}{<error descr="No absolute path allowed here">$absoluteWorkingPath/test/resources/completion/path/testfile.tex</error>}""")
        myFixture.checkHighlighting()
    }
}