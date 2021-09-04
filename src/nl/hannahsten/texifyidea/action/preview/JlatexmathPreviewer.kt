package nl.hannahsten.texifyidea.action.preview

import nl.hannahsten.texifyidea.ui.PreviewForm
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.scilab.forge.jlatexmath.DefaultTeXFont
import org.scilab.forge.jlatexmath.ParseException
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import org.scilab.forge.jlatexmath.cyrillic.CyrillicRegistration
import org.scilab.forge.jlatexmath.greek.GreekRegistration
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import java.io.*
import javax.imageio.ImageIO
import javax.swing.JLabel
import javax.swing.SwingUtilities

class JlatexmathPreviewer : Previewer {
    override fun preview(input: String, previewForm: PreviewForm) {
        val tempBaseName = "temp"
        try {
            // Fonts should be shapes, otherwise the user would have needed to install the font to render it
            // when converting to png (because fonts are not saved in svg).
            toSVG(input, tempBaseName, fontAsShapes = true)
            saveSvgAsPng(tempBaseName)
            val image = ImageIO.read(File("$tempBaseName.png"))
            SwingUtilities.invokeLater {
                previewForm.setPreview(image, "")
            }
        }
        catch (e: ParseException) {
            previewForm.setLatexErrorMessage(e.message ?: "There was an unknown problem with compiling the preview.")
        }
    }


    @Throws(FileNotFoundException::class, TranscoderException::class, IOException::class)
    private fun saveSvgAsPng(tempBaseName: String) {
        val ti = TranscoderInput(FileInputStream("$tempBaseName.svg"))
        val os = FileOutputStream("$tempBaseName.png")
        val to = TranscoderOutput(os)
        val pt = PNGTranscoder()
        pt.transcode(ti, to)
        os.flush()
        os.close()
    }

    /**
     * https://github.com/opencollab/jlatexmath/blob/af77a8e80d41ff67dfe2f42f14b41f6860dfeeec/jlatexmath-example-export/src/test/java/org/scilab/forge/jlatexmath/examples/export/Convert.java#L39
     */
    @Throws(IOException::class)
    fun toSVG(latex: String?, tempBaseName: String, fontAsShapes: Boolean) {
        val domImpl = GenericDOMImplementation.getDOMImplementation()
        val svgNS = "http://www.w3.org/2000/svg"
        val document = domImpl.createDocument(svgNS, "svg", null)
        val ctx = SVGGeneratorContext.createDefault(document)
        val g2 = SVGGraphics2D(ctx, fontAsShapes)
        DefaultTeXFont.registerAlphabet(CyrillicRegistration())
        DefaultTeXFont.registerAlphabet(GreekRegistration())
        val formula = TeXFormula(latex)
        val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20f)
        icon.insets = Insets(5, 5, 5, 5)
        g2.svgCanvasSize = Dimension(icon.iconWidth, icon.iconHeight)
        g2.color = Color.white
        g2.fillRect(0, 0, icon.iconWidth, icon.iconHeight)
        val jl = JLabel()
        jl.foreground = Color(0, 0, 0)
        icon.paintIcon(jl, g2, 0, 0)
        val useCSS = true
        val svgs = FileOutputStream("$tempBaseName.svg")
        val out: Writer = OutputStreamWriter(svgs, "UTF-8")
        g2.stream(out, useCSS)
        svgs.flush()
        svgs.close()
    }
}