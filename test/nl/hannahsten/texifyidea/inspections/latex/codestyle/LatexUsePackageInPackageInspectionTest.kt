package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.TexifyRegexInspectionTestBase

class LatexUsePackageInPackageInspectionRegexTest : TexifyRegexInspectionTestBase(LatexUsePackageInPackageInspection()) {

    override val successfulMatches: List<String> = listOf(
        """\usepackage{xcolor, twee}""",
        """\usepackage[colorlinks]{hyperref}""",
        """\usepackage[aaa, bbb]{test}""",
        """\usepackage[options]{packagename}[version]""",
        """\usepackage{name}[version]"""
    )

    override val failingMatches: List<String> = listOf(
        """\usepackage[a][b]{test}"""
    )
}

class LatexUsePackageInPackageInspectionQuickFixTest : TexifyInspectionTestBase(LatexUsePackageInPackageInspection()) {

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
}