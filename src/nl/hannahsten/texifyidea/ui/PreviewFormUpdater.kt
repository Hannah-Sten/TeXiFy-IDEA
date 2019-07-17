package nl.hannahsten.texifyidea.ui

import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.SwingUtilities.invokeLater

/**
 * @author Sergei Izmailov
 */
class PreviewFormUpdater(private val previewForm: PreviewForm) {

    /**
     * Modify this variable to include more packages.
     *
     * Unless you are going to set your own \pagestyle{}, simply append to this variable.
     */
    var preamble = """
        \pagestyle{empty}

        \usepackage{color}

    """.trimIndent()

    /**
     * Controls how long (in seconds) we will wait for the document compilation. If the time taken exceeds this,
     * we will return an error and not output a preview.
     */
    var waitTime = 3L

    /**
     * Sets the code that will be previewed, whether that be an equation, a tikz picture, or whatever else
     * you are trying to preview.
     *
     * This function also starts the creation and compilation of the temporary preview document, and will then
     * either display the preview, or if something failed, the error produced.
     */
    fun compilePreview(previewCode: String) {
        previewForm.setEquation(previewCode)

        // First define the function that actually does stuff in a temp folder. The usual temp directory might not be
        // accessible by inkscape (e.g., when inkscape is a snap), and using function we can specify an alternative
        // temp directory in case the usual fails.
        fun setPreviewCodeInTemp(tempDirectory: File) {
            try {
                val tempBasename = Paths.get(tempDirectory.path.toString(), "temp").toString()
                val writer = PrintWriter("$tempBasename.tex", "UTF-8")

                val tmpContent = """\documentclass{article}
$preamble

\begin{document}

$previewCode

\end{document}"""
                writer.println(tmpContent)
                writer.close()

                val latexStdoutText = runCommand("pdflatex",
                        arrayOf(
                                "-interaction=nonstopmode",
                                "-halt-on-error",
                                "$tempBasename.tex"),
                        tempDirectory
                ) ?: return

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
                ) ?: throw AccessDeniedException(tempDirectory)

                val image = ImageIO.read(File("$tempBasename.png"))
                invokeLater {
                    previewForm.setPreview(image, latexStdoutText)
                }
            }
            finally {
                // Delete all the created temp files in the default temp directory.
                tempDirectory.deleteRecursively()
            }
        }

        GlobalScope.launch {
            try {
                // Create the default temp directory.
                setPreviewCodeInTemp(createTempDir())
            } catch (exception: AccessDeniedException) {
                // If pdf2svg or inkscape does not have access to the temp directory, try again with temp folder in the
                // home directory.
                setPreviewCodeInTemp(createTempDir(directory = File(System.getProperty("user.home"))))
            } catch (exception: IOException) {
                previewForm.setLatexErrorMessage("${exception.message}")
            }
        }
    }

    private fun runCommand(command: String, args: Array<String>, workDirectory: File): String? {

        val executable = Runtime.getRuntime().exec(
                arrayOf(command) + args,
                null,
                workDirectory
        )

        val (stdout, stderr) = executable.inputStream.bufferedReader().use { stdout ->
            executable.errorStream.bufferedReader().use { stderr ->
                Pair(stdout.readText(), stderr.readText())
            }
        }

        executable.waitFor(waitTime, TimeUnit.SECONDS)

        if (executable.exitValue() != 0) {
            previewForm.setLatexErrorMessage("$command exited with ${executable.exitValue()}\n$stdout\n$stderr")
            return null
        }

        return stdout
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
