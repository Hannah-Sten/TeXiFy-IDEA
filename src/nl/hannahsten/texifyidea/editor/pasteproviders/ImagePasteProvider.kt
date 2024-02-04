package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.file.SaveImageDialog
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.files.extractFile
import nl.hannahsten.texifyidea.util.files.isLatexFile
import org.apache.commons.io.FilenameUtils
import org.jsoup.Jsoup
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage

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
        if (hasRawImage(clipboardTransferable)) {
            pasteRawImage(project, file, clipboardTransferable)
        }
        // When pasting an existing image file.
        else if (clipboardTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            pasteImageFile(project, file, clipboardTransferable)
        }
    }

    private fun pasteRawImage(project: Project, file: VirtualFile, clipboard: Transferable) {
        val image = clipboard.getTransferData(DataFlavor.imageFlavor) as BufferedImage

        // Check if there is HTML image metadata present.
        val (imageName, extension) = extractMetaData(clipboard)

        SaveImageDialog(project, image, imageName, extension) { imageFile -> InsertGraphicWizardAction(imageFile).executeAction(file, project) }
    }

    /**
     * Extract HTML metadata if present.
     */
    private fun extractMetaData(clipboard: Transferable): Pair<String?, String?> {
        if (clipboard.isDataFlavorSupported(DataFlavor.fragmentHtmlFlavor).not()) return Pair(null, null)
        // Get metadata
        val clipboardData = clipboard.getTransferData(DataFlavor.fragmentHtmlFlavor) as String
        val html = Clipboard.extractHtmlFragmentFromClipboard(clipboardData)?.let {
            Jsoup.parse(it)
        } ?: return Pair(null, null)
        val image = html.select("img").firstOrNull() ?: return Pair(null, null)

        // Handle data.
        val source = image.attr("src") ?: return Pair(null, null)
        val imageFormat = FilenameUtils.getExtension(source)
        val imageName = FilenameUtils.getBaseName(source)
        return Pair(imageName, imageFormat)
    }

    private fun pasteImageFile(project: Project, file: VirtualFile, clipboard: Transferable) {
        val clipboardFile = clipboard.extractFile() ?: return
        InsertGraphicWizardAction(clipboardFile).executeAction(file, project)
    }

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false

        val transferable = dataContext.getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return false
        return hasRawImage(transferable) ||
            transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
    }

    fun hasRawImage(transferable: Transferable): Boolean {
        return transferable.isDataFlavorSupported(DataFlavor.imageFlavor) && transferable.getTransferData(DataFlavor.imageFlavor) is BufferedImage
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}