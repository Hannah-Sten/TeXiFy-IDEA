package nl.hannahsten.texifyidea.action.okular

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.ui.OkularConfigureInverseSearchDialog

/**
 * @author Abby Berkers
 */
class ConfigureInverseSearchAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        OkularConfigureInverseSearchDialog()
    }

    /**
     * Hide this option when Okular is not available.
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = PdfViewer.OKULAR.isAvailable()
    }
}