package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.SystemInfo
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
            if (project.selectedRunConfig()?.pdfViewer == EvinceViewer ||
                (project.selectedRunConfig() == null && project.latexTemplateRunConfig()?.pdfViewer == EvinceViewer)
            ) {
                EvinceInverseSearchListener.start(project)
            }
        }
    }
}
