package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.forcedFirstRequiredParameterAsCommand

/**
 * @author Hannah Schellekens
 */
class GotoCommandDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun createNavigationItem(item: LatexCommands): NavigationItem? {
        return NavigationItemUtil.createCommandDefinitionNavigationItem(item)
    }

    override fun processElements(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        NewSpecialCommandsIndex.processCommandDef(scope, filter, processor)
    }

    override fun extractName(item: LatexCommands): String? {
        return item.forcedFirstRequiredParameterAsCommand()?.name
    }
}