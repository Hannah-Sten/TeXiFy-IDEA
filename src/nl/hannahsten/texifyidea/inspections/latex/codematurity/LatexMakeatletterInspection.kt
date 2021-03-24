package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Best would be if we could detect when the usage of \makeatletter/\makeatother is unnecessary, but this is practically impossible.
 *
 * @author Hannah Schellekens
 */
open class LatexMakeatletterInspection : TexifyRegexInspection(
    inspectionDisplayName = "Discouraged use of \\makeatletter in tex sources",
    inspectionId = "Makeatletter",
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    errorMessage = { "${it.group(1)} should only be used when necessary" },
    pattern = Pattern.compile("(\\\\makeatletter|\\\\makeatother)"),
    replacement = { _, _ -> "" },
    quickFixName = { "Remove command" }
) {

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        val file = element.containingFile
        val extension = file.virtualFile.extension
        return extension?.toLowerCase() == "tex" && super.checkContext(matcher, element)
    }
}