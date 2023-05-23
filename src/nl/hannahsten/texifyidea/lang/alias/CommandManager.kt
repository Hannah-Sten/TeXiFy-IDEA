package nl.hannahsten.texifyidea.lang.alias

import nl.hannahsten.texifyidea.lang.LabelingCommandInformation
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.psi.requiredParameter
import nl.hannahsten.texifyidea.util.psi.requiredParameters
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.collections.set

/**
 * Manages all available LaTeX commands and their aliases.
 *
 *
 * The `CommandManager` differentiates between two types of commands: aliases and original
 * commands.
 *
 *
 * When a command is registered to the manager ([CommandManager.registerCommand]), it
 * is marked as an *original command*. When the original command is changed into an alias for
 * another command, the command manager still makes sure that you can look up the original
 * functionality of the command. This is to cover cases when for example somebody uses `\renewcommand`.
 *
 *
 * *Aliases* are just what they say on the tin: alternate names for a command. The manager
 * makes you register and look up all aliases for a given command.
 *
 * @author Hannah Schellekens
 */
// Currently it is a singleton, in the future this may be one instance per fileset
object CommandManager : Iterable<String?>, Serializable, AliasManager() {

    /**
     * Maps an original command to the set of current aliases.
     *
     *
     * When adding new aliases, it could happen that some commands lose their functionality in
     * favour of another (using for example `\renewcommand`.) This map maps the original
     * commands to the instance of the set of new aliases.
     *
     *
     * The commands are stored including the backslash.
     *
     *
     * *Example:*
     *
     *
     * Latex:<br></br>
     * `\let\goodepsilon\varepsilon`<br></br>
     * `\let\varepsilon\epsilon`<br></br>
     * `\let\epsilon\goodepsilon`
     *
     *
     * Definitions:<br></br>
     * `A := {\varepsilon, \goodepsilon}`<br></br>
     * `B := {\epsilon}`
     *
     *
     * Map:<br></br>
     * `\epsilon => A`<br></br>
     * `\varepsilon => B`
     */
    private val original = ConcurrentHashMap<String, Set<String>>()

    /**
     * Map commands that define labels to the positions of the label parameter.
     * For example, in \newcommand{\mylabel}[2]{\section{#1}\label{sec:#2} the label parameter is second, so \mylabel maps to [2].
     * It is not so nice to have to maintain this separately, but maintaining
     * parameter position mappings between general alias sets is too much overhead for now.
     */
    val labelAliasesInfo: MutableMap<String, LabelingCommandInformation> =
        CommandMagic.labelDefinitionsWithoutCustomCommands.associateWith {
            LabelingCommandInformation(
                listOf(0),
                true
            )
        }.toMutableMap()

    /**
     * Registers a brand new command to the command manager.
     *
     *
     * The command you register must be unique and will become an original command.
     *
     * @param command
     * A new command (should not exist already) starting with a backslash. *E.g. `\begin`*
     * @throws IllegalArgumentException
     * When the command has already been registered.s
     */
    @Throws(IllegalArgumentException::class)
    fun registerCommand(command: String) {
        super.register(command)
        original[command] = aliases[command] ?: mutableSetOf(command)
    }

    /**
     * Registers a brand new command to the command manager.
     *
     *
     * The command you register must be unique and will become an original command.
     *
     * @param commandNoSlash
     * A new command (should not exist already) starting without the command backslash. *E
     * .g. `begin`*
     * @throws IllegalArgumentException
     * When the command has already been registered.s
     */
    @Throws(IllegalArgumentException::class)
    fun registerCommandNoSlash(commandNoSlash: String) {
        registerCommand("\\" + commandNoSlash)
    }

    /**
     * Register an alias for a given command.
     *
     *
     * The alias will be added to the set of aliases for the given command. The alias will be
     * removed from its original alias set if the alias is an existing command.
     *
     * @param commandNoSlash
     * An existing command to register the alias for starting without the command backslash.
     * *E.g. `begin`*
     * @param aliasNoSlash
     * The alias to register for the command starting without the command backslash. This
     * could be either a new command, or an existing command *E.g. `start`*
     * @throws IllegalArgumentException
     * When the given command already exixts.
     */
    @Throws(IllegalArgumentException::class)
    fun registerAliasNoSlash(commandNoSlash: String, aliasNoSlash: String) {
        registerAlias("\\" + commandNoSlash, "\\" + aliasNoSlash)
    }

    override fun findAllAliases(
        aliasSet: Set<String>,
        indexedDefinitions: Collection<LatexCommands>
    ) {
        val firstAlias = aliasSet.first()

        // Get definitions which define one of the commands in the given command names set
        // These will be aliases of the given set (which is assumed to be an alias set itself)
        indexedDefinitions.filter {
            // Assume the parameter definition has the command being defined in the first required parameter,
            // and the command definition itself in the second
            it.requiredParameter(1)?.containsAny(aliasSet) == true
        }
            .mapNotNull { it.requiredParameter(0) }
            .forEach { registerAlias(firstAlias, it) }

        // Extract label parameter positions
        // Assumes the predefined label definitions all have the label parameter in the same position
        // For example, in \newcommand{\mylabel}[2]{\section{#1}\label{sec:#2}} we want to parse out the 2 in #2
        if (aliasSet.intersect(CommandMagic.labelDefinitionsWithoutCustomCommands).isNotEmpty()) {
            indexedDefinitions.forEach { commandDefinition ->
                val definedCommand = commandDefinition.requiredParameter(0) ?: return@forEach
                if (definedCommand.isBlank()) return@forEach

                val isFirstParameterOptional = commandDefinition.parameterList.filter { it.optionalParam != null }.size > 1

                val parameterCommands = commandDefinition.requiredParameters().getOrNull(1)
                    ?.requiredParamContentList
                    ?.flatMap { it.childrenOfType(LatexCommands::class) }
                    ?.asSequence()

                // Positions of label parameters in the custom commands (starting from 0)
                val positions = parameterCommands
                    ?.filter { it.name in CommandMagic.labelDefinitionsWithoutCustomCommands }
                    ?.mapNotNull { it.requiredParameter(0) }
                    ?.mapNotNull {
                        if (it.indexOf('#') != -1) {
                            it.getOrNull(it.indexOf('#') + 1)
                        }
                        else null
                    }
                    ?.map(Character::getNumericValue)
                    // LaTeX starts from 1, we from 0 (consistent with how we count required parameters)
                    ?.map { it - 1 }
                    // For the moment we only consider required parameters and ignore the optional one
                    ?.map { if (isFirstParameterOptional) it - 1 else it }
                    ?.filter { it >= 0 }
                    ?.toList() ?: return@forEach
                if (positions.isEmpty()) return@forEach

                // Check if there is a command which increases a counter before the \label
                // If so, the \label just labels the counter increasing command, and not whatever will appear before usages of the custom labeling command
                val definitionContainsIncreaseCounterCommand =
                    parameterCommands.takeWhile { it.name !in CommandMagic.labelDefinitionsWithoutCustomCommands }
                        .any { it.name in CommandMagic.increasesCounter }

                val prefix = parameterCommands.filter { it.name in CommandMagic.labelDefinitionsWithoutCustomCommands }
                    .mapNotNull { it.requiredParameter(0) }
                    .map {
                        if (it.indexOf('#') != -1) {
                            val prefix = it.substring(0, it.indexOf('#'))
                            prefix.ifBlank { "" }
                        }
                        else ""
                    }.firstOrNull() ?: ""

                labelAliasesInfo[definedCommand] =
                    LabelingCommandInformation(positions, !definitionContainsIncreaseCounterCommand, prefix)
            }
        }
    }

    /**
     * Get an unmodifiable set with all the aliases for the given command.
     *
     * @param commandNoSlash
     * An existing command to get all aliases of starting without the command backslash.
     * *E.g. `begin`*
     * @return An unmodifiable set of all aliases including the command itself. All aliases include
     * a command backslash.
     * @throws IllegalArgumentException
     * When the given command is not registered.
     */
    @Throws(IllegalArgumentException::class)
    fun getAliasesNoSlash(commandNoSlash: String): Set<String> {
        return getAliases("\\" + commandNoSlash)
    }

    /**
     * Get an unmodifiable set with all the aliases for an original command.
     *
     *
     * A command is original when it first gets registered to the CommandManager. This way you
     * can get all updated aliases in case the given command no longer provides the original
     * functionality.
     *
     * @param originalCommand
     * The original command of which to get the aliases of starting with the command
     * backslash. *E.g. `\begin`*
     * @return An unmodifiable set of all aliases of the original command. All aliases include a
     * command backslash.
     * @throws IllegalArgumentException
     * When the original command has not been registered.
     */
    @Throws(IllegalArgumentException::class)
    fun getAliasesFromOriginal(originalCommand: String): Set<String> {
        require(isOriginal(originalCommand)) { "originalCommand '$originalCommand' has not been registered" }
        return original[originalCommand]?.toSet() ?: emptySet()
    }

    /**
     * Get an unmodifiable set with all the aliases for an original command.
     *
     *
     * A command is original when it first gets registered to the CommandManager. This way you
     * can get all updated aliases in case the given command no longer provides the original
     * functionality.
     *
     * @param originalCommandNoSlash
     * The original command of which to get the aliases of starting without the command
     * backslash. *E.g. `begin`*
     * @return An unmodifiable set of all aliases of the original command. All aliases include a
     * command backslash.
     * @throws IllegalArgumentException
     * When the original command has not been registered.
     */
    @Throws(IllegalArgumentException::class)
    fun getAliasesFromOriginalNoSlash(originalCommandNoSlash: String): Set<String> {
        return getAliasesFromOriginal("\\" + originalCommandNoSlash)
    }

    /**
     * Get an unmodifiable set of all registered commands (including aliases).
     *
     * @return An unmodifiable set of all commands. All command include a command backslash.
     */
    val allCommands: Set<String>
        get() = Collections.unmodifiableSet(aliases.keys)

    /**
     * Checks if the given command has been registered to the command manager.
     *
     * @param commandNoSlash
     * The command to check if it has been registered starting without the command
     * backslash. *E.g. `begin`*
     * @return `true` if the given command is present in the command manager, `false`
     * otherwise.
     */
    fun isRegisteredNoSlash(commandNoSlash: String): Boolean {
        return isRegistered("\\" + commandNoSlash)
    }

    /**
     * Checks if the given command is an original command.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     *
     * @param command
     * The command to check if it is an original command starting with the command
     * backslash. *E.g. `\begin`*
     * @return `true` if the given command is an original command in the command manager,
     * `false` if not.
     */
    fun isOriginal(command: String): Boolean {
        return original.containsKey(command)
    }

    /**
     * Checks if the given command is an original command.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     *
     * @param commandNoSlash
     * The command to check if it is an original command starting without the command
     * backslash. *E.g. `begin`*
     * @return `true` if the given command is an original command in the command manager,
     * `false` if not.
     */
    fun isOriginalNoSlash(commandNoSlash: String): Boolean {
        return isOriginal("\\" + commandNoSlash)
    }

    /**
     * Removes all commands from the command manager.
     */
    fun clear() {
        aliases.clear()
        original.clear()
    }

    /**
     * Gets the amount of registered aliases.
     */
    fun size(): Int {
        return aliases.size
    }

    /**
     * Gets the amount of original commands.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    fun originalSize(): Int {
        return original.size
    }

    /**
     * Create a stream of aliases.
     */
    fun stream(): Stream<String?> {
        return StreamSupport.stream(spliterator(), false)
    }

    /**
     * Create a parallel stream of aliases.
     */
    fun parallelStream(): Stream<String?> {
        return StreamSupport.stream(spliterator(), true)
    }

    /**
     * Create a stream of original commands.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    fun streamOriginal(): Stream<String> {
        return StreamSupport.stream(spliteratorOriginal(), false)
    }

    /**
     * Create a parallel stream of original commands.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    fun parallelStreamOriginal(): Stream<String> {
        return StreamSupport.stream(spliteratorOriginal(), true)
    }

    /**
     * Iterator for aliases.
     */
    override fun iterator(): MutableIterator<String> {
        return aliases.keys.iterator()
    }

    /**
     * Spliterator for aliases.
     */
    override fun spliterator(): Spliterator<String?> {
        return aliases.keys.spliterator()
    }

    /**
     * Iterator for original commands.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    fun iteratorOriginal(): Iterator<String> {
        return original.keys.iterator()
    }

    /**
     * Spliterator for original commands.
     *
     *
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    private fun spliteratorOriginal(): Spliterator<String> {
        return original.keys.spliterator()
    }

    override fun toString(): String {
        return "CommandManager{" + "aliases=" + aliases +
            ", original=" + original +
            '}'
    }
}