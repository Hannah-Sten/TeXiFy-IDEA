package nl.rubensten.texifyidea.window


import javax.swing.*
import java.awt.*
import java.awt.image.BufferedImage
import java.lang.Integer.max
import java.lang.Double.min

internal class ImagePanel : JPanel() {

    private var img: Image? = null
    private var img_width: Int = 1
    private var img_height: Int = 1
    private var scaled: Image? = null

    fun set_image(img: BufferedImage) {
        this.img = img
        this.img_height = img.height
        this.img_width = img.width
        this.invalidate()
    }

    override fun invalidate() {
        super.invalidate()

        if (img == null) return

        val width = max(50,width)
        val height = max(50,height)

        val ratio = min(width.toDouble()/img_width,height.toDouble()/img_height)

        val w = (img_width*ratio).toInt()
        val h = (img_height*ratio).toInt()

        scaled = img?.getScaledInstance(w, h, Image.SCALE_SMOOTH)
    }

    override fun getPreferredSize(): Dimension {
        return if (img == null)
            Dimension(200, 200)
        else
            Dimension(
                    img!!.getWidth(this), img!!.getHeight(this))
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (this.scaled != null) {
            g.drawImage(scaled, 0, 0, null)
        }
    }

    companion object {

        private val serialVersionUID = 1L
    }
}
