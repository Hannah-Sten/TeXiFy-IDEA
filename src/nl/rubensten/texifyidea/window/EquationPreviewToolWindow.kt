package nl.rubensten.texifyidea.window

import javax.swing.*
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.*
import java.io.IOException
import com.intellij.openapi.wm.ToolWindow
import java.awt.GridBagLayout




class EquationPreviewToolWindow(toolWindow: ToolWindow) {
    private val preview_text=  JTextArea("latex text")
    private val preview_form = PreviewForm()

    init {
        set_image()
        set_text()
    }

    val content: JPanel?
        get() = preview_form.panel


    private fun set_image() {
        try {
            val myPicture = ImageIO.read(File("/home/sergei/Pictures/malysh.i.karlson.0-05-06.jpg"))
            preview_form.preview_panel.set_image(myPicture)
        } catch (e: IOException) {

        }

    }

    private fun set_text() {
    }

    private fun createUIComponents() {
        // TODO: place custom component creation code here
    }
}
