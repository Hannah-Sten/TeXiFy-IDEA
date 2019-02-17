package nl.rubensten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.findSectionMarkers
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
class GotoSectionDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findSectionMarkers()

    override fun LatexCommands.extractName() = (this as? LatexCommands)?.requiredParameter(0)

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createSectionMarkerNavigationItem(this)
}