package nl.hannahsten.texifyidea.lang.alias

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.projectSearchScope
import java.util.concurrent.ConcurrentHashMap
// TODO: Very slow
/**
 * Manage aliases for commands and environments.
 */
abstract class AliasManager {

    /**
     * Maps a command to a set of aliases including the command itself.
     * Similar for environments.
     *
     * All elements of the set are in the map as keys as well linking to the set in which they
     * are themselves. This means that the set of all keys that are aliases of each other all
     * link to the same set of aliases. This ensures that you only have to modify the alias sets
     * once and automatically update for all others.
     *
     *
     * The commands are stored including the backslash.
     *
     *
     * *Example:*
     *
     *
     * Definitions:<br></br>
     * `A := {\one, \een, \ein}`<br></br>
     * `B := {\twee}`<br></br>
     * `C := {\drie, \three}`
     *
     *
     * Map:<br></br>
     * `\one => A`<br></br>
     * `\een => A`<br></br>
     * `\ein => A`<br></br>
     * `\twee => B`<br></br>
     * `\three => C`<br></br>
     * `\drie => C`
     */
    open val aliases = ConcurrentHashMap<String, MutableSet<String>>()

    /**
     * We have to somehow know when we need to look for new aliases.
     * We do this by keeping track of the \newcommand-like commands in the index,
     * and when this changes we go gather new aliases.
     */
    open var indexedCommandDefinitions = setOf<LatexCommands>()

    /**
     * Register a new item, which creates a new alias set.
     */
    fun register(alias: String) {
        synchronized(aliases) {
            aliases[alias] = mutableSetOf(alias)
        }
    }

    /**
     * Checks if the given alias has been registered to the alias manager.
     *
     * @param alias
     * The alias to check if it has been registered
     * *E.g. `\begin`*
     * @return `true` if the given alias is present in the alias manager, `false`
     * otherwise.
     */
    fun isRegistered(alias: String): Boolean {
        synchronized(aliases) {
            return aliases.containsKey(alias)
        }
    }

    /**
     * Register an alias for a given item.
     *
     *
     * The alias will be added to the set of aliases for the given item.
     *
     * @param item
     * An existing item to register the alias for starting with a backslash. *E.g.
     * `\begin`*
     * @param alias
     * The alias to register for the item. This could be either
     * a new item, or an existing item *E.g. `\start`*
     * @param isRedefinition If the alias is being redefined, remove the original definition
     */
    @Synchronized
    fun registerAlias(item: String, alias: String, isRedefinition: Boolean = false) {
        synchronized(aliases) {
            val aliasSet = aliases[item] ?: mutableSetOf()

            // If the alias is already assigned and we are redefining it: unassign it.
            if (isRedefinition && isRegistered(alias)) {
                val previousAliases = aliases[alias]
                previousAliases?.remove(alias)
                aliases.remove(alias)
            }
            aliasSet.add(alias)
            aliases[alias] = aliasSet
        }
    }

    /**
     * Get an unmodifiable set with all the aliases for the given item.
     *
     * @param item
     * An existing item to get all aliases of starting with a backslash. *E.g. `\begin`*
     * @return An unmodifiable set of all aliases including the item itself. Returns the empty set if the item is not registered.
     */
    fun getAliases(item: String): Set<String> {
        if (!isRegistered(item)) return emptySet()
        synchronized(aliases) {
            return aliases[item]?.toSet() ?: emptySet()
        }
    }

    /**
     * If needed (based on the number of indexed \newcommand-like commands) check for new aliases of the given alias set. This alias can be any alias of its alias set.
     * If the alias set is not yet registered, it will be registered as a new alias set.
     */
    fun updateAliases(aliasSet: Collection<String>, project: Project) {
        // Register if needed
        if (aliasSet.isEmpty()) return
        val firstAlias = aliasSet.first()
        var wasRegistered = false
        if (!isRegistered(firstAlias)) {
            register(firstAlias)
            wasRegistered = true
            aliasSet.forEach { registerAlias(firstAlias, it) }
        }

        // If the command name itself is not directly in the given set, check if it is perhaps an alias of a command in the set
        // Uses projectScope now, may be improved to filesetscope, TODO
        val indexedCommandDefinitions = NewSpecialCommandsIndex.getRegularCommandDef(project, project.projectSearchScope)

        // Check if something has changed (the number of indexed command might be the same while the content is different), and if so, update the aliases.
        // Also do this the first time something is registered, because then we have to update aliases as well
        val hasChanged = this.indexedCommandDefinitions != indexedCommandDefinitions
        // If a refresh is already in progress, no need to start another one, but no need to block this thread either
        if (hasChanged || wasRegistered) {
            // Update everything, since it is difficult to know beforehand what aliases could be added or not
            // Alternatively we could save a numberOfIndexedCommandDefinitions per alias set, and only update the
            // requested alias set (otherwise only the first alias set requesting an update will get it)
            // We have to deepcopy the set of alias sets before iterating over it, because we want to modify aliases
            val deepCopy = aliases.values.toSet().map { it.toSet() }
            for (copiedAliasSet in deepCopy) {
                findAllAliases(copiedAliasSet, indexedCommandDefinitions)
            }

            this.indexedCommandDefinitions = indexedCommandDefinitions.toSet()
        }
    }

    /**
     * Find all aliases that are defined in the [indexedDefinitions] and register them.
     */
    abstract fun findAllAliases(
        aliasSet: Set<String>,
        indexedDefinitions: Collection<LatexCommands>
    )
}