package nl.rubensten.texifyidea.window

import com.google.common.io.Files
import java.io.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.*

class PreviewForm {
    var panel: JPanel? = null
    private var equation_area: JTextArea? = null
    private var output_area: JTextArea? = null
    private var output_panel: JPanel? = null
    private var equation_panel9: JPanel? = null
    private var preview_panel: ImagePanel? = null

    private val preamble = """
    \usepackage{amsmath,amsthm,amssymb,amsfonts}
    \usepackage{color}
    """.trimIndent()

    fun set_equation_text(equation_text: String) {
        equation_area!!.text = equation_text

        try {
            val tmpdir = Files.createTempDir()
            try {
                val tmp_basename = tmpdir.path + "/equation"
                val writer = PrintWriter(tmp_basename + ".tex", "UTF-8")

                val tmp_content = """
\documentclass{article}
""" + preamble +
                        """
\pagestyle{empty}
\begin{document}
""" + equation_text +
                        """
\end{document}"""
                writer.println(tmp_content)
                writer.close()

                val latex = Runtime.getRuntime().exec(
                        arrayOf("pdflatex",
                                "-halt-on-error",
                                tmp_basename + ".tex",
                                "-interaction=nonstopmode"),
                        null,
                        tmpdir
                )
                latex.outputStream.close()
                if (!latex.waitFor(3, TimeUnit.SECONDS)){
                    latex.destroy()
                    output_area!!.text = "Latex took more than 3 seconds. Terminated."
                    return;
                }

                val reader = BufferedReader(InputStreamReader(latex.inputStream))
                output_area!!.text = reader.readText()
                reader.close()
                if (latex.exitValue() == 0) {
                    val imagemagick = Runtime.getRuntime().exec(
                            arrayOf("convert",
                                    "-verbose",
                                    "-density", "300",
                                    tmp_basename + ".pdf",
                                    "-quality", "100",
                                    "-flatten",
                                    "-sharpen","0x1.0",
                                    "-trim",
                                    tmp_basename + ".png"
                            ),
                            null,
                            tmpdir
                    )

                    if (!imagemagick.waitFor(3, TimeUnit.SECONDS)){
                        imagemagick.destroy()
                        output_area!!.text = "Imagemagick took more than 3 seconds. Terminated."
                        return;
                    }

                    if (imagemagick.exitValue()==0){
                        val image = ImageIO.read(File(tmp_basename + ".png"))

                        preview_panel!!.set_image(image)
                        preview_panel!!.requestFocus()
                    }

                }else{
                    output_area!!.text += "Latex exited with " + latex.exitValue()
                }
            } finally {
                tmpdir.delete()
            }

        } catch (e: IOException) {

        }

    }
}
