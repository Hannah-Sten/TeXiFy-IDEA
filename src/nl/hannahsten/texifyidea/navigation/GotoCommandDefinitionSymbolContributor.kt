package nl.hannahsten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.findCommandDefinitions
import nl.hannahsten.texifyidea.util.psi.forcedFirstRequiredParameterAsCommand

/**
 * @author Hannah Schellekens
 */
class GotoCommandDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findCommandDefinitions()

    override fun LatexCommands.extractName() = forcedFirstRequiredParameterAsCommand()?.name

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createCommandDefinitionNavigationItem(this)
}