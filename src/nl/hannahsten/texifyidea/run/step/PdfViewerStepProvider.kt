package nl.hannahsten.texifyidea.run.step

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import javax.swing.Icon

object PdfViewerStepProvider : StepProvider {

    override val name: String = "Open PDF"

    override val icon: Icon = TexifyIcons.PDF_FILE

    override val id: String = "pdf-viewer"

    override fun createStep(configuration: LatexRunConfiguration) = PdfViewerStep(this, configuration)
}