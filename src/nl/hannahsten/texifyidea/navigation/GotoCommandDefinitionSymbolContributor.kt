package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
class GotoCommandDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun createNavigationItem(item: LatexCommands): NavigationItem? {
        return NavigationItemUtil.createCommandDefinitionNavigationItem(item)
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        NewDefinitionIndex.processAllKeys(scope, processor)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        NewDefinitionIndex.processByName(name, parameters.project, parameters.searchScope, parameters.idFilter) {
            processor.process(createNavigationItem(it))
        }
    }
}