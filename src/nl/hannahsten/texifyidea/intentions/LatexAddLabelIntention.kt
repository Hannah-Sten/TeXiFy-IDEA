package nl.hannahsten.texifyidea.intentions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.inplace.MemberInplaceRenamer
import nl.hannahsten.texifyidea.psi.LatexCommandWithParams
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.labels.Labels
import kotlin.math.max

abstract class LatexAddLabelIntention(name: String) : TexifyIntentionBase(name) {

    /**
     * This class handles the rename of a label parameter after insertion
     */
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

    protected inline fun <reified T : PsiElement> findTarget(editor: Editor?, file: PsiFile?): T? {
        val offset = editor?.caretModel?.offset ?: return null
        val element = file?.findElementAt(offset) ?: return null
        // Also check one position back, because we want it to trigger in \section{a}<caret>
        return element as? T ?: element.parentOfType()
            ?: file.findElementAt(max(0, offset - 1)) as? T
            ?: file.findElementAt(max(0, offset - 1))?.parentOfType()
    }

    protected fun getUniqueLabelWithPrefix(base: String, prefix: String, file: PsiFile): LabelWithPrefix {
        val labelBase = "$prefix:$base"
        val fullLabel = Labels.getUniqueLabelName(labelBase, file)
        return LabelWithPrefix(prefix, fullLabel.substring(prefix.length + 1))
    }

    data class LabelWithPrefix(val prefix: String, val base: String) {

        val prefixText = "$prefix:"
        val labelText = "$prefix:$base"
    }

    protected fun createLabelAndStartRename(
        editor: Editor,
        project: Project,
        command: LatexCommandWithParams,
        label: LabelWithPrefix,
        moveCaretTo: RangeMarker
    ) {
        val helper = LatexPsiHelper(project)
        val parameter = helper.setOptionalParameter(command, "label", "{${label.labelText}}")

        if (parameter == null) {
            Notification("LaTeX", "Could not add label", "Something went wrong while trying to add the label ${label.labelText} to command ${command.getName()}. Please try again", NotificationType.WARNING).notify(project)
            return
        }

        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)

        // setOptionalParameter should create an appropriate optionalArgument node with label={text} in it
        val parameterText =
            parameter.keyValValue?.keyValContentList?.firstOrNull()?.parameterGroup?.parameterGroupText?.parameterTextList?.firstOrNull()
                ?: throw AssertionError("parameter created by setOptionalParameter for $command with text ${label.labelText} does not have the right structure: ${parameter.text}")
        // Move the caret onto the label
        editor.caretModel.moveToOffset(parameterText.textOffset + label.prefix.length + 1)
        val renamer = LabelInplaceRenamer(parameterText, editor, label.prefixText, label.base, moveCaretTo)
        renamer.performInplaceRefactoring(LinkedHashSet())
    }

    override fun startInWriteAction() = true

    // Not clear to me why the default implementation does not work, but this avoids the "Intention preview fallback is used for action" error
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        invoke(project, editor, file)
        return IntentionPreviewInfo.DIFF
    }
}