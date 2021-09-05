package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.ui.PreviewForm
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * Preview based on Inkscape.
 */
class InkscapePreviewer : Previewer {

    override fun preview(input: String, previewForm: PreviewForm, project: Project, preamble: String, waitTime: Long) {
        GlobalScope.launch {
            try {
                // Snap apps are confined to the users home directory
                if (SystemEnvironment.isInkscapeInstalledAsSnap) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    setPreviewCodeInTemp(
                        FileUtil.createTempDirectory(File(System.getProperty("user.home")), "preview", null),
                        input,
                        project,
                        preamble,
                        previewForm,
                        waitTime
                    )
                }
                else {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    setPreviewCodeInTemp(FileUtil.createTempDirectory("preview", null), input, project, preamble, previewForm, waitTime)
                }
            }
            catch (exception: AccessDeniedException) {
                previewForm.setLatexErrorMessage("${exception.message}")
            }
            catch (exception: IOException) {
                previewForm.setLatexErrorMessage("${exception.message}")
            }
        }
    }

    /**
     * First define the function that actually does stuff in a temp folder. The usual temp directory might not be
     * accessible by inkscape (e.g., when inkscape is a snap), and using function we can specify an alternative
     * temp directory in case the usual fails.
     */
    private fun setPreviewCodeInTemp(
        tempDirectory: File,
        previewCode: String,
        project: Project,
        preamble: String,
        previewForm: PreviewForm,
        waitTime: Long
    ) {
        try {
            val tempBasename = Paths.get(tempDirectory.path.toString(), "temp").toString()
            val writer = PrintWriter("$tempBasename.tex", "UTF-8")

            val tmpContent =
                """\documentclass{article}
$preamble
\begin{document}
$previewCode
\end{document}"""
            writer.println(tmpContent)
            writer.close()

            val latexStdoutText = runPreviewFormCommand(
                LatexSdkUtil.getExecutableName("pdflatex", project),
                arrayOf(
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    "$tempBasename.tex"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: return

            runInkscape(tempBasename, tempDirectory, waitTime, previewForm)
            val image = ImageIO.read(File("$tempBasename.png"))
            SwingUtilities.invokeLater {
                previewForm.setPreview(image, latexStdoutText)
            }
        }
        finally {
            // Delete all the created temp files in the default temp directory.
            tempDirectory.deleteRecursively()
        }
    }

    private fun runPreviewFormCommand(
        command: String,
        args: Array<String>,
        workDirectory: File,
        waitTime: Long,
        previewForm: PreviewForm
    ): String? {

        val result = runCommandWithExitCode(command, *args, workingDirectory = workDirectory, timeout = waitTime)

        if (result.second != 0) {
            previewForm.setLatexErrorMessage("$command exited with ${result.second}\n${result.first ?: ""}")
            return null
        }

        return result.first
    }

    /**
     * Run inkscape command to convert pdf to png, depending on the version of inkscape.
     */
    private fun runInkscape(tempBasename: String, tempDirectory: File, waitTime: Long, previewForm: PreviewForm) {
        // If 1.0 or higher
        if (SystemEnvironment.inkscapeMajorVersion >= 1) {
            runPreviewFormCommand(
                inkscapeExecutable(),
                arrayOf(
                    "$tempBasename.pdf",
                    "--export-area-drawing",
                    "--export-dpi", "1000",
                    "--export-background", "#FFFFFF",
                    "--export-background-opacity", "1.0",
                    "--export-filename", "$tempBasename.png"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: throw AccessDeniedException(tempDirectory)
        }
        else {
            runPreviewFormCommand(
                pdf2svgExecutable(),
                arrayOf(
                    "$tempBasename.pdf",
                    "$tempBasename.svg"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: return

            runPreviewFormCommand(
                inkscapeExecutable(),
                arrayOf(
                    "$tempBasename.svg",
                    "--export-area-drawing",
                    "--export-dpi", "1000",
                    "--export-background", "#FFFFFF",
                    "--export-png", "$tempBasename.png"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: throw AccessDeniedException(tempDirectory)
        }
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