package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.inDirectEnvironment
import nl.hannahsten.texifyidea.util.isComment
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Johannes Berger
 */
class LatexEscapeAmpersandInspection : TexifyRegexInspection(
    inspectionDisplayName = "Unescaped & character",
    inspectionId = "EscapeAmpersand",
    errorMessage = { """Escape character \ expected""" },
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
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
        if (this.inDirectEnvironment(EnvironmentMagic.getAllTableEnvironments(project))) return true
        if (this.inDirectEnvironment(EnvironmentMagic.alignableEnvironments)) return true

        // Other exceptions
        val command = this.firstParentOfType(LatexCommands::class)?.name
        return command in CommandMagic.urls ||
            command in project.getLabelReferenceCommands() ||
            command in project.getLabelDefinitionCommands()
    }
}