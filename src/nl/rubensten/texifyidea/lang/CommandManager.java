package nl.rubensten.texifyidea.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: A whole lot.
 *
 * @author Ruben Schellekens
 */
public class CommandManager {

    /**
     * TODO: docs.
     */
    private static final CommandManager INSTANCE = new CommandManager();

    /**
     * TODO: docs.
     */
    private Map<String, Set<String>> aliases;

    /**
     * TODO: docs.
     */
    private Map<String, Set<String>> original;

    /**
     * TODO: docs.
     */
    private static CommandManager getInstance() {
        return INSTANCE;
    }

    private CommandManager() {
        aliases = new HashMap<>();
        original = new HashMap<>();
    }

    /**
     * TODO: implement.
     */
    public void registerCommand(String command) throws IllegalArgumentException {
    }

    /**
     * TODO: implement.
     */
    public void registerCommandNoSlash(String commandNoSlash) throws IllegalArgumentException {
    }

    /**
     * TODO: implement.
     */
    public void registerAlias(String command, String alias) throws IllegalArgumentException {
    }

    /**
     * TODO: implement.
     */
    public void registerAliasNoSlash(String commandNoSlash, String aliasNoSlash) throws IllegalArgumentException {
    }

    /**
     * TODO: implement.
     */
    public Set<String> getAliases(String command) {
        return null;
    }

    /**
     * TODO: implement.
     */
    public Set<String> getAliasesNoSlash(String commandNoSlash) {
        return null;
    }

    /**
     * TODO: implement.
     */
    public Set<String> getAliasesFromOriginal(String originalCommand) {
        return null;
    }

    /**
     * TODO: implement.
     */
    public Set<String> getAliasesFromOriginalNoSlash(String originalCommandNoSlash) {
        return null;
    }

    /**
     * TODO: implement.
     */
    public Set<String> getAllCommands() {
        return null;
    }

    /**
     * TODO: docs.
     */
    public void clear() {
        aliases.clear();
        original.clear();
    }

}
