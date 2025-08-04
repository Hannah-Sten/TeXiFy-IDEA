package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.inMathContext

/**
 * @author Hannah Schellekens
 */
open class LatexMathFunctionTextInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Use math function instead of \\text"

    override val inspectionId = "MathFunctionText"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        file.commandsInFile("\\text").asSequence()
            .filter { it.inMathContext() }
            .filter { it.requiredParameterText(0)?.trim() in affectedCommands }
            .forEach { affectedTextCommand ->
                descriptors.add(
                    manager.createProblemDescriptor(
                        affectedTextCommand,
                        "Use math function instead of \\text",
                        MathFunctionFix(SmartPointerManager.createPointer(affectedTextCommand)),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private class MathFunctionFix(val textCommandPointer: SmartPsiElementPointer<LatexCommands>) : LocalQuickFix {

        override fun getFamilyName() = "Convert to math function"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val textCommand = textCommandPointer.element ?: return
            val mathFunction = extractFunction(textCommand) ?: return
            textCommand.node.removeChild(textCommand.parameterList[0].node)
            val newCmdToken = LatexPsiHelper(project).createFromText(mathFunction).firstChild.findFirstChildTyped<LatexCommands>()?.commandToken ?: return
            textCommand.node.replaceChild(textCommand.commandToken.node, newCmdToken.node)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            val textCommand = textCommandPointer.element ?: return IntentionPreviewInfo.EMPTY
            val functionText = extractFunction(textCommand) ?: return IntentionPreviewInfo.EMPTY
            return IntentionPreviewInfo.CustomDiff(LatexFileType, textCommand.text, functionText)
        }

        private fun extractFunction(textCommandElement: LatexCommands): String? {
            return textCommandElement.requiredParameterText(0)?.trim()?.let { "\\$it" }
        }
    }

    private val affectedCommands = CommandMagic.slashlessMathOperators.asSequence()
        .map { it.command }
        .toSet()
}