package nl.hannahsten.texifyidea.run.pdfviewer

import nl.hannahsten.texifyidea.run.executable.CustomExecutable
import java.io.File

/**
 * PDF viewer given by an arbitrary executable.
 * This includes known pdf viewers with a custom location, so we might be able to forward search depending
 * on the pdf viewer.
 */
class CustomPdfViewer(
    override val executablePath: String,
    override val name: String = File(executablePath).name, override val displayName: String? = "Custom PDF Viewer",
) : PdfViewer, CustomExecutable {
    override fun isAvailable() = File(executablePath).isFile

    // todo check executables
    fun isSumatra() = executablePath.endsWith("sumatra.exe")
}