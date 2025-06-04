package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.inMathContext
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Johannes Berger
 */
class LatexEscapeUnderscoreInspection : TexifyRegexInspection(
    inspectionDisplayName = "Unescaped _ character",
    inspectionId = "EscapeUnderscore",
    errorMessage = { """Escape character \ expected""" },
    highlight = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    pattern = Pattern.compile("""(?<!\\)_"""),
    replacement = { _, _ -> """\_""" },
    quickFixName = { """Change to \_""" }
) {

    override fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.isUnderscoreAllowed()) return false
        return super.checkContext(matcher, element)
    }

    private fun PsiElement.isUnderscoreAllowed(): Boolean {
        if (this.inMathContext()) return true
        if (this.firstParentOfType(LatexNormalText::class) != null) return false
        if (this.firstParentOfType(LatexCommands::class)?.name in commandsDisallowingUnderscore) return false
        return true
    }

    private val commandsDisallowingUnderscore =
        CommandMagic.sectionNameToLevel.keys + CommandMagic.textStyles + setOf("""\caption""")
}