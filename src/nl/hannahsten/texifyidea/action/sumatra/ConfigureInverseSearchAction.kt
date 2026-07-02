package nl.hannahsten.texifyidea.action.sumatra

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import nl.hannahsten.texifyidea.TexifyBundle
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
            setTitle(TexifyBundle.message("ui.dialog.configure.inverse.search.title"))
            setCenterPanel(
                JLabel(
                    TexifyBundle.message("ui.dialog.configure.inverse.search.sumatra.body.html"),
                    AllIcons.General.WarningDialog,
                    SwingConstants.LEADING
                )
            )

            addOkAction()
            addCancelAction()
            setOkOperation {
                SumatraViewer.configureInverseSearch(e.project)
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
