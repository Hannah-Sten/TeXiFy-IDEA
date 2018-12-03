package nl.rubensten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.util.findCommandDefinitions
import nl.rubensten.texifyidea.util.forcedFirstRequiredParameterAsCommand

/**
 * @author Ruben Schellekens
 */
class GotoCommandDefinitionSymbolContributor : ChooseByNameContributor {

    override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
        val commands = project?.findCommandDefinitions() ?: return emptyArray()
        return commands.asSequence()
                .map { it to it.forcedFirstRequiredParameterAsCommand()?.name }
                .filter { (_, definedName) ->
                    definedName == name || (pattern != null && (definedName ?: "").contains(pattern, ignoreCase = true))
                }
                .mapNotNull { (psi, _) -> NavigationItemUtil.createCommandDefinitionNavigationItem(psi) }
                .toList()
                .toTypedArray()
    }

    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
        val commands = project?.findCommandDefinitions() ?: return emptyArray()
        return commands.mapNotNull { it.forcedFirstRequiredParameterAsCommand()?.name }.toTypedArray()
    }
}