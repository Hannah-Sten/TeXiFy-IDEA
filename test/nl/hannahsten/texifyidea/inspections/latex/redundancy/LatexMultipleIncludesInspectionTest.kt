package nl.hannahsten.texifyidea.inspections.latex.redundancy

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexMultipleIncludesInspectionTest : TexifyInspectionTestBase(LatexMultipleIncludesInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{<error descr="Package has already been included">rubikrotation</error>}
            \usepackage{<error descr="Package has already been included">rubikrotation</error>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testConditionalInclude() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            
            \onlyifstandalone{
            \usepackage{amsmath}
            \usepackage{amsmath}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testDuplicatesInOneCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \documentclass{article}
            \usepackage{<error descr="Package has already been included">amsmath</error>, <error descr="Package has already been included">amsmath</error>}
            \usepackage{amssymb,<error descr="Package has already been included">amsmath</error>}
            """.trimIndent()
        )
    }
}