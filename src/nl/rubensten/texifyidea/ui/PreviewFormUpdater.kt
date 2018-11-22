package nl.rubensten.texifyidea.ui

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class PreviewFormUpdater(val previewForm: PreviewForm) {

    var preamble = """
        \usepackage{amsmath,amsthm,amssymb,amsfonts}
        \usepackage{color}

        \pagestyle{empty}
    """.trimIndent()


    fun setEquationText(equationText: String) {
        previewForm.setEquation(equationText)

        try {
            val tempDirectory = createTempDir()
            try {
                val tempBasename = "${tempDirectory.path}/equation"
                val writer = PrintWriter("$tempBasename.tex", "UTF-8")

                val tmpContent = """
                    \documentclass{article}
                    $preamble

                    \begin{document}
                    $equationText

                    \end{document}
                """.trimIndent()
                writer.println(tmpContent)
                writer.close()

                val latexStdoutText = runCommand("pdflatex",
                        arrayOf(
                                "-halt-on-error",
                                "$tempBasename.tex",
                                "-interaction=nonstopmode"),
                        tempDirectory
                )?.second ?: return

                runCommand("pdf2svg",
                        arrayOf(
                                "$tempBasename.pdf",
                                "$tempBasename.svg"
                        ),
                        tempDirectory
                ) ?: return

                runCommand(
                        "inkscape",
                        arrayOf("$tempBasename.svg",
                                "--export-area-drawing",
                                "--export-dpi", "1000",
                                "--export-background", "#FFFFFF",
                                "--export-png", "$tempBasename.png"
                        ),
                        tempDirectory
                ) ?: return

                val image = ImageIO.read(File("$tempBasename.png"))
                previewForm.setPreview(image, latexStdoutText)


            } finally {
                FileUtils.deleteDirectory(tempDirectory)
            }

        } catch (ignored: IOException) {
        }

    }

    private fun runCommand(command: String, args: Array<String>, workDirectory: File): Triple<Int, String, String>? {
        val executable = Runtime.getRuntime().exec(
                arrayOf(command) + args,
                null,
                workDirectory
        )

        executable.outputStream.close()

        if (!executable.waitFor(3, TimeUnit.SECONDS)) {
            executable.destroy()
            previewForm.setLatexErrorMessage("$command took more than 3 seconds. Terminated.")
            return null
        }
        if (executable.exitValue() != 0) {
            previewForm.setLatexErrorMessage("$command exited with ${executable.exitValue()}\n " )
        }
        executable.inputStream.bufferedReader().use { stdout ->
            executable.errorStream.bufferedReader().use { stderr ->
                return Triple(executable.exitValue(), stdout.readText(), stderr.readText())
            }
        }

    }
}