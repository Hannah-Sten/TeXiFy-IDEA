package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.run.pdfviewer.Evince
import nl.hannahsten.texifyidea.run.pdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.run.step.PdfViewerStep
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.run.pdfviewer.EvinceViewer
import nl.hannahsten.texifyidea.util.latexTemplateRunConfig
import nl.hannahsten.texifyidea.util.selectedRunConfig

/**
 * @author Sten Wessel
 */
class StartEvinceInverseSearchListener : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (!SystemInfo.isWindows) {
            if (
                project.selectedRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.any { it.state.pdfViewer is Evince } == true || (project.selectedRunConfig() == null && project.latexTemplateRunConfig()?.compileSteps?.filterIsInstance<PdfViewerStep>()?.any {it.state.pdfViewer is Evince } == true)
            ) {
                EvinceInverseSearchListener.start(project)
            }
        }
    }
}
