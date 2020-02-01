package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.formatting.blocks.prev
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.openedEditor

class LatexVerbatimInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "UseOfVerbatimEnvironment"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val begins = file.childrenOfType(LatexBeginCommand::class)
        for (begin in begins) {
            val beginEnvironment = begin.environmentName() ?: continue

            if (Magic.Environment.verbatim.any { it == beginEnvironment }) {
                // Don't trigger the inspection when the verbatim environment is in its own file.
                if (Magic.Environment.verbatim.any { file.text.startsWith("\\begin{$it}") }) continue
                // Don't trigger the inspection when the verbatim environment is surrounded by formatter comments.
                if (begin.node.treeParent.prev()?.text?.contains("@formatter:off") == true) continue

                descriptors.add(manager.createProblemDescriptor(
                        begin,
                        begin,
                        "Verbatim environment might break TeXiFy formatter and/or parser",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InsertFormatterCommentsFix(),
                        MoveToFileFix()
                ))
            }
        }

        return descriptors
    }

    class InsertFormatterCommentsFix : LocalQuickFix {
        override fun getFamilyName(): String =
                "Insert comments to disable the formatter (fixes formatter issues)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val beginCommand = descriptor.startElement as LatexBeginCommand
            val endCommand = beginCommand.endCommand() ?: return

            val editor = beginCommand.containingFile.openedEditor() ?: return
            val indent: String = editor.document.lineIndentationByOffset(beginCommand.textOffset)
            val offComment = "% @formatter:off\n$indent"
            val onComment = "\n$indent% @formatter:on"

            editor.insertAndMove(beginCommand.textOffset, offComment)
            editor.insertAndMove(endCommand.endOffset() + offComment.length, onComment)

        }
    }

    class MoveToFileFix : LocalQuickFix {
        override fun getFamilyName(): String =
                "Move verbatim environment to another file (fixes formatter and parser issues)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            println("move verbatim to file")
        }
    }
}