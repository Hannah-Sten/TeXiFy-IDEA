package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateCommandDef
import java.nio.file.Path
import java.nio.file.Paths

class LatexAbsolutePathInspectionTest : TexifyInspectionTestBase(LatexAbsolutePathInspection()) {

    var absoluteWorkingPath: String

    init {
        val currentRelativePath: Path = Paths.get("")
        absoluteWorkingPath = currentRelativePath.toAbsolutePath().toString()
    }

    fun testNotSupportedAbsolutePath() {
        myFixture.configureByText(LatexFileType, """\include{<error descr="No absolute path allowed here">$absoluteWorkingPath/test/resources/completion/path/testfile.tex</error>}""")
        myFixture.checkHighlighting()
    }

    fun testNotSupportedAbsolutePathAsSecondParameter() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{import}
            \import{/absolute/path/to/}{<error descr="No absolute path allowed here">$absoluteWorkingPath/test/resources/completion/path/testfile.tex</error>}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }
}