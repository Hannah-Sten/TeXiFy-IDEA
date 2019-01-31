package nl.rubensten.texifyidea.action

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.components.BaseComponent
import com.intellij.util.messages.MessageBus
import nl.rubensten.texifyidea.action.analysis.WordCountAction
import nl.rubensten.texifyidea.action.group.LatexAnalyzeMenuGroup

/**
 * Register a menu item programmatically instead of in plugin.xml, so we can customize when it is shown or not.
 */
class TexifyMenuRegistration : BaseComponent {
    override fun initComponent() {
        super.initComponent()

        // The Analyze menu group is not present in PyCharm and probably also not in other IDEs, so we will only use it in IntelliJ.
        // In PyCharm (and others) we will use the Code group.

        // Documentation for registering an action: http://www.jetbrains.org/intellij/sdk/docs/basics/action_system.html?search=action#registering-actions-from-code

        val action = WordCountAction()
        // Associate the action with an ID
        com.intellij.openapi.actionSystem.ActionManager.getInstance().registerAction("texify.analysis.WordCount", action)

        // Get an instance of the Analyze action group by ID
        val analyzeGroup: DefaultActionGroup = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction(IdeActions.GROUP_ANALYZE) as DefaultActionGroup

        analyzeGroup.addSeparator()
//        analyzeGroup.add(action)

        val latexAnalyzeMenuGroup = LatexAnalyzeMenuGroup()
        latexAnalyzeMenuGroup.add(action)

        analyzeGroup.addAll(latexAnalyzeMenuGroup)

    }
}