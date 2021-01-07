package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.inplace.MemberInplaceRenamer
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.endOffset
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.openedEditor
import nl.hannahsten.texifyidea.util.formatAsLabel

open class LatexAddLabelToEnvironmentIntention(val environment: SmartPsiElementPointer<LatexEnvironment>? = null) :
    LatexAddLabelIntention() {
    private class LabelInplaceRenamer(
        elementToRename: PsiNamedElement,
        editor: Editor,
        private val prefix: String,
        initialName: String,
        private val endMarker: RangeMarker
    ) : MemberInplaceRenamer(elementToRename, null, editor, initialName, initialName) {

        override fun getRangeToRename(element: PsiElement): TextRange {
            // The label prefix is given by convention and not renamed
            return TextRange(prefix.length, element.textLength)
        }

        override fun createRenameProcessor(element: PsiElement?, newName: String?): RenameProcessor {
            // Automatically prepend the prefix
            return super.createRenameProcessor(element, "$prefix$newName")
        }

        override fun moveOffsetAfter(success: Boolean) {
            super.moveOffsetAfter(success)
            myEditor.caretModel.moveToOffset(endMarker.endOffset)
        }

        override fun restoreCaretOffsetAfterRename() {
            // Dispose the marker like the parent method does, but do not move the caret. We already moved it in
            // moveOffsetAfter
            if (myBeforeRevert != null) {
                myBeforeRevert.dispose()
            }
        }
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (file?.isLatexFile() == false) {
            return false
        }

        return findTarget<LatexEnvironment>(editor, file)?.environmentName in Magic.Environment.labeled
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) {
            return
        }

        val environment = this.environment?.element
            ?: findTarget(editor, file)
            ?: return

        val helper = LatexPsiHelper(project)
        // Determine label name.
        val prefix = Magic.Environment.labeled[environment.environmentName] ?: ""
        val createdLabel = getUniqueLabelName(
            environment.environmentName.formatAsLabel(),
            prefix, environment.containingFile
        )

        val openedEditor = environment.containingFile.openedEditor() ?: return

        if (Magic.Environment.labelAsParameter.contains(environment.environmentName)) {
            val param = helper.setOptionalParameter(environment.beginCommand, "label", "{$createdLabel}")
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(openedEditor.document)
            val parameterText =
                param.keyvalValue!!.keyvalContentList.first().parameterGroup!!.parameterGroupText!!.parameterTextList.first()
            openedEditor.caretModel.moveToOffset(parameterText.textOffset + prefix.length + 1)
            val prefixText = "$prefix:"
            val onlyName = createdLabel.substring(prefixText.length)

            // We need to store the end position in a marker because the rename changes the document and, therefore,
            // the offsets. The marker updates itself automatically
            val endMarker =
                openedEditor.document.createRangeMarker(environment.startOffset, environment.endOffset())
            val renamer = LabelInplaceRenamer(parameterText!!, openedEditor, prefixText, onlyName, endMarker)
            renamer.performInplaceRefactoring(LinkedHashSet())
        }
        else {
            // in a float environment the label must be inserted after a caption
            val labelCommand = helper.addToContent(
                environment, helper.createLabelCommand(createdLabel),
                environment.environmentContent?.childrenOfType<LatexCommands>()
                    ?.findLast { c -> c.name == "\\caption" }
            )

            // Adjust caret offset
            val caretModel = openedEditor.caretModel
            caretModel.moveToOffset(labelCommand.endOffset())
        }

    }

}