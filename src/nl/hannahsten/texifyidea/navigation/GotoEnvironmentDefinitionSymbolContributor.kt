package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.findEnvironmentDefinitions
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * @author Hannah Schellekens
 */
class GotoEnvironmentDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findEnvironmentDefinitions()

    override fun LatexCommands.extractName() = requiredParameter(0)

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createEnvironmentDefinitionNavigationItem(this)

    override fun createNavigationItem(item: LatexCommands): NavigationItem? {
        TODO("Not yet implemented")
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        TODO("Not yet implemented")
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        TODO("Not yet implemented")
    }
}