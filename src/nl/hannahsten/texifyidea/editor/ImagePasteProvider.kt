package nl.hannahsten.texifyidea.editor

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.SaveImageFromClipboardDialog
import nl.hannahsten.texifyidea.util.files.isLatexFile
import java.awt.datatransfer.DataFlavor
import javax.swing.text.DefaultEditorKit

/**
 * When pasting image files: do the same as dragging and dropping an image file.
 * When pasting images: save the image.
 *
 * @author Hannah Schellekens
 */
open class ImagePasteProvider : PasteProvider {

    override fun performPaste(dataContext: DataContext) {
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardTransferable = dataContext.getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return

        SaveImageFromClipboardDialog(project, clipboardTransferable) {
            println("Closed!")
        }
    }

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false

        val transferable = dataContext.getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return false
        return transferable.isDataFlavorSupported(DataFlavor.imageFlavor)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)
}