package nl.rubensten.texifyidea.ui

import com.google.common.io.Files
import java.io.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.*

/**
 * @author Sergei Izmailov
 * */
class PreviewForm {

    var panel: JPanel? = null
    private var equationArea: JTextArea? = null
    private var outputArea: JTextArea? = null
    private var previewPanel: ImagePanel? = null

    private val preamble = """
        \usepackage{amsmath,amsthm,amssymb,amsfonts}
        \usepackage{color}
    """.trimIndent()

    @Suppress("UnstableApiUsage")
    fun setEquationText(equationText: String) {
        equationArea!!.text = equationText

        try {

            val tempDirectory = Files.createTempDir()
            try {
                val tempBasename = "${tempDirectory.path}/equation"
                val writer = PrintWriter("$tempBasename.tex", "UTF-8")

                val tmpContent = """
                    \documentclass{article}
                    $preamble

                    \pagestyle{empty}
                    \begin{document}
                    $equationText

                    \end{document}
                """.trimIndent()
                writer.println(tmpContent)
                writer.close()

                val latex = Runtime.getRuntime().exec(
                        arrayOf("pdflatex",
                                "-halt-on-error",
                                tempBasename + ".tex",
                                "-interaction=nonstopmode"),
                        null,
                        tempDirectory
                )
                latex.outputStream.close()
                if (!latex.waitFor(3, TimeUnit.SECONDS)) {
                    latex.destroy()
                    outputArea!!.text = "Latex took more than 3 seconds. Terminated."
                    previewPanel!!.clearImage()
                    return
                }

                latex.inputStream.bufferedReader().use {
                    outputArea!!.text = it.readText()
                }

                if (latex.exitValue() == 0) {
                    val pdf2svg = Runtime.getRuntime().exec(
                            arrayOf("pdf2svg",
                                    "$tempBasename.pdf",
                                    "$tempBasename.svg"
                            ),
                            null,
                            tempDirectory
                    )

                    if (!pdf2svg.waitFor(3, TimeUnit.SECONDS)) {
                        pdf2svg.destroy()
                        previewPanel!!.clearImage()
                        outputArea!!.text = "pdf2svg took more than 3 seconds. Terminated."
                        return
                    }

                    if (pdf2svg.exitValue() == 0) {

                        val inkscape = Runtime.getRuntime().exec(
                                arrayOf("inkscape",
                                        "$tempBasename.svg",
                                        "--export-area-drawing",
                                        "--export-dpi", "1000",
                                        "--export-background", "#FFFFFF",
                                        "--export-png", "$tempBasename.png"
                                ),
                                null,
                                tempDirectory
                        )

                        if (!inkscape.waitFor(3, TimeUnit.SECONDS)) {
                            inkscape.destroy()
                            previewPanel!!.clearImage()
                            outputArea!!.text = "inkscape took more than 3 seconds. Terminated."
                            return
                        }

                        if (inkscape.exitValue() == 0) {
                            val image = ImageIO.read(File("$tempBasename.png"))

                            previewPanel!!.setImage(image)
                            previewPanel!!.requestFocus()
                        }
                        else {
                            outputArea!!.text += "Inkscape exited with " + inkscape.exitValue()
                            previewPanel!!.clearImage()
                        }
                    }
                    else {
                        outputArea!!.text += "Pdf2svg exited with " + pdf2svg.exitValue()
                        previewPanel!!.clearImage()
                    }
                }
                else {
                    previewPanel!!.clearImage()
                    outputArea!!.text += "Latex exited with " + latex.exitValue()
                }
            } finally {
                tempDirectory.delete()
            }

        }
        catch (ignored: IOException) {

        }

    }
}
