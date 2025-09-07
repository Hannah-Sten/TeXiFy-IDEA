package nl.hannahsten.texifyidea.inspections.latex.codestyle

import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexUsePackageInPackageInspectionQuickFixTest : TexifyInspectionTestBase(LatexUsePackageInPackageInspection()) {

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

// TODO: Enable again when it is re-implemented.
//
//    override val successfulMatches: List<String> = listOf(
//        """\usepackage{xcolor, twee}""",
//        """\usepackage[colorlinks]{hyperref}""",
//        """\usepackage[aaa, bbb]{test}""",
//        """\usepackage[options]{packagename}[version]""",
//        """\usepackage{name}[version]"""
//    )
//
//    override val failingMatches: List<String> = listOf(
//        """\usepackage[a][b]{test}"""
//    )
}