package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.parentsOfType
import nl.hannahsten.texifyidea.util.parser.isDefinitionOrRedefinition

class LatexEscapeHashOutsideCommandInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EscapeHashOutsideCommand",
    regex = Regex("""(?<!\\)#"""),
    inspectionGroup = InsightGroup.LATEX
) {
    override fun errorMessage(matcher: MatchResult): String {
        return "Unescaped #"
    }

    override fun quickFixName(matcher: MatchResult): String {
        return "Escape #"
    }

    override fun getReplacement(match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\#"
    }

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        if (!super.shouldInspectElement(element, lookup)) return false
        // Do not inspect inside command definitions/redefinitions
        // TODO: improve
        if (element.parentsOfType<LatexCommands>().any { it.isDefinitionOrRedefinition() }) return false
        // Do not inspect inside URL-like commands
        val parentCmd = element.parentOfType(LatexCommands::class)
        if (parentCmd?.name in CommandMagic.urls) return false
        return true
    }
}
