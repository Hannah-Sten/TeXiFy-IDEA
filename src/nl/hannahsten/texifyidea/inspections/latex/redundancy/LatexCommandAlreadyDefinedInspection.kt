package nl.hannahsten.texifyidea.inspections.latex.redundancy

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.parser.forcedFirstRequiredParameterAsCommand
import nl.hannahsten.texifyidea.util.parser.isKnown
import nl.hannahsten.texifyidea.util.replaceString
import java.util.*

/**
 * Warns for commands that (maybe by mistake) redefine commands that are defined by LaTeX (packages).
 *
 * @author Sten Wessel
 */
class LatexCommandAlreadyDefinedInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "CommandAlreadyDefined"

    override val ignoredSuppressionScopes = EnumSet.of(
        MagicCommentScope.ENVIRONMENT,
        MagicCommentScope.MATH_ENVIRONMENT,
        MagicCommentScope.COMMAND,
        MagicCommentScope.GROUP
    )!!

    override fun getDisplayName() = "Command is already defined"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val commands = file.commandsInFile()

        for (command in commands) {
            // Error when \newcommand is used on existing command
            if ("\\newcommand" == command.name) {
                val newCommand = command.forcedFirstRequiredParameterAsCommand() ?: continue

                if (newCommand.isKnown()) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            "Command may already be defined in a LaTeX package",
                            true,
                            // Warning because may not be true: command may be defined in a LaTeX package which is not actually imported
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly, RenewCommandFix
                        )
                    )
                }
            }
            // Warning when a builtin command gets overridden
            else if ("\\def" == command.name || "\\let" == command.name) {
                val newCommand = command.forcedFirstRequiredParameterAsCommand() ?: continue

                if (newCommand.isKnown()) {
                    descriptors.add(
                        manager.createProblemDescriptor(
                            command,
                            "Command may already be defined in a LaTeX package",
                            true,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            isOntheFly
                        )
                    )
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
