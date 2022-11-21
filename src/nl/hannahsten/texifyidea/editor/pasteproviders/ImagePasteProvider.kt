package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.file.SaveImageFromWebDialog
import nl.hannahsten.texifyidea.util.Log
import org.jsoup.nodes.Node
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

open class ImagePasteProvider : LatexPasteProvider {

    private fun pasteRawImage(project: Project, file: VirtualFile, clipboard: BufferedImage): String {
        var outstring = ""

        SaveImageFromWebDialog(project, clipboard) {
            it.savedImage?.let { imageFile ->
                outstring = InsertGraphicWizardAction(imageFile).getActionString(file, project)
            }
        }

        return outstring
    }

    override fun translateHtml(htmlIn: Node, dataContext: DataContext): String {
        Log.warn("Trying to parse an image")

        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return ""
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return ""
        val url = URL(htmlIn.attr("src"))

        val image = ImageIO.read(url)
        return pasteRawImage(project, file, image)
    }
}