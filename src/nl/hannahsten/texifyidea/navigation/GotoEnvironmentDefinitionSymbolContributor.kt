package nl.hannahsten.texifyidea.navigation

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.findEnvironmentDefinitions
import nl.hannahsten.texifyidea.util.psi.requiredParameter

/**
 * @author Hannah Schellekens
 */
class GotoEnvironmentDefinitionSymbolContributor : TexifyGotoSymbolBase<LatexCommands>() {

    override fun Project.findElements() = findEnvironmentDefinitions()

    override fun LatexCommands.extractName() = requiredParameter(0)

    override fun LatexCommands.createNavigationItem() = NavigationItemUtil.createEnvironmentDefinitionNavigationItem(this)
}