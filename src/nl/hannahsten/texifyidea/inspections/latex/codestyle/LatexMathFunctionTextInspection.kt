package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.AbstractTexifyCommandBasedInspection
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.psi.nameWithoutSlash

/**
 * @author Hannah Schellekens
 */
class LatexMathFunctionTextInspection : AbstractTexifyCommandBasedInspection(
    inspectionId = "MathFunctionText",
    applicableContexts = setOf(LatexContexts.Math)
) {

    private val affectedCommands = CommandMagic.slashlessMathOperators.asSequence()
        .map { it.command }
        .toSet()

    override fun getDisplayName() = "Use math function instead of \\text"

    override fun inspectCommand(
        command: LatexCommands,
        contexts: LContextSet,
        defBundle: DefinitionBundle,
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    ) {
        val name = command.nameWithoutSlash ?: return
        if (name != "text") return
        if (!isApplicableInContexts(contexts)) return
        val param = command.requiredParameterText(0)?.trim() ?: return
        if (param !in affectedCommands) return
        descriptors.add(
            manager.createProblemDescriptor(
                command,
                "Use math function instead of \\text",
                MathFunctionFix(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly
            )
        )
    }

    private class MathFunctionFix : LocalQuickFix {
        override fun getFamilyName() = "Convert to math function"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textCommand = descriptor.psiElement as? LatexCommands ?: return
            val mathFunction = extractFunction(textCommand) ?: return
            if (textCommand.parameterList.isEmpty()) return
            textCommand.node.removeChild(textCommand.parameterList[0].node)
            val newCmdToken = LatexPsiHelper(project).createFromText(mathFunction).firstChild.findFirstChildTyped<LatexCommands>()?.commandToken ?: return
            textCommand.node.replaceChild(textCommand.commandToken.node, newCmdToken.node)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            val textCommand = previewDescriptor.psiElement as? LatexCommands ?: return IntentionPreviewInfo.EMPTY
            val functionText = extractFunction(textCommand) ?: return IntentionPreviewInfo.EMPTY
            return IntentionPreviewInfo.CustomDiff(LatexFileType, textCommand.text, functionText)
        }

        private fun extractFunction(textCommandElement: LatexCommands): String? {
            return textCommandElement.requiredParameterText(0)?.trim()?.let { "\\$it" }
        }
    }
}