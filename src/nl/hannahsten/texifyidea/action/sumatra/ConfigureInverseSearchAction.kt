package nl.hannahsten.texifyidea.action.sumatra

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import javax.swing.JLabel
import javax.swing.SwingConstants

/**
 * Sets up inverse search integration with SumatraPDF.
 *
 * This action attempts to set the permanent inverse search (backward search) command setting in SumatraPDF. This action is Windows-only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
open class ConfigureInverseSearchAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        DialogBuilder().apply {
            setTitle("Configure Inverse Search")
            setCenterPanel(
                JLabel(
                    "<html>To enable inverse search (from PDF to source file), the inverse search setting in SumatraPDF must be changed.<br/>" +
                        "By clicking OK, this change will automatically be applied and SumatraPDF will be restarted.<br/><br/>" +
                        "Warning: this will permanently overwrite the previous inverse search setting in SumatraPDF.</html>",
                    AllIcons.General.WarningDialog,
                    SwingConstants.LEADING
                )
            )

            addOkAction()
            addCancelAction()
            setOkOperation {
                SumatraViewer.configureInverseSearch()
                dialogWrapper.close(0)
            }

            show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = SumatraViewer.isAvailable()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
