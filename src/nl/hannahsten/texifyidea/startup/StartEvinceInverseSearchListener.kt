package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import nl.hannahsten.texifyidea.run.pdfviewer.Evince
import nl.hannahsten.texifyidea.run.pdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.run.step.PdfViewerStep
import nl.hannahsten.texifyidea.util.selectedRunConfig

/**
 * @author Sten Wessel
 */
class StartEvinceInverseSearchListener : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (project.selectedRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.any { it.state.pdfViewer is Evince } == true) {
            EvinceInverseSearchListener.start(project)
        }
    }
}
