package nl.hannahsten.texifyidea.editor

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.file.SaveImageFromClipboardDialog
import nl.hannahsten.texifyidea.util.files.extractFile
import nl.hannahsten.texifyidea.util.files.isLatexFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 * When pasting image files: do the same as dragging and dropping an image file.
 * When pasting images: save the image.
 *
 * @author Hannah Schellekens
 */
open class ImagePasteProvider : PasteProvider {

    override fun performPaste(dataContext: DataContext) {
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val clipboardTransferable = dataContext.getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return

        // When pasting some copied image.
        if (SaveImageFromClipboardDialog.supportsImage(clipboardTransferable)) {
            pasteRawImage(project, file, clipboardTransferable)
        }
        // When pasting an existing image file.
        else if (clipboardTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            pasteImageFile(project, file, clipboardTransferable)
        }
    }

    private fun pasteRawImage(project: Project, file: VirtualFile, clipboard: Transferable) {
        SaveImageFromClipboardDialog(project, clipboard) {
            it.savedImage?.let { imageFile ->
                InsertGraphicWizardAction(imageFile).executeAction(file, project)
            }
        }
    }

    private fun pasteImageFile(project: Project, file: VirtualFile, clipboard: Transferable) {
        val clipboardFile = clipboard.extractFile() ?: return
        InsertGraphicWizardAction(clipboardFile).executeAction(file, project)
    }

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false

        val transferable = dataContext.getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return false
        return SaveImageFromClipboardDialog.supportsImage(transferable) ||
            transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)
}