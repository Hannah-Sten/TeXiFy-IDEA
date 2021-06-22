package nl.hannahsten.texifyidea.run.pdfviewer

import nl.hannahsten.texifyidea.run.executable.CustomExecutable
import java.io.File

class CustomPdfViewer(
    override val executablePath: String,
    override val executableName: String = File(executablePath).name,
    override val name: String = executableName,
    override val displayName: String = name
) : PdfViewer, CustomExecutable {
    override fun isAvailable() = true
}