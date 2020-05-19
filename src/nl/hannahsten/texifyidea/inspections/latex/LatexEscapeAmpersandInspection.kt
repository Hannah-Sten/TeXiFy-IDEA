package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.*
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
        if (element.isComment()) return false
        return checkContext(element)
    }

    private fun PsiElement.isAmpersandAllowed(): Boolean {
        // Do not trigger inside comments.
        if (this.isComment()) return true

        // Do not trigger in environments that use the ampersand as special character.
        val invalidEnvironments = Magic.Environment.tableEnvironments + Magic.Environment.alignableEnvironments
        if (this.inDirectEnvironment(invalidEnvironments)) return true

        // Do not trigger in URLs.
        if (this.firstParentOfType(LatexCommands::class)?.name in Magic.Command.urls) return true

        return false
    }
}