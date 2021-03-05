package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.LatexCommandsIndex
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.util.deleteElement
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.hannahsten.texifyidea.util.parentOfType
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
            val index = CommandMagic.stylePrimitives.indexOf(command.name)
            if (index < 0) {
                continue
            }
            descriptors.add(
                manager.createProblemDescriptor(
                    command,
                    "Use of TeX primitive " + CommandMagic.stylePrimitives[index] + " is discouraged",
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
            val cmdIndex = CommandMagic.stylePrimitives.indexOf(element.name)
            if (cmdIndex < 0) {
                return
            }
            val content = element.parentOfType(LatexNoMathContent::class)
            val next = content!!.nextSiblingIgnoreWhitespace()
            val after = if (next == null) "" else next.text
            val replacement =
                String.format(CommandMagic.stylePrimitveReplacements[cmdIndex], after)
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