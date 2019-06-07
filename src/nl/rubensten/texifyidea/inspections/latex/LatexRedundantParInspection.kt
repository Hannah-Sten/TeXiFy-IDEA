package nl.rubensten.texifyidea.inspections.latex

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.rubensten.texifyidea.inspections.TexifyRegexInspection
import nl.rubensten.texifyidea.psi.LatexTypes
import nl.rubensten.texifyidea.util.toTextRange
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class LatexRedundantParInspection : TexifyRegexInspection(
        inspectionDisplayName = "Redundant use of \\par",
        inspectionId = "RedundantPar",
        errorMessage = { "Use of \\par is redundant here" },
        pattern = Pattern.compile("((\\s*\\n\\s*\\n\\s*(\\\\par))|(\\n\\s*(\\\\par)\\s*\\n)|((\\\\par)\\s*\\n\\s*\\n))"),
        replacement = { _, _ -> "" },
        replacementRange = this::parRange,
        quickFixName = { "Remove \\par" },
        highlightRange = { parRange(it).toTextRange() }
) {

    companion object {

        fun parRange(it: Matcher) = if (it.group(3) != null) {
            it.groupRange(3)
        }
        else if (it.group(5) != null) {
            it.groupRange(5)
        }
        else {
            it.groupRange(7)
        }
    }

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        val elt = element.containingFile.findElementAt(matcher.end()) as? LeafPsiElement
        return super.checkContext(matcher, element) && elt?.elementType != LatexTypes.COMMAND_TOKEN ?: true
    }
}