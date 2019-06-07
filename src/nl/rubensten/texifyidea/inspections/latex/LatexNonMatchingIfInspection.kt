package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.commandsInFile
import nl.rubensten.texifyidea.util.matches
import java.util.*

/**
 * @author Ruben Schellekens
 */
open class LatexNonMatchingIfInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Open if-then-else control sequence"

    override val inspectionId = "NonMatchingIf"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        // Find matches.
        val stack = ArrayDeque<LatexCommands>()
        val commands = file.commandsInFile().sortedBy { it.textOffset }
        for (command in commands) {
            val name = command.name
            if (command.name in Magic.Command.endIfs) {
                // Non-opened fi.
                if (stack.isEmpty()) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            "No matching \\if-command found",
                            Magic.General.noQuickFix,
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly
                    ))
                    continue
                }

                stack.pop()
            }
            else if (Magic.Pattern.ifCommand.matches(name) && name !in Magic.Command.ignoredIfs) {
                stack.push(command)
            }
        }

        // Mark unclosed ifs.
        for (cmd in stack) {
            descriptors.add(manager.createProblemDescriptor(
                    cmd,
                    "If statement is not closed",
                    Magic.General.noQuickFix,
                    ProblemHighlightType.GENERIC_ERROR,
                    isOntheFly
            ))
        }

        return descriptors
    }
}