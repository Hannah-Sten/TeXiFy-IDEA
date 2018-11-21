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
                    preview_panel!!.clear_image()
                    return;
                }

                val reader = BufferedReader(InputStreamReader(latex.inputStream))
                output_area!!.text = reader.readText()
                reader.close()
                if (latex.exitValue() == 0) {
                    val pdf2svg = Runtime.getRuntime().exec(
                            arrayOf("pdf2svg",
                                    tmp_basename + ".pdf",
                                    tmp_basename + ".svg"
                            ),
                            null,
                            tmpdir
                    )

                    if (!pdf2svg.waitFor(3, TimeUnit.SECONDS)){
                        pdf2svg.destroy()
                        preview_panel!!.clear_image()
                        output_area!!.text = "pdf2svg took more than 3 seconds. Terminated."
                        return;
                    }

                    if (pdf2svg.exitValue()==0){

                        val inkscape = Runtime.getRuntime().exec(
                                arrayOf("inkscape",
                                        tmp_basename + ".svg",
                                        "--export-area-drawing",
                                        "--export-dpi", "1000",
                                        "--export-background", "#FFFFFF",
                                        "--export-png", tmp_basename + ".png"
                                ),
                                null,
                                tmpdir
                        )
                        if (!inkscape.waitFor(3, TimeUnit.SECONDS)){
                            inkscape.destroy()
                            preview_panel!!.clear_image()
                            output_area!!.text = "inkscape took more than 3 seconds. Terminated."
                            return;
                        }

                        if (inkscape.exitValue()==0){
                            val image = ImageIO.read(File(tmp_basename + ".png"))

                            preview_panel!!.set_image(image)
                            preview_panel!!.requestFocus()
                        }else{
                            output_area!!.text += "Inkscape exited with " + inkscape.exitValue()
                            preview_panel!!.clear_image()
                        }
                    }else{
                        output_area!!.text += "Pdf2svg exited with " + pdf2svg.exitValue()
                        preview_panel!!.clear_image()
                    }

                }else{
                    preview_panel!!.clear_image()
                    output_area!!.text += "Latex exited with " + latex.exitValue()
                }
            } finally {
                tmpdir.delete()
            }

        } catch (e: IOException) {

        }

    }
}
