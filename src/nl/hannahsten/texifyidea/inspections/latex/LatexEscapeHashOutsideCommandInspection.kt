package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.isCommandDefinition
import nl.hannahsten.texifyidea.util.parentsOfType
import java.util.regex.Pattern

class LatexEscapeHashOutsideCommandInspection : TexifyRegexInspection(
        inspectionDisplayName = "Unescaped # outside of command definition",
        inspectionId = "EscapeHashOutsideCommand",
        pattern = Pattern.compile("""(?<!\\)#"""),
        errorMessage = { "unescaped #" },
        quickFixName = { "escape #" },
        replacement = { _, _ -> """\#""" }
) {
    override fun checkContext(element: PsiElement): Boolean {
        return element.parentsOfType<LatexCommands>().all { !it.isCommandDefinition() }
    }
}