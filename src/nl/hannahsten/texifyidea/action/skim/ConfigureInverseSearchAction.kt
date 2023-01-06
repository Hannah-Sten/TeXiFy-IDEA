package nl.hannahsten.texifyidea.action.skim

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.ui.SkimConfigureInverseSearchDialog

/**
 * @author Stephan Sundermann
 */
class ConfigureInverseSearchAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SkimConfigureInverseSearchDialog()
    }

    /**
     * Hide this option when Skim is not available.
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = InternalPdfViewer.SKIM.isAvailable()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
