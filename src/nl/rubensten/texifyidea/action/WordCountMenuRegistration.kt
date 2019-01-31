package nl.rubensten.texifyidea.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.BaseComponent
import com.intellij.util.messages.MessageBus
import nl.rubensten.texifyidea.action.analysis.WordCountAction
import nl.rubensten.texifyidea.action.group.LatexAnalyzeMenuGroup

/**
 * Register a menu item programmatically instead of in plugin.xml, so we can customize when it is shown or not.
 */
class WordCountMenuRegistration : BaseComponent {
    override fun initComponent() {
        super.initComponent()

        // Documentation for registering an action: http://www.jetbrains.org/intellij/sdk/docs/basics/action_system.html?search=action#registering-actions-from-code

        // The Analyze menu group is not present in PyCharm and probably also not in other IDEs, so we will only use it in IntelliJ.
        // In PyCharm (and others) we will use the Code group.
        val applicationName = ApplicationNamesInfo.getInstance().scriptName

        if (applicationName == "idea") {

            // Get an instance of the Analyze action group by ID
            val analyzeGroup: DefaultActionGroup = ActionManager.getInstance().getAction(IdeActions.GROUP_ANALYZE) as DefaultActionGroup

            analyzeGroup.addSeparator()

            val latexAnalyzeMenuGroup = ActionManager.getInstance().getAction("texify.LatexMenuAnalyze") as DefaultActionGroup

            analyzeGroup.addAll(latexAnalyzeMenuGroup)
        } else {
            // Get an instance of the Analyze action group by ID
//            val analyzeGroup: DefaultActionGroup = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(IdeActions.????) as DefaultActionGroup

//            analyzeGroup.addSeparator()
//            analyzeGroup.add(action)
        }

    }
}