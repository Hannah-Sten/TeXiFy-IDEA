package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyInspectionBase
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.replaceString
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Sten Wessel
 */
open class LatexNoExtensionInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "NoExtension"

    override fun getDisplayName() = "File argument should not include the extension"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        LatexCommandsIndex.getItems(file).asSequence()
                .filter { it.name in Magic.Command.illegalExtensions }
                .filter { command ->
                    Magic.Command.illegalExtensions[command.name]!!.any { command.requiredParameter(0)?.endsWith(it) == true }
                }
                .forEach {
                    descriptors.add(manager.createProblemDescriptor(
                            it,
                            TextRange.allOf(it.requiredParameter(0)!!).shiftRight(it.commandToken.textLength + 1),
                            "File argument should not include the extension",
                            ProblemHighlightType.GENERIC_ERROR,
                            isOntheFly,
                            RemoveExtensionFix
                    ))
                }

        return descriptors
    }

    /**
     * @author Sten Wessel
     */
    object RemoveExtensionFix : LocalQuickFix {

        override fun getFamilyName() = "Remove file extension"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val document = command.containingFile.document() ?: return

            val replacement = Magic.Command.illegalExtensions[command.name]
                    ?.find { command.requiredParameter(0)?.endsWith(it) == true }
                    ?.run { command.requiredParameter(0)?.removeSuffix(this) } ?: return

            // Exclude the enclosing braces
            val range = command.parameterList.first { it.requiredParam != null }.textRange.shiftRight(1).grown(-2)

            document.replaceString(range, replacement)
        }
    }
}