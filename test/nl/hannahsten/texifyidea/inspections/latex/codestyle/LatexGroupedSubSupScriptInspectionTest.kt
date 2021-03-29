package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.inspections.latex.TexifyRegexInspectionTestBase
import nl.hannahsten.texifyidea.inspections.latex.codestyle.LatexGroupedSubSupScriptInspection

/**
 * @author Hannah Schellekens
 */
class LatexGroupedSubSupScriptInspectionTest : TexifyRegexInspectionTestBase(LatexGroupedSubSupScriptInspection()) {

    override val successfulMatches = listOf(
        """hi_thisisanexpectedwarning""",
        """so^alsoexpected"""
    )

    override val failingMatches = listOf(
        """hi\_thisisnot""",
        """\string^alsonot""",
        """\char`\^alsonot""",
        """\verb!^!alsonot""",
        """\hat{}\ theusualway"""
    )
}