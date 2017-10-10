package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexMakeatletterInspection : TexifyRegexInspection(
        inspectionDisplayName = "Discouraged use of \\makeatletter in tex sources",
        myInspectionId = "Makeatletter",
        highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        errorMessage = { "${it.group(1)} shouldn't be used in tex sources" },
        pattern = Pattern.compile("(\\\\makeatletter|\\\\makeatother)"),
        replacement = { _, _ -> "" },
        quickFixName = { "Remove command" }
) {

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        val file = element.containingFile
        val extension = file.virtualFile.extension
        return extension?.toLowerCase() == "tex"
    }
}