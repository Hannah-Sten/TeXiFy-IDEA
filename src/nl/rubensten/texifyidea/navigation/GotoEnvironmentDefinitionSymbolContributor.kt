package nl.rubensten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.findEnvironmentDefinitions
import nl.rubensten.texifyidea.util.requiredParameter

/**
 * @author Ruben Schellekens
 */
class GotoEnvironmentDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findEnvironmentDefinitions()

    override fun LatexCommands.extractName() = requiredParameter(0)

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createEnvironmentDefinitionNavigationItem(this)
}