package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.TexifyUtil
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.replaceString

/**
 * @author Sten Wessel
 */
class LatexCommandAlreadyDefinedInspection : TexifyInspectionBase() {

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Command is already defined"

    override fun getInspectionId() = "CommandAlreadyDefined"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = LatexCommandsIndex.getItems(file)

        for (command in commands) {
            // Error when \newcommand is used on existing command
            if ("\\newcommand" == command.name) {
                val newCommand = TexifyUtil.getForcedFirstRequiredParameterAsCommand(command) ?: continue

                if (TexifyUtil.isCommandKnown(newCommand)) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            "Command is already defined",
                            true,
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly, RenewCommandFix
                    ))
                }
            }
            // Warning when a builtin command gets overridden
            else if ("\\def" == command.name || "\\let" == command.name) {
                val newCommand = TexifyUtil.getForcedFirstRequiredParameterAsCommand(command) ?: continue

                if (TexifyUtil.isCommandKnown(newCommand)) {
                    descriptors.add(manager.createProblemDescriptor(
                            command,
                            "Command is already defined",
                            true,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                    ))
                }
            }

        }

        return descriptors
    }

    object RenewCommandFix : LocalQuickFix {

        override fun getFamilyName() = "Convert to \\renewcommand"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? LatexCommands ?: return

            val range = element.commandToken.textRange

            val document = element.containingFile.document() ?: return

            document.replaceString(range, "\\renewcommand")
        }
    }
}
