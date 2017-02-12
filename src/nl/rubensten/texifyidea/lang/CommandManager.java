package nl.rubensten.texifyidea.lang;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Manages all available LaTeX commands and their aliases.
 * <p>
 * The {@code CommandManager} differentiates between two types of commands: aliases and original
 * commands.
 * <p>
 * When a command is registered to the manager ({@link CommandManager#registerCommand(String)}), it
 * is marked as an <i>original command</i>. When the original command is changed into an alias for
 * another command, the command manager still makes sure that you can look up the original
 * functionality of the command. This is to cover cases when for example somebody uses {@code
 * \renewcommand}.
 * <p>
 * <i>Aliases</i> are just what they say on the tin: alternate names for a command. The manager
 * makes you register and look up all aliases for a given command.
 * <p>
 * Singleton. Access via {@link CommandManager#getInstance()}.
 *
 * @author Ruben Schellekens
 */
public class CommandManager implements Iterable<String>, Serializable {

    private static final long serialVersionUID = 192873489129843L;

    /**
     * Singleton instance of CommandManager.
     */
    private static final CommandManager INSTANCE = new CommandManager();

    /**
     * Maps a command to a set of aliases including the command itself.
     * <p>
     * All elements of the set are in the map as keys as well linking to the set in which they
     * are themselves. This means that the set of all keys that are aliases of each other all
     * link to the same set of aliases. This ensures that you only have to modify the alias sets
     * once and automatically update for all others.
     * <p>
     * The commands are stored including the backslash.
     * <p>
     * <i>Example:</i>
     * <p>
     * Definitions:<br>
     * {@code A := {\one, \een, \ein}}<br>
     * {@code B := {\twee}}<br>
     * {@code C := {\drie, \three}}
     * <p>
     * Map:<br>
     * {@code \one => A}<br>
     * {@code \een => A}<br>
     * {@code \ein => A}<br>
     * {@code \twee => B}<br>
     * {@code \three => C}<br>
     * {@code \drie => C}
     */
    private Map<String, Set<String>> aliases;

    /**
     * Maps an original command to the set of current aliases.
     * <p>
     * When adding new aliases, it could happen that some commands lose their functionality in
     * favour of another (using for example {@code \renewcommand}.) This map maps the original
     * commands to the instance of the set of new aliases.
     * <p>
     * The commands are stored including the backslash.
     * <p>
     * <i>Example:</i>
     * <p>
     * Latex:<br>
     * {@code \let\goodepsilon\varepsilon}<br>
     * {@code \let\varepsilon\epsilon}<br>
     * {@code \let\epsilon\goodepsilon}
     * <p>
     * Definitions:<br>
     * {@code A := {\varepsilon, \goodepsilon}}<br>
     * {@code B := {\epsilon}}
     * <p>
     * Map:<br>
     * {@code \epsilon => A}<br>
     * {@code \varepsilon => B}
     */
    private Map<String, Set<String>> original;

    /**
     * Get the singleton instance.
     */
    public static CommandManager getInstance() {
        return INSTANCE;
    }

    private CommandManager() {
        aliases = new HashMap<>();
        original = new HashMap<>();
    }

    /**
     * Registers a brand new command to the command manager.
     * <p>
     * The command you register must be unique and will become an original command.
     *
     * @param command
     *         A new command (should not exist already) starting with a backslash. <i>E.g. {@code
     *         \begin}</i>
     * @throws IllegalArgumentException
     *         When the command has already been registered.s
     */
    public void registerCommand(String command) throws IllegalArgumentException {
        if (isRegistered(command)) {
            throw new IllegalArgumentException(
                    "command '" + command + "' has already been registered");
        }

        Set<String> aliasSet = new HashSet<>();
        aliasSet.add(command);
        original.put(command, aliasSet);
        aliases.put(command, aliasSet);
    }

    /**
     * Registers a brand new command to the command manager.
     * <p>
     * The command you register must be unique and will become an original command.
     *
     * @param commandNoSlash
     *         A new command (should not exist already) starting without the command backslash. <i>E
     *         .g. {@code begin}</i>
     * @throws IllegalArgumentException
     *         When the command has already been registered.s
     */
    public void registerCommandNoSlash(String commandNoSlash) throws IllegalArgumentException {
        registerCommand("\\" + commandNoSlash);
    }

    /**
     * Register an alias for a given command.
     * <p>
     * The alias will be added to the set of aliases for the given command. The alias will be
     * removed from its original alias set if the alias is an existing command.
     *
     * @param command
     *         An existing command to register the alias for starting with a backslash. <i>E.g.
     *         {@code \begin}</i>
     * @param alias
     *         The alias to register for the command starting with a backslash. This could be either
     *         a new command, or an existing command <i>E.g. {@code \start}</i>
     * @throws IllegalArgumentException
     *         When the given command does not exixt.
     */
    public void registerAlias(String command, String alias) throws IllegalArgumentException {
        if (!isRegistered(command)) {
            throw new IllegalArgumentException(
                    "command '" + command + "' has not been registererd");
        }

        Set<String> aliasSet = aliases.get(command);

        // If the alias is already assigned: unassign it.
        if (isRegistered(alias)) {
            Set<String> previousAliases = aliases.get(alias);
            previousAliases.remove(alias);
            aliases.remove(alias);
        }

        aliasSet.add(alias);
        aliases.put(alias, aliasSet);
    }

    /**
     * Register an alias for a given command.
     * <p>
     * The alias will be added to the set of aliases for the given command. The alias will be
     * removed from its original alias set if the alias is an existing command.
     *
     * @param commandNoSlash
     *         An existing command to register the alias for starting without the command backslash.
     *         <i>E.g. {@code begin}</i>
     * @param aliasNoSlash
     *         The alias to register for the command starting without the command backslash. This
     *         could be either a new command, or an existing command <i>E.g. {@code start}</i>
     * @throws IllegalArgumentException
     *         When the given command already exixts.
     */
    public void registerAliasNoSlash(String commandNoSlash, String aliasNoSlash) throws IllegalArgumentException {
        registerAlias("\\" + commandNoSlash, "\\" + aliasNoSlash);
    }

    /**
     * Get an unmodifiable set with all the aliases for the given command.
     *
     * @param command
     *         An existing command to get all aliases of starting with a backslash. <i>E.g. {@code
     *         \begin}</i>
     * @return An unmodifiable set of all aliases including the command itself. All aliases include
     * a command backslash.
     * @throws IllegalArgumentException
     *         When the given command is not registered.
     */
    public Set<String> getAliases(String command) throws IllegalArgumentException {
        if (!isRegistered(command)) {
            throw new IllegalArgumentException(
                    "command '" + command + "' has not been registered");
        }

        return Collections.unmodifiableSet(aliases.get(command));
    }

    /**
     * Get an unmodifiable set with all the aliases for the given command.
     *
     * @param commandNoSlash
     *         An existing command to get all aliases of starting without the command backslash.
     *         <i>E.g. {@code begin}</i>
     * @return An unmodifiable set of all aliases including the command itself. All aliases include
     * a command backslash.
     * @throws IllegalArgumentException
     *         When the given command is not registered.
     */
    public Set<String> getAliasesNoSlash(String commandNoSlash) throws IllegalArgumentException {
        return getAliases("\\" + commandNoSlash);
    }

    /**
     * Get an unmodifiable set with all the aliases for an original command.
     * <p>
     * A command is original when it first gets registered to the CommandManager. This way you
     * can get all updated aliases in case the given command no longer provides the original
     * functionality.
     *
     * @param originalCommand
     *         The original command of which to get the aliases of starting with the command
     *         backslash. <i>E.g. {@code \begin}</i>
     * @return An unmodifiable set of all aliases of the original command. All aliases include a
     * command backslash.
     * @throws IllegalArgumentException
     *         When the original command has not been registered.
     */
    public Set<String> getAliasesFromOriginal(String originalCommand) throws
            IllegalArgumentException {
        if (!isOriginal(originalCommand)) {
            throw new IllegalArgumentException(
                    "originalCommand '" + originalCommand + "' has not been registered");
        }

        return Collections.unmodifiableSet(original.get(originalCommand));
    }

    /**
     * Get an unmodifiable set with all the aliases for an original command.
     * <p>
     * A command is original when it first gets registered to the CommandManager. This way you
     * can get all updated aliases in case the given command no longer provides the original
     * functionality.
     *
     * @param originalCommandNoSlash
     *         The original command of which to get the aliases of starting without the command
     *         backslash. <i>E.g. {@code begin}</i>
     * @return An unmodifiable set of all aliases of the original command. All aliases include a
     * command backslash.
     * @throws IllegalArgumentException
     *         When the original command has not been registered.
     */
    public Set<String> getAliasesFromOriginalNoSlash(String originalCommandNoSlash) throws
            IllegalArgumentException {
        return getAliasesFromOriginal("\\" + originalCommandNoSlash);
    }

    /**
     * Get an unmodifiable set of all registered commands (including aliases).
     *
     * @return An unmodifiable set of all commands. All command include a command backslash.
     */
    public Set<String> getAllCommands() {
        return Collections.unmodifiableSet(aliases.keySet());
    }

    /**
     * Checks if the given command has been registered to the command manager.
     *
     * @param command
     *         The command to check if it has been registered starting with the command backslash.
     *         <i>E.g. {@code \begin}</i>
     * @return {@code true} if the given command is present in the command manager, {@code false}
     * otherwise.
     */
    public boolean isRegistered(String command) {
        return aliases.containsKey(command);
    }

    /**
     * Checks if the given command has been registered to the command manager.
     *
     * @param commandNoSlash
     *         The command to check if it has been registered starting without the command
     *         backslash. <i>E.g. {@code begin}</i>
     * @return {@code true} if the given command is present in the command manager, {@code false}
     * otherwise.
     */
    public boolean isRegisteredNoSlash(String commandNoSlash) {
        return isRegistered("\\" + commandNoSlash);
    }

    /**
     * Checks if the given command is an original command.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     *
     * @param command
     *         The command to check if it is an original command starting with the command
     *         backslash. <i>E.g. {@code \begin}</i>
     * @return {@code true} if the given command is an original command in the command manager,
     * {@code false} if not.
     */
    public boolean isOriginal(String command) {
        return original.containsKey(command);
    }

    /**
     * Checks if the given command is an original command.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     *
     * @param commandNoSlash
     *         The command to check if it is an original command starting without the command
     *         backslash. <i>E.g. {@code begin}</i>
     * @return {@code true} if the given command is an original command in the command manager,
     * {@code false} if not.
     */
    public boolean isOriginalNoSlash(String commandNoSlash) {
        return isOriginal("\\" + commandNoSlash);
    }

    /**
     * Removes all commands from the command manager.
     */
    public void clear() {
        aliases.clear();
        original.clear();
    }

    /**
     * Gets the amount of registered aliases.
     */
    public int size() {
        return aliases.size();
    }

    /**
     * Gets the amount of original commands.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    public int originalSize() {
        return original.size();
    }

    /**
     * Create a stream of aliases.
     */
    public Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Create a parallel stream of aliases.
     */
    public Stream<String> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Create a stream of original commands.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    public Stream<String> streamOriginal() {
        return StreamSupport.stream(spliteratorOriginal(), false);
    }

    /**
     * Create a parallel stream of original commands.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    public Stream<String> parallelStreamOriginal() {
        return StreamSupport.stream(spliteratorOriginal(), true);
    }

    /**
     * Iterator for aliases.
     */
    @Override
    public Iterator<String> iterator() {
        return aliases.keySet().iterator();
    }

    /**
     * Spliterator for aliases.
     */
    @Override
    public Spliterator<String> spliterator() {
        return aliases.keySet().spliterator();
    }

    /**
     * Iterator for original commands.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    public Iterator<String> iteratorOriginal() {
        return original.keySet().iterator();
    }

    /**
     * Spliterator for original commands.
     * <p>
     * A command is original when it gets registered to the command manager instead of being set
     * as alias.
     */
    public Spliterator<String> spliteratorOriginal() {
        return original.keySet().spliterator();
    }

    @Override
    public String toString() {
        return "CommandManager{" + "aliases=" + aliases +
                ", original=" + original +
                '}';
    }
}
