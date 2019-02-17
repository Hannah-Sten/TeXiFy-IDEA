package nl.rubensten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.findCommandDefinitions
import nl.rubensten.texifyidea.util.forcedFirstRequiredParameterAsCommand

/**
 * @author Ruben Schellekens
 */
class GotoCommandDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findCommandDefinitions()

    override fun LatexCommands.extractName() = forcedFirstRequiredParameterAsCommand()?.name

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createCommandDefinitionNavigationItem(this)
}