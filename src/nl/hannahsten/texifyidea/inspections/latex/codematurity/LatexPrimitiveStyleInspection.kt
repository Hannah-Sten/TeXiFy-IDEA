package nl.hannahsten.texifyidea.inspections.latex.codematurity

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import org.jetbrains.annotations.Nls

/**
 * @author Hannah Schellekens
 */
class LatexPrimitiveStyleInspection : TexifyInspectionBase() {

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    @Nls
    override fun getDisplayName(): String {
        return "Discouraged use of TeX styling primitives"
    }

    override val inspectionId: String
        get() = "PrimitiveStyle"

    override fun inspectFile(
        file: PsiFile,
        manager: InspectionManager,
        isOntheFly: Boolean
    ): List<ProblemDescriptor> {
        val descriptors = mutableListOf<ProblemDescriptor>()
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            val index = CommandMagic.stylePrimitives.indexOf(command.name)
            if (index < 0) {
                continue
            }
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    "Use of TeX primitive " + CommandMagic.stylePrimitives[index] + " is discouraged",
                    InspectionFix(SmartPointerManager.createPointer(command)),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }
        return descriptors
    }

    private inner class InspectionFix(val oldCommand: SmartPsiElementPointer<LatexCommands>) : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String {
            return "Convert to LaTeX alternative"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = oldCommand.element ?: return

            element.setName(CommandMagic.stylePrimitiveReplacements[element.name] ?: return)
        }
    }
}