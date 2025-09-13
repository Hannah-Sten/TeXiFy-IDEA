package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.inspections.createDescriptor
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.nextSiblingIgnoreWhitespace
import java.util.*
import kotlin.math.max

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Hannah Schellekens
 */
class LatexDiscouragedUseOfDefInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "DiscouragedUseOfDef",
) {

    override fun getDisplayName() = "Use \\(re)newcommand instead of \\let and \\def"

    override fun inspectCommand(command: LatexCommands, contexts: LContextSet, lookup: LatexSemanticsLookup, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        val name = command.nameWithoutSlash ?: return
        if (name != "let" && name != "def") return
        val descriptor = manager.createDescriptor(
            command,
            "The use of TeX primitive \\$name is discouraged",
            rangeInElement = TextRange(0, command.textLength),
            isOnTheFly = isOnTheFly,
            highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            fixes = arrayOf(NewcommandFix(), NewcommandFix("\\renewcommand"))
        )
        descriptors.add(descriptor)
    }

    /**
     * @author Hannah Schellekens
     */
    private class NewcommandFix(val commandName: String) : LocalQuickFix {

        constructor() : this("\\newcommand")

        override fun getFamilyName() = "Convert to $commandName"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val (cmd, nArgs, value) = getArguments(command) ?: return

            val startOFfset = command.textOffset
            val endOffset = max(cmd.textOffset + cmd.textLength, value.textOffset + value.textLength)

            val numberArgumentsText = nArgs?.let { "[$nArgs]" } ?: ""
            val valueText = if (value.text.startsWith("{") && value.text.endsWith("}")) value.text.drop(1).dropLast(1) else value.text

            document.replaceString(startOFfset, endOffset, "$commandName{${cmd.text}}$numberArgumentsText{$valueText}")
        }

        fun getArguments(command: LatexCommands): Triple<PsiElement, Int?, PsiElement>? {
            val parent = command.parent
            val definedCommand = parent.nextSiblingIgnoreWhitespace()?.findFirstChildOfType(LatexCommands::class) ?: return null
            // Either the definition is wrapped in braces, in which case it will be parsed as a parameter of the command being defined, or it is a sibling of the command
            val value = definedCommand.findFirstChildOfType(LatexParameter::class)
                ?: definedCommand.nextSiblingIgnoreWhitespace()
                ?: definedCommand.parent.nextSiblingIgnoreWhitespace()
                ?: return null
            if (value.text.matches(Regex("""(#\d)+#?"""))) {
                val numberOfArguments = Regex("""#\d""").findAll(value.text).count()
                val bracedValue = value.nextSiblingIgnoreWhitespace() ?: return null
                return Triple(definedCommand.commandToken, numberOfArguments, bracedValue)
            }
            else {
                return Triple(definedCommand.commandToken, null, value)
            }
        }
    }
}
