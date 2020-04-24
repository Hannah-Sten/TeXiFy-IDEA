package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.isComment
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Johannes Berger
 */
class LatexEscapeUnderscoreInspection : TexifyRegexInspection(
        inspectionDisplayName = "Unescaped _ character",
        inspectionId = "LatexEscapeUnderscore",
        errorMessage = { """Escape character \ expected""" },
        highlight = ProblemHighlightType.WARNING,
        pattern = Pattern.compile("""(?<!\\)_"""),
        replacement = { _, _ -> """\_""" },
        quickFixName = { """Change to \_""" }
) {
    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.isUnderscoreAllowed()) return false
        return checkContext(element)
    }

    private fun PsiElement.isUnderscoreAllowed(): Boolean {
        if (this.isComment()) return true
        if (this.firstParentOfType(LatexCommands::class)?.name in commandsAllowingUnderscore) return true
        return false
    }

    companion object {
        val commandsAllowingUnderscore = Magic.Command.urls +
                Magic.Command.labelDefinition +
                Magic.Command.reference +
                Magic.Command.absoluteImportCommands +
                Magic.Command.relativeImportCommands +
                setOf("\\input", "\\newcommand", "\\bibliography")
    }
}