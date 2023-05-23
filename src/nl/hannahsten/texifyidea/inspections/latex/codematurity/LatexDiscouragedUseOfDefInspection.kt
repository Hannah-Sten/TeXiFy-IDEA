package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameter
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.psi.firstChildOfType
import nl.hannahsten.texifyidea.util.psi.nextSiblingIgnoreWhitespace
import java.util.*
import kotlin.math.max

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Hannah Schellekens
 */
open class LatexDiscouragedUseOfDefInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "DiscouragedUseOfDef"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Use \\(re)newcommand instead of \\let and \\def"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()
        for (cmd in commands) {
            // Only consider \let and \def.
            if (cmd.name == "\\let" || cmd.name == "\\def") {
                descriptors.add(
                    manager.createProblemDescriptor(
                        cmd,
                        TextRange(0, cmd.textLength),
                        "The use of TeX primitive ${cmd.name} is discouraged",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        NewcommandFix(),
                        NewcommandFix("\\renewcommand")
                    )
                )
            }
        }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private open class NewcommandFix(val commandName: String) : LocalQuickFix {

        constructor() : this("\\newcommand")

        override fun getFamilyName() = "Convert to $commandName"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val (cmd, value) = getArguments(command) ?: return

            val startOFfset = command.textOffset
            val endOffset = max(cmd.textOffset + cmd.textLength, value.textOffset + value.textLength)

            val valueText = if (value.text.startsWith("{") && value.text.endsWith("}")) value.text.drop(1).dropLast(1) else value.text

            document.replaceString(startOFfset, endOffset, "$commandName{${cmd.text}}{$valueText}")
        }

        open fun getArguments(command: LatexCommands): Pair<PsiElement, PsiElement>? {
            val parent = command.parent
            val definedCommand = parent.nextSiblingIgnoreWhitespace()?.firstChildOfType(LatexCommands::class) ?: return null
            // Either the definition is wrapped in braces, in which case it will be parsed as a parameter of the command being defined, or it is a sibling of the command
            val value = definedCommand.firstChildOfType(LatexParameter::class)
                ?: definedCommand.nextSiblingIgnoreWhitespace()
                ?: definedCommand.parent.nextSiblingIgnoreWhitespace()
                ?: return null
            return Pair(definedCommand.commandToken, value)
        }
    }
}
