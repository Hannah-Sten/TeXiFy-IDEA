package nl.rubensten.texifyidea.inspections.latex

/**
 * @author Ruben Schellekens
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