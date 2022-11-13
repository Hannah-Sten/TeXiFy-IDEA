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
import java.io.*
import java.net.URL
import javax.imageio.ImageIO


open class ImagePasteProvider : LatexPasteProvider {

    private fun pasteRawImage(project: Project, file: VirtualFile, clipboard: BufferedImage) {
        SaveImageFromWebDialog(project, clipboard) {
            it.savedImage?.let { imageFile ->
                InsertGraphicWizardAction(imageFile).executeAction(file, project)
            }
        }
    }

    override fun translateHtml(htmlIn: Node, dataContext: DataContext): String {
        Log.warn("Trying to parse an image")

        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return ""
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return ""
        val url = URL(htmlIn.attr("src"))

        /*val connection = url.openConnection()
        connection.doOutput = true
        val openStream = connection.getInputStream()
        val instream: InputStream = BufferedInputStream(openStream)
        val baos = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var n = 0
        while (-1 != instream.read(buf).also { n = it }) {
            baos.write(buf, 0, n)
        }
        baos.close()
        instream.close()
        val response: ByteArray = baos.toByteArray()*/

//        File("DUMMY").writeBytes(response)
//        val createImageInputStream = ImageIO.createImageInputStream(response)
//        val image = ImageIO.read(File("DUMMY"))

        val image = ImageIO.read(url)
        pasteRawImage(project, file, image)

        Log.warn("WTF I DIDNT DIE")
        return "THE IMAGE GO HERE"
    }
}