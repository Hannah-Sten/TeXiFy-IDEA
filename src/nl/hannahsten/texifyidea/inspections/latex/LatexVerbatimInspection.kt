package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.formatting.blocks.prev
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.ui.CreateFileDialog
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.createFile
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.openedEditor
import java.io.File

class LatexVerbatimInspection : TexifyInspectionBase() {
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

    override val inspectionId: String = "UseOfVerbatimEnvironment"

    private val onTag = CodeStyleSettings.getDefaults().FORMATTER_ON_TAG
    private val offTag = CodeStyleSettings.getDefaults().FORMATTER_OFF_TAG

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val begins = file.childrenOfType(LatexBeginCommand::class)
        for (begin in begins) {
            val beginEnvironment = begin.environmentName() ?: continue

            if (Magic.Environment.verbatim.any { it == beginEnvironment }) {
                // Don't trigger the inspection when the verbatim environment is in its own file.
                if (Magic.Environment.verbatim.any { file.text.startsWith("\\begin{$it}") }) continue
                // Don't trigger the inspection when the verbatim environment is surrounded by formatter comments.
                if (begin.node.treeParent.prev()?.text?.contains(offTag) == true) continue

                descriptors.add(manager.createProblemDescriptor(
                        begin,
                        begin,
                        "Verbatim environment might break TeXiFy formatter and/or parser",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InsertFormatterCommentsFix(onTag, offTag),
                        MoveToFileFix()
                ))
            }
        }

        return descriptors
    }

    class InsertFormatterCommentsFix(val onTag: String, val offTag: String) : LocalQuickFix {
        override fun getFamilyName(): String =
                "Insert comments to disable the formatter (fixes formatter issues)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val beginCommand = descriptor.startElement as LatexBeginCommand
            val endCommand = beginCommand.endCommand() ?: return

            val editor = beginCommand.containingFile.openedEditor() ?: return
            val indent: String = editor.document.lineIndentationByOffset(beginCommand.textOffset)
            val offComment = "% $offTag\n$indent"
            val onComment = "\n$indent% $onTag"

            editor.insertAndMove(beginCommand.textOffset, offComment)
            editor.insertAndMove(endCommand.endOffset() + offComment.length, onComment)
        }
    }

    class MoveToFileFix : LocalQuickFix {
        override fun getFamilyName(): String =
                "Move verbatim environment to another file (fixes formatter and parser issues)"

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val begin = descriptor.startElement as LatexBeginCommand
            val end = begin.endCommand() ?: return

            val file = begin.containingFile ?: return
            val editor = file.openedEditor() ?: return
            val document = editor.document

            val text = document.getText(TextRange(begin.textOffset, end.endOffset()))
            // Display a dialog to ask for the location and name of the new file.
            val filePath = CreateFileDialog(file.containingDirectory.virtualFile.canonicalPath, begin.environmentName()?.formatAsFileName() ?: "")
                    .newFileFullPath ?: return
            val root = file.findRootFile().containingDirectory.virtualFile.canonicalPath ?: return

            // Execute write actions.
            runWriteAction {
                val createdFile = createFile("$filePath.tex", text)
                document.deleteString(begin.textOffset, end.endOffset())
                val fileNameRelativeToRoot = createdFile.absolutePath
                        .replace(File.separator, "/")
                        .replace("$root/", "")
                val indent = document.lineIndentationByOffset(begin.textOffset)
                editor.insertAndMove(begin.textOffset, "\n$indent\\input{${fileNameRelativeToRoot.dropLast(4)}}\n")

            }
        }
    }
}