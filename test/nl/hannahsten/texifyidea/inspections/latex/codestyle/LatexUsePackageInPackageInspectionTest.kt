package nl.hannahsten.texifyidea.inspections.latex.codestyle

import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexUsePackageInPackageInspectionTest : TexifyInspectionTestBase(LatexUsePackageInPackageInspection()) {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    fun testRequiredArgumentQuickFix() = testQuickFix(
        before = """\usepackage{xcolor}""",
        after = """\RequirePackage{xcolor}""",
        fileName = "mypackage.sty"
    )

    fun testOptionalArgumentQuickFix() = testQuickFix(
        before = """\usepackage[optional]{xcolor}""",
        after = """\RequirePackage[optional]{xcolor}""",
        fileName = "mypackage.sty"
    )

    fun testVersionArgumentQuickFix() = testQuickFix(
        before = """\usepackage{xcolor}[version]""",
        after = """\RequirePackage{xcolor}[version]""",
        fileName = "mypackage.sty"
    )

    fun testOptionalAndVersionArgumentQuickFix() = testQuickFix(
        before = """\usepackage[optional]{xcolor}[version]""",
        after = """\RequirePackage[optional]{xcolor}[version]""",
        fileName = "mypackage.sty"
    )

    fun testMatches() {
        myFixture.configureByText(
            StyleFileType,
            """
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage{xcolor, twee}</weak_warning>
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage[colorlinks]{hyperref}</weak_warning>
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage[aaa, bbb]{test}</weak_warning>
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage[options]{packagename}[version]</weak_warning>
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage{name}[version]</weak_warning>
            <weak_warning descr="Use \RequirePackage{...} instead of \usepackage{...}">\usepackage[a][b]{test}</weak_warning>
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}