package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.file.SaveImageDialog
import nl.hannahsten.texifyidea.util.currentTextEditor
import org.apache.commons.io.FilenameUtils
import org.jsoup.nodes.Element
import java.awt.image.BufferedImage
import java.net.URI
import java.net.URL
import javax.imageio.IIOException
import javax.imageio.ImageIO

/**
 * When there is an <img> tag in HTML with a link to an image, download the image and insert it similar to [ImagePasteProvider].
 */
open class ImageHtmlToLatexConverter : HtmlToLatexConverter {

    /**
     * Show a dialog to the user where to save the file and then get the LaTeX to include it.
     */
    private fun saveFileAndGetLaTeX(project: Project, file: VirtualFile, image: BufferedImage, sourceUrl: URL): String {
        var outstring = ""

        val extension = FilenameUtils.getExtension(sourceUrl.file)
        val imageName = FilenameUtils.getBaseName(sourceUrl.file).take(100)

        val editor = project.currentTextEditor() ?: return ""
        SaveImageDialog(project, image, imageName, extension) { imageFile ->
            outstring = InsertGraphicWizardAction(imageFile).showDialogAndGetText(editor, file, project) ?: ""
        }

        return outstring
    }

    override fun convertHtmlToLatex(htmlIn: Element, file: LatexFile): String {
        val url = URI(htmlIn.attr("src")).toURL()
        val image = try {
            ImageIO.read(url)
        }
        catch (e: IIOException) {
            Notification("LaTeX", "Could not download image from $url", e.message ?: "", NotificationType.ERROR).notify(file.project)
            return ""
        }
        return saveFileAndGetLaTeX(file.project, file.virtualFile, image, url)
    }
}