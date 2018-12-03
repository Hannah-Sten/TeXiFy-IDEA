package nl.rubensten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.util.extractLabelName
import nl.rubensten.texifyidea.util.findLabels

/**
 * @author Ruben Schellekens
 */
class GotoLabelSymbolContributor : ChooseByNameContributor {

    override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
        val labels = project?.findLabels() ?: return emptyArray()
        return labels.asSequence()
                .filter { it.extractLabelName() == name || (pattern != null && it.extractLabelName().contains(pattern, ignoreCase = true)) }
                .mapNotNull { NavigationItemUtil.createLabelNavigationItem(it) }
                .toList()
                .toTypedArray()
    }

    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
        // includeNonProjectItems is ignored until we add latex SDKs.
        val labels = project?.findLabels() ?: return emptyArray()
        return labels.map { it.extractLabelName() }.toTypedArray()
    }
}