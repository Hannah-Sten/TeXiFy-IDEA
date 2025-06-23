package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.NewDefinitionIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class GotoDefinitionSymbolContributor : ChooseByNameContributorEx {

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        NewDefinitionIndex.processAllKeys(scope, filter, processor)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        NewDefinitionIndex.processByName(name, parameters.project, parameters.searchScope, parameters.idFilter) { def ->
            createNavigationItem(def, name)?.let { processor.process(it) } ?: true
        }
    }

    fun createNavigationItem(item: LatexCommands, name: String): NavigationItem? {
        val defCommand = item.name ?: return null
        if (defCommand in CommandMagic.commandDefinitionsAndRedefinitions) {
            return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_COMMAND)
        }
        if (defCommand in CommandMagic.environmentDefinitions || defCommand in CommandMagic.environmentRedefinitions) {
            return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_ENVIRONMENT)
        }
        return GoToSymbolProvider.BaseNavigationItem(item, name, TexifyIcons.DOT_COMMAND)
    }
}