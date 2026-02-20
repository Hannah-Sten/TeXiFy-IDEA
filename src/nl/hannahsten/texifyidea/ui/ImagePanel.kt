package nl.hannahsten.texifyidea.ui

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.lang.Double.min
import java.lang.Integer.max
import javax.swing.JPanel

/**
 * @author Sergei Izmailov
 */
internal class ImagePanel : JPanel() {

    private var image: Image? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var scaled: Image? = null

    fun setImage(img: BufferedImage) {
        this.image = img
        this.imageHeight = img.height
        this.imageWidth = img.width
        this.invalidate()
        revalidate()
        repaint()
    }

    fun clearImage() {
        this.image = null
        this.scaled = null
        this.imageHeight = 1
        this.imageWidth = 1
        this.invalidate()
        revalidate()
        repaint()
    }

    override fun invalidate() {
        super.invalidate()

        if (image == null) return

        val width = max(50, width)
        val height = max(50, height)

        // With larger ratios, the quality of the png will be too low for convenient viewing
        val ratio = min(width.toDouble() / imageWidth, height.toDouble() / imageHeight).coerceAtMost(2.0)

        val scaledImageWidth = (imageWidth * ratio).toInt()
        val scaledImageHeight = (imageHeight * ratio).toInt()

        scaled = image?.getScaledInstance(scaledImageWidth, scaledImageHeight, Image.SCALE_SMOOTH)
    }

    override fun getPreferredSize(): Dimension = if (image == null) {
        Dimension(200, 200)
    }
    else Dimension(image!!.getWidth(this), image!!.getHeight(this))

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (this.scaled != null) {
            g.drawImage(scaled, 0, 0, null)
        }
    }
}