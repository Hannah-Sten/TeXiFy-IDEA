package nl.rubensten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.util.findEnvironmentDefinitions
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
class GotoEnvironmentDefinitionSymbolContributor : ChooseByNameContributor {

    override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
        val environments = project?.findEnvironmentDefinitions() ?: return emptyArray()
        return environments.asSequence()
                .map { it to it.requiredParameter(0) }
                .filter { (_, definedName) ->
                    definedName == name || (pattern != null && (definedName ?: "").contains(pattern, ignoreCase = true))
                }
                .mapNotNull { (psi, _) -> NavigationItemUtil.createEnvironmentDefinitionNavigationItem(psi) }
                .toList()
                .toTypedArray()
    }

    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
        val environments = project?.findEnvironmentDefinitions() ?: return emptyArray()
        return environments.mapNotNull { it.requiredParameter(0) }.toTypedArray()
    }
}