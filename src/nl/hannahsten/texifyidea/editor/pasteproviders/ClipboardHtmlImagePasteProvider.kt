package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.file.SaveImageFromWebDialog
import org.jsoup.nodes.Node
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

/**
 * When there is an image inside html in the clipboard, this paste provider will start the InsertGraphicWizardAction
 * todo this class is very similar to ImagePasteProvider but for a third type of image (embedded in html (?) )
 */
open class ClipboardHtmlImagePasteProvider : LatexPasteProvider {

    // todo this seems to duplicate the other ImagePasteProvider#pasteRawImage
    private fun pasteRawImage(project: Project, file: VirtualFile, clipboard: BufferedImage, sourceUrl: URL): String {
        var outstring = ""

        SaveImageFromWebDialog(project, clipboard, sourceUrl) {
            it.savedImage?.let { imageFile ->
                outstring = InsertGraphicWizardAction(imageFile).getGraphicString(file, project)
            }
        }

        return outstring
    }

    // todo this looks very much like ImagePasteProvider#performPaste
    override fun convertHtmlToLatex(htmlIn: Node, dataContext: DataContext): String {
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return ""
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return ""
        val url = URL(htmlIn.attr("src"))

        val image = ImageIO.read(url)
        return pasteRawImage(project, file, image, url)
    }
}