package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.isDefinitionOrRedefinition
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.parentsOfType
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
        return super.checkContext(element) && element.parentsOfType<LatexCommands>().all { !it.isDefinitionOrRedefinition() } && element.parentOfType(LatexCommands::class)?.name !in CommandMagic.urls
    }
}
