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
    override val executableName: String = File(executablePath).name,
    override val name: String = executableName,
    override val displayName: String = name
) : PdfViewer, CustomExecutable {
    override fun isAvailable() = File(executablePath).isFile
}