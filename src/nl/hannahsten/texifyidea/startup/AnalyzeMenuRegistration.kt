package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Register a menu item programmatically instead of in plugin.xml, so we can customize when it is shown or not (xml is preferred, but we cannot switch on application name in xml).
 *
 * @author Thomas Schouten
 */
class AnalyzeMenuRegistration : StartupActivity, DumbAware {

    @Synchronized
    override fun runActivity(project: Project) {
        // Get the group which should be added to either the Analyze menu or something else
        val latexAnalyzeMenuGroup = ActionManager.getInstance().getAction("texify.LatexMenuAnalyze") as DefaultActionGroup

        val analyzeGroup = getAnalyzeGroup()

        // Add the group which contains the LaTeX actions to the Analyze menu
        // First remove it, to avoid adding it twice
        analyzeGroup.remove(latexAnalyzeMenuGroup)
        analyzeGroup.add(latexAnalyzeMenuGroup)
    }

    /**
     * The Analyze menu group is not present in PyCharm and probably also not in other IDEs, so we will only use it in IntelliJ.
     * In PyCharm (and others) we will use the Code group.
     */
    private fun getAnalyzeGroup(): DefaultActionGroup {
        val applicationName = ApplicationNamesInfo.getInstance().scriptName

        return if (applicationName == "idea") {
            // Get an instance of the Analyze action group by ID
            ActionManager.getInstance().getAction(IdeActions.GROUP_ANALYZE) as DefaultActionGroup
        }
        else {
            // Get an instance of the Code action group by ID
            ActionManager.getInstance().getAction("CodeMenu") as DefaultActionGroup
        }
    }

    fun unload() {
        val latexAnalyzeMenuGroup = ActionManager.getInstance().getAction("texify.LatexMenuAnalyze") as DefaultActionGroup
        val analyzeGroup = getAnalyzeGroup()
        analyzeGroup.remove(latexAnalyzeMenuGroup)
    }
}