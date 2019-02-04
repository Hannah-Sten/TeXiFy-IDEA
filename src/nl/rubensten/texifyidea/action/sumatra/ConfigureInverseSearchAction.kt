package nl.rubensten.texifyidea.action.sumatra

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.ui.DialogBuilder
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.run.isSumatraAvailable
import javax.swing.JLabel
import javax.swing.SwingConstants


/**
 * Sets up inverse search integration with SumatraPDF.
 *
 * This action attempts to set the permanent inverse search command setting in SumatraPDF. This action is Windows-only.
 *
 * @author Sten Wessel
 * @since b0.4
 */
open class ConfigureInverseSearchAction : AnAction(
        "ConfigureInverseSearch",
        "Setup inverse search integration with SumatraPDF and TeXiFy IDEA.",
        TexifyIcons.SETTINGS
) {

    override fun actionPerformed(e: AnActionEvent) {
        DialogBuilder().apply {
            setTitle("Configure inverse search")
            setCenterPanel(JLabel(
                    "<html>To enable inverse search (from PDF to source file), the inverse search setting in SumatraPDF must be changed.<br/>" +
                    "By clicking OK, this change will automatically be applied and SumatraPDF will be started.<br/><br/>" +
                    "Warning: this will permanently overwrite the previous inverse search setting in SumatraPDF.</html>",
                    AllIcons.General.WarningDialog,
                    SwingConstants.LEADING
            ))

            addOkAction()
            addCancelAction()
            setOkOperation {
                val path = PathManager.getBinPath()
                var name = ApplicationNamesInfo.getInstance().scriptName

                // If we can find a 64-bits Java, then we can start (the equivalent of) idea64.exe since that will use the 64-bits Java
                // see issue 104 and https://github.com/Ruben-Sten/TeXiFy-IDEA/issues/809
                // If we find a 32-bits Java or nothing at all, we will keep (the equivalent of) idea.exe which is the default
                if (System.getProperty("sun.arch.data.model") == "64") {
                    // We will assume that since the user is using a 64-bit IDEA that name64 exists, this is at least true for idea64.exe and pycharm64.exe on Windows
                    name += "64"
                    // We also remove an extra "" because it opens an empty IDEA instance when present
                    Runtime.getRuntime().exec("cmd.exe /c start SumatraPDF -inverse-search \"\\\"$path\\$name.exe\\\" --line %l \\\"%f\\\"\"")
                }
                else {
                    Runtime.getRuntime().exec("cmd.exe /c start SumatraPDF -inverse-search \"\\\"$path\\$name.exe\\\" \\\"\\\" --line %l \\\"%f\\\"\"")
                }

                dialogWrapper.close(0)
            }

            show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = isSumatraAvailable
    }
}
