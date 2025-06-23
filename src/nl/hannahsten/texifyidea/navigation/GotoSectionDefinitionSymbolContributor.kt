package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * @author Hannah Schellekens
 */
class GotoSectionDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    //    override fun Project.findElements() = findSectionMarkers()
//
//    override fun LatexCommands.extractName() = requiredParameter(0)
//
//    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createSectionMarkerNavigationItem(this)
    override fun processElements(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        // TODO
    }

    override fun extractName(item: LatexCommands): String? {
        return item.requiredParameter(0)
    }

    override fun createNavigationItem(item: LatexCommands): NavigationItem? {
        return NavigationItemUtil.createSectionMarkerNavigationItem(item)
    }
}