package nl.rubensten.texifyidea.lang;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * TODO: A whole lot.
 *
 * @author Ruben Schellekens
 */
public class CommandManager implements Iterable<String>, Serializable {

    private static final long serialVersionUID = 192873489129843L;

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
    public boolean isRegistered(String command) {
        return aliases.containsKey(command);
    }

    /**
     * TODO: implement.
     */
    public boolean isRegisteredNoSlash(String commandNoSlash) {
        return false;
    }

    /**
     * TODO: docs.
     */
    public boolean isOriginal(String command) {
        return original.containsKey(command);
    }

    /**
     * TODO: implement.
     */
    public boolean isOriginalNoSlash(String commandNoSlash) {
        return false;
    }

    /**
     * TODO: docs.
     */
    public void clear() {
        aliases.clear();
        original.clear();
    }

    /**
     * TODO: docs.
     */
    public int size() {
        return aliases.size();
    }

    /**
     * TODO: docs.
     */
    public int originalSize() {
        return original.size();
    }

    /**
     * TODO: docs.
     */
    public Stream<String> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * TODO: docs.
     */
    public Stream<String> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * TODO: docs.
     */
    @Override
    public Iterator<String> iterator() {
        return aliases.keySet().iterator();
    }

    /**
     * TODO: docs.
     */
    @Override
    public Spliterator<String> spliterator() {
        return aliases.keySet().spliterator();
    }

}
