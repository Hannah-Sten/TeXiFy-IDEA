package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.inspections.AbstractTexifyRegexBasedInspection
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class LatexEscapeHashOutsideCommandInspection : AbstractTexifyRegexBasedInspection(
    inspectionId = "EscapeHashOutsideCommand",
    regex = Regex("""(?<!\\)#"""),
    inspectionGroup = InsightGroup.LATEX,
    excludedContexts = setOf(LatexContexts.InsideDefinition, LatexContexts.URL)
) {
    override fun errorMessage(matcher: MatchResult, context: LContextSet): String = "Unescaped #"

    override fun quickFixName(matcher: MatchResult, contexts: LContextSet): String = "Escape #"

    override fun getReplacement(match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor): String = "\\#"

    override fun additionalChecks(element: PsiElement, match: MatchResult, bundle: DefinitionBundle, file: PsiFile): Boolean = !LatexPsiUtil.isInsideDefinition(element, bundle)
}