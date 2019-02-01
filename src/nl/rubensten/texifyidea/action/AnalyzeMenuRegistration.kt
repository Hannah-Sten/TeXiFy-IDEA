package nl.rubensten.texifyidea.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.BaseComponent

/**
 * Register a menu item programmatically instead of in plugin.xml, so we can customize when it is shown or not (xml is preferred, but we cannot switch on application name in xml).
 *
 * @author Thomas Schouten
 */
class AnalyzeMenuRegistration : BaseComponent {

    override fun initComponent() {
        super.initComponent()

        // Documentation for registering an action: http://www.jetbrains.org/intellij/sdk/docs/basics/action_system.html?search=action#registering-actions-from-code

        // Get the group which should be added to either the Analyze menu or something else
        val latexAnalyzeMenuGroup = ActionManager.getInstance().getAction("texify.LatexMenuAnalyze") as DefaultActionGroup

        // The Analyze menu group is not present in PyCharm and probably also not in other IDEs, so we will only use it in IntelliJ.
        // In PyCharm (and others) we will use the Code group.
        val applicationName = ApplicationNamesInfo.getInstance().scriptName

        if (applicationName == "idea") {
            // Get an instance of the Analyze action group by ID
            val analyzeGroup: DefaultActionGroup = ActionManager.getInstance().getAction(IdeActions.GROUP_ANALYZE) as DefaultActionGroup

            // Add the group which contains the LaTeX actions to the Analyze menu
            analyzeGroup.add(latexAnalyzeMenuGroup)
        }
        else {
            // Get an instance of the Code action group by ID
            val analyzeGroup: DefaultActionGroup = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction("CodeMenu") as DefaultActionGroup

            analyzeGroup.add(latexAnalyzeMenuGroup)
        }

    }
}