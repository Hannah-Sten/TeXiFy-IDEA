package nl.rubensten.texifyidea.ui

import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * @author Sergei Izmailov
 */
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

                runCommand(
                        pdf2svgExecutable(),
                        arrayOf(
                                "$tempBasename.pdf",
                                "$tempBasename.svg"
                        ),
                        tempDirectory
                ) ?: return

                runCommand(
                        inkscapeExecutable(),
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


            }
            finally {
                tempDirectory.deleteRecursively()
            }
        }
        catch (exception: IOException) {
            previewForm.setLatexErrorMessage("${exception.message}")
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

        val (stdout, stderr) = executable.inputStream.bufferedReader().use { stdout ->
            executable.errorStream.bufferedReader().use { stderr ->
                Pair(stdout.readText(), stderr.readText())
            }
        }
        if (executable.exitValue() != 0) {
            previewForm.setLatexErrorMessage("$command exited with ${executable.exitValue()}\n$stdout\n$stderr")
            return null
        }
        return Triple(executable.exitValue(), stdout, stderr)
    }

    private fun inkscapeExecutable(): String {
        var suffix = ""
        if (SystemInfo.isWindows) {
            suffix = ".exe"
        }
        return "inkscape$suffix"
    }

    private fun pdf2svgExecutable(): String {
        var suffix = ""
        if (SystemInfo.isWindows) {
            suffix = ".exe"
        }
        return "pdf2svg$suffix"
    }
}