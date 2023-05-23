package nl.hannahsten.texifyidea.inspections.latex.codestyle

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.psi.inMathContext
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.psi.requiredParameter
import nl.hannahsten.texifyidea.util.psi.requiredParameters

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
            .filter { it.requiredParameter(0)?.trim() in AFFECTED_COMMANDS }
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
            val document = textCommand.containingFile.document() ?: return
            val mathFunction = textCommand.requiredParameter(0)?.trim() ?: return
            document.replaceString(textCommand.startOffset, textCommand.requiredParameters()[0].endOffset, "\\$mathFunction")
        }
    }

    companion object {

        private val AFFECTED_COMMANDS = CommandMagic.slashlessMathOperators.asSequence()
            .map { it.command }
            .toSet()
    }
}