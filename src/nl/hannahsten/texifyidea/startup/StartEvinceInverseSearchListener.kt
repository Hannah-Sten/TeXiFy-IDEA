package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.util.latexTemplateRunConfig
import nl.hannahsten.texifyidea.util.selectedRunConfig

/**
 * @author Sten Wessel
 */
class StartEvinceInverseSearchListener : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (project.selectedRunConfig()?.pdfViewer == InternalPdfViewer.EVINCE ||
            (project.selectedRunConfig() == null && project.latexTemplateRunConfig()?.pdfViewer == InternalPdfViewer.EVINCE)
        ) {
            EvinceInverseSearchListener.start(project)
        }
    }
}
