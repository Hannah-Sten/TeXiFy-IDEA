package nl.hannahsten.texifyidea.startup

import com.intellij.execution.RunManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileState
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * @author Sten Wessel
 */
class StartEvinceInverseSearchListener : StartupActivity, DumbAware {

    override fun runActivity(project: Project) {
        val runConfig = RunManager.getInstance(project).selectedConfiguration?.configuration as? LatexRunConfiguration ?: return

        if (runConfig.pdfViewer == PdfViewer.EVINCE) {
            EvinceInverseSearchListener.start(project)
        }
    }
}
