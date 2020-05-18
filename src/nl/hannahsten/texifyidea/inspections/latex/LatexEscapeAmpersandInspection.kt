package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.isComment
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Johannes Berger
 */
class LatexEscapeAmpersandInspection : TexifyRegexInspection(
        inspectionDisplayName = "Unescaped & character",
        inspectionId = "EscapeAmpersand",
        errorMessage = { """Escape character \ expected""" },
        highlight = ProblemHighlightType.WARNING,
        pattern = Pattern.compile("""(?<!\\)&"""),
        replacement = { _, _ -> """\&""" },
        quickFixName = { """Change to \&""" }
) {
    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.isAmpersandAllowed()) return false
        return super.checkContext(matcher, element)
    }

    private fun PsiElement.isAmpersandAllowed(): Boolean {
        if (this.isComment()) return true
        if (this.firstParentOfType(LatexEnvironment::class)?.environmentName in Magic.Environment.tableEnvironments) return true
        if (this.firstParentOfType(LatexEnvironment::class)?.environmentName in alignableEnvironments) return true
        if (this.firstParentOfType(LatexCommands::class)?.name in Magic.Command.urls) return true
        return false
    }

    companion object {
        val alignableEnvironments = setOf(
                "eqnarray", "eqnarray*",
                "split",
                "align", "align*",
                "alignat", "alignat*",
                "flalign", "flalign*",
                "aligned", "alignedat",
                "cases", "dcases",
                "smallmatrix", "smallmatrix*",
                "matrix", "matrix*",
                "pmatrix", "pmatrix*")
    }
}