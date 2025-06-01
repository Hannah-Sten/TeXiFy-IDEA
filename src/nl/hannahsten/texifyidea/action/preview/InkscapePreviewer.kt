package nl.hannahsten.texifyidea.action.preview

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.JBColor
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import nl.hannahsten.texifyidea.util.toHex
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

/**
 * Preview based on Inkscape.
 */
class InkscapePreviewer : Previewer {

    override fun preview(input: String, previewForm: PreviewForm, project: Project, preamble: String, waitTime: Long) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating preview...") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    // Snap apps are confined to the users home directory
                    if (SystemEnvironment.isInkscapeInstalledAsSnap) {
                        setPreviewCodeInTemp(
                            FileUtil.createTempDirectory(File(System.getProperty("user.home")), "preview", null).toPath(),
                            input,
                            project,
                            preamble,
                            previewForm,
                            waitTime
                        )
                    }
                    else {
                        setPreviewCodeInTemp(FileUtil.createTempDirectory("preview", null).toPath(), input, project, preamble, previewForm, waitTime)
                    }
                }
                catch (exception: AccessDeniedException) {
                    previewForm.setLatexErrorMessage("${exception.message}")
                }
                catch (exception: IOException) {
                    previewForm.setLatexErrorMessage("${exception.message}")
                }
            }
        })
    }

    /**
     * First define the function that actually does stuff in a temp folder. The usual temp directory might not be
     * accessible by inkscape (e.g., when inkscape is a snap), and using function we can specify an alternative
     * temp directory in case the usual fails.
     */
    private fun setPreviewCodeInTemp(
        tempDirectory: Path,
        previewCode: String,
        project: Project,
        preamble: String,
        previewForm: PreviewForm,
        waitTime: Long
    ) {
        try {
            val tempBasename = Paths.get(tempDirectory.toString(), "temp").toString()
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

            // Sets error message to the UI if any
            val success = runInkscape(tempBasename, tempDirectory, waitTime, previewForm)
            if (success) {
                val image = ImageIO.read(File("$tempBasename.png"))
                SwingUtilities.invokeLater {
                    previewForm.setPreview(image, latexStdoutText)
                }
            }
        }
        finally {
            // Delete all the created temp files in the default temp directory.
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun runPreviewFormCommand(
        command: String,
        args: Array<String>,
        workDirectory: Path,
        waitTime: Long,
        previewForm: PreviewForm
    ): String? {
        val result = runCommandWithExitCode(command, *args, workingDirectory = workDirectory, timeout = waitTime, returnExceptionMessage = true)

        if (result.second != 0) {
            previewForm.setLatexErrorMessage("$command exited with ${result.second}\n${result.first ?: ""}")
            return null
        }

        return result.first
    }

    /**
     * Run inkscape command to convert pdf to png, depending on the version of inkscape.
     *
     * @return If successful
     */
    private fun runInkscape(tempBasename: String, tempDirectory: Path, waitTime: Long, previewForm: PreviewForm): Boolean {
        // If 1.0 or higher
        if (SystemEnvironment.inkscapeMajorVersion >= 1 || !SystemEnvironment.isAvailable("inkscape")) {
            runPreviewFormCommand(
                inkscapeExecutable(),
                arrayOf(
                    "$tempBasename.pdf",
                    "--export-area-drawing",
                    "--export-dpi", "1000",
                    "--export-background", JBColor.background().toHex(),
                    "--export-background-opacity", "1.0",
                    "--export-filename", "$tempBasename.png"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: return false
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
            ) ?: return false

            runPreviewFormCommand(
                inkscapeExecutable(),
                arrayOf(
                    "$tempBasename.svg",
                    "--export-area-drawing",
                    "--export-dpi", "1000",
                    "--export-background", JBColor.background().toHex(),
                    "--export-png", "$tempBasename.png"
                ),
                tempDirectory,
                waitTime,
                previewForm
            ) ?: return false
        }
        return true
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