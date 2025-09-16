package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.parentOfType

class LatexEscapeHashOutsideCommandInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EscapeHashOutsideCommand",
    regex = Regex("""(?<!\\)#"""),
    inspectionGroup = InsightGroup.LATEX
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String {
        return "Unescaped #"
    }

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String {
        return "Escape #"
    }

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String {
        return "\\#"
    }

    override fun additionalChecks(element: PsiElement, match: MatchResult, bundle: DefinitionBundle, file: PsiFile): Boolean {
        return !LatexPsiUtil.isInsideDefinition(element, bundle)
    }

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        if (!super.shouldInspectElement(element, lookup)) return false
        // Do not inspect inside command definitions/redefinitions
        // Do not inspect inside URL-like commands
        val parentCmd = element.parentOfType(LatexCommands::class)
        return parentCmd?.name !in CommandMagic.urls
    }
}
