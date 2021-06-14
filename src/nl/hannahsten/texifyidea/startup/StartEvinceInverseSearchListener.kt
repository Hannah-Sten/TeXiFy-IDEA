package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.run.step.PdfViewerStep
import nl.hannahsten.texifyidea.util.selectedRunConfig

/**
 * @author Sten Wessel
 */
class StartEvinceInverseSearchListener : StartupActivity, DumbAware {

    override fun runActivity(project: Project) {
        if (project.selectedRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.any { it.state.pdfViewer == InternalPdfViewer.EVINCE } == true) {
            EvinceInverseSearchListener.start(project)
        }
    }
}
