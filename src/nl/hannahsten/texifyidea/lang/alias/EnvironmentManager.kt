package nl.hannahsten.texifyidea.lang.alias

import arrow.core.nonEmptyListOf
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LabelingEnvironmentInformation
import nl.hannahsten.texifyidea.lang.commands.LatexNewDefinitionCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.requiredParameter

/**
 * Similar to the [CommandManager], this manages aliases of environments.
 */
object EnvironmentManager : AliasManager() {

    /**
     * Maintain information about label parameter locations of environments for which that is applicable.
     * Maps environment name to parameter index of the \begin command, starting from 0 but including the first parameter which is the environment name
     */
    val labelAliasesInfo = mutableMapOf<String, LabelingEnvironmentInformation>()

    override suspend fun findAllAliases(project: Project, aliasSet: Set<String>, indexedDefinitions: Collection<LatexCommands>) {
        val firstAlias = aliasSet.first()

        // Assume the environment that is defined is the first parameter, and that the first part of the definition is in the second
        // e.g. \newenvironment{mytabl}{\begin{tabular}{cc}}{\end{tabular}}
        val definitions = indexedDefinitions.filter { definition ->
            smartReadAction(project) { definition.requiredParameter(1) }?.containsAny(aliasSet.map { "\\begin{$it}" }.toSet()) == true
                // This command always defines an alias for the listings environment
                || (smartReadAction(project) { definition.name } == LatexNewDefinitionCommand.LSTNEWENVIRONMENT.cmd && aliasSet.contains(DefaultEnvironment.LISTINGS.environmentName))
        }
        definitions
            .mapNotNull { smartReadAction(project) { it.requiredParameter(0) } }
            .forEach { registerAlias(firstAlias, it) }

        // Update label parameter position information
        if (aliasSet.intersect(EnvironmentMagic.labelAsParameter).isNotEmpty()) {
            definitions.forEach {
                val definedEnvironment = smartReadAction(project) { it.requiredParameter(0) } ?: return@forEach
                // The label may be in an optional parameter of an environment, but it may also be in other places like a \lstset, so for now we do a text-based search
                val text = smartReadAction(project) { it.requiredParameter(1) } ?: return@forEach
                val index = "label\\s*=\\s*\\{?\\s*#(\\d)".toRegex().find(text)?.groupValues?.getOrNull(1)?.toInt() ?: return@forEach
                labelAliasesInfo[definedEnvironment] = LabelingEnvironmentInformation(nonEmptyListOf(index))
            }
        }
    }
}