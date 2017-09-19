package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.inspections.InspectionGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexPsiUtil
import nl.rubensten.texifyidea.util.document
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * For now, only not using it before `\ref` or `\cite` will be detected.
 *
 * @author Ruben Schellekens
 */
open class LatexDiscouragedUseOfDefInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InspectionGroup.LATEX

    override fun getDisplayName() = "Use \\(re)newcommand instead of \\let and \\def"

    override fun getInspectionId() = "DiscouragedUseOfDef"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = LatexCommandsIndex.getIndexedCommands(file)
        for (cmd in commands) {
            // Only consider \let and \def.
            if (cmd.name == "\\let" || cmd.name == "\\def") {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        TextRange(0, cmd.textLength),
                        "The use of TeX primitive ${cmd.name} is discouraged",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        NewcommandFix(),
                        NewcommandFix("\\renewcommand")
                ))

                // TODO: Determine new/renew based on context.
            }
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class NewcommandFix(val commandName: String) : LocalQuickFix {

        constructor() : this("\\newcommand")

        override fun getFamilyName(): String {
            return "Convert to $commandName"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val (cmd, value) = getArguments(command) ?: return

            val startOFfset = command.textOffset
            val endOffset = Math.max(cmd.textOffset + cmd.textLength, value.textOffset + value.textLength)

            document.replaceString(startOFfset, endOffset, "$commandName{${cmd.text}}{${value.text}}")
        }

        open fun getArguments(command: LatexCommands): Pair<PsiElement, PsiElement>? {
            val parent = command.parent.parent
            val firstSib = LatexPsiUtil.getNextSiblingIgnoreWhitespace(parent) ?: return null
            val secondSib = LatexPsiUtil.getNextSiblingIgnoreWhitespace(firstSib) ?: return null
            return Pair(firstSib, secondSib)
        }
    }
}