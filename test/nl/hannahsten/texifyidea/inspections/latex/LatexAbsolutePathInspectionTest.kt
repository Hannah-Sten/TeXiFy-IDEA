package nl.hannahsten.texifyidea.inspections.latex

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
        myFixture.configureByText(LatexFileType, """<error>\include{$absoluteWorkingPath/test/resources/completion/path/testfile.tex}</error>""")
        myFixture.checkHighlighting()
    }
}