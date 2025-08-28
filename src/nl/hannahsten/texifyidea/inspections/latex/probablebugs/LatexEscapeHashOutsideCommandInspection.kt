package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.TexifyContextAwareInspectionBase
import nl.hannahsten.texifyidea.lang.LContextSet

// class LatexEscapeHashOutsideCommandInspection : TexifyRegexInspection(
//    inspectionDisplayName = "Unescaped # outside of command definition",
//    inspectionId = "EscapeHashOutsideCommand",
//    pattern = Pattern.compile("""(?<!\\)#"""),
//    errorMessage = { "unescaped #" },
//    quickFixName = { "escape #" },
//    replacement = { _, _ -> """\#""" }
// ) {
//
//    override fun checkContext(element: PsiElement): Boolean {
//        return super.checkContext(element) && element.parentsOfType<LatexCommands>().all { !it.isDefinitionOrRedefinition() } && element.parentOfType(LatexCommands::class)?.name !in CommandMagic.urls
//    }
// }

class LatexEscapeHashOutsideCommandInspection : TexifyContextAwareInspectionBase(
    inspectionId = "EscapeHashOutsideCommand",
    regex = """(?<!\\)#""".toRegex(),
) {
    override fun errorMessage(matcher: MatchResult, file: PsiFile): String {
        return "Unescaped #"
    }

    override fun replacement(matcher: MatchResult, file: PsiFile): String {
        return """\#"""
    }

    override fun quickFixName(matcher: MatchResult, file: PsiFile): String {
        return "Escape #"
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
    }
}
