package nl.hannahsten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.findSectionMarkers
import nl.hannahsten.texifyidea.util.psi.requiredParameter

/**
 * @author Hannah Schellekens
 */
class GotoSectionDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findSectionMarkers()

    override fun LatexCommands.extractName() = requiredParameter(0)

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createSectionMarkerNavigationItem(this)
}