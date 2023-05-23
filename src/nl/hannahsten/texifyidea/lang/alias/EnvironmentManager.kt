package nl.hannahsten.texifyidea.lang.alias

import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.psi.requiredParameter

/**
 * Similar to the [CommandManager], this manages aliases of environments.
 */
object EnvironmentManager : AliasManager() {

    override fun findAllAliases(aliasSet: Set<String>, indexedDefinitions: Collection<LatexCommands>) {
        val firstAlias = aliasSet.first()

        // Assume the environment that is defined is the first parameter, and that the first part of the definition is in the second
        // e.g. \newenvironment{mytabl}{\begin{tabular}{cc}}{\end{tabular}}
        indexedDefinitions.filter { definition ->
            definition.requiredParameter(1)?.containsAny(aliasSet.map { "\\begin{$it}" }.toSet()) == true
        }
            .mapNotNull { it.requiredParameter(0) }
            .forEach { registerAlias(firstAlias, it) }
    }
}