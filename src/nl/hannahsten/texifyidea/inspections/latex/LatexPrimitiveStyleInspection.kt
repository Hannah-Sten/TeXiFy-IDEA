package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexMathContent
import nl.hannahsten.texifyidea.psi.LatexPsiUtil.getNextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.deleteElement
import org.jetbrains.annotations.Nls
import kotlin.reflect.jvm.internal.impl.utils.SmartList

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
        val descriptors: MutableList<ProblemDescriptor> =
            SmartList()
        val commands = LatexCommandsIndex.getItems(file)
        for (command in commands) {
            val index = Magic.Command.stylePrimitives.indexOf(command.name)
            if (index < 0) {
                continue
            }
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    "Use of TeX primitive " + Magic.Command.stylePrimitives[index] + " is discouraged",
                    InspectionFix(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOntheFly
                )
            )
        }
        return descriptors
    }

    private inner class InspectionFix : LocalQuickFix {

        @Nls
        override fun getFamilyName(): String {
            return "Convert to LaTeX alternative"
        }

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val element = descriptor.psiElement as? LatexCommands ?: return

            // Find elements that go after the primitive.
            val cmdIndex = Magic.Command.stylePrimitives.indexOf(element.name)
            if (cmdIndex < 0) {
                return
            }
            var content = element.parent.parent
            if (content is LatexMathContent) {
                content = element.parent
            }
            val next = getNextSiblingIgnoreWhitespace(content!!)
            val after = if (next == null) "" else next.text
            val replacement =
                String.format(Magic.Command.stylePrimitveReplacements[cmdIndex], after)
            val document =
                PsiDocumentManager.getInstance(project).getDocument(element.containingFile)

            // Delete the ending part..
            if (next != null) {
                document!!.deleteElement(next)
            }

            // Replace command.
            val range = element.commandToken.textRange
            document?.replaceString(range.startOffset, range.endOffset, replacement)
        }
    }
}