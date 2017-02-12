package nl.rubensten.texifyidea.lang;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link CommandManager}.
 *
 * @author Ruben Schellekens
 */
public class CommandManagerTest {

    private CommandManager manager;

    @Before
    public void setup() {
        manager = CommandManager.getInstance();
    }

    private void resetup(String[][] aliasGroups) {
        manager.clear();

        if (aliasGroups == null) {
            return;
        }

        for (String[] group : aliasGroups) {
            if (group.length == 0) {
                continue;
            }

            registerCommandSwitchSlash(group[0]);
            for (int i = 1; i < group.length; i++) {
                registerAliasSwitchSlash(group[0], group[i]);
            }
        }
    }

    private Set<String> getAliasesFromOriginalSwitchSlash(String command) {
        if (command.startsWith("\\")) {
            return manager.getAliasesFromOriginal(command);
        }
        else {
            return manager.getAliasesFromOriginalNoSlash(command);
        }
    }

    private Set<String> getAliasesSwitchSlash(String command) {
        if (command.startsWith("\\")) {
            return manager.getAliases(command);
        }
        else {
            return manager.getAliasesNoSlash(command);
        }
    }

    private boolean isOriginalSwithSlash(String command) {
        if (command.startsWith("\\")) {
            return manager.isOriginal(command);
        }
        else {
            return manager.isOriginalNoSlash(command);
        }
    }

    private boolean isRegisteredSwitchSlash(String command) {
        if (command.startsWith("\\")) {
            return manager.isRegistered(command);
        }
        else {
            return manager.isRegisteredNoSlash(command);
        }
    }

    private void registerAliasSwitchSlash(String command, String alias) {
        if (command.startsWith("\\")) {
            manager.registerAlias(command, alias);
        }
        else {
            manager.registerAliasNoSlash(command, alias);
        }
    }

    private void registerCommandSwitchSlash(String command) {
        if (command.startsWith("\\")) {
            manager.registerCommand(command);
        }
        else {
            manager.registerCommandNoSlash(command);
        }
    }

    private void checkDefaultAliases(Function<String, Set<String>> method) {
        assertEquals("Alias 1: contains \\one",
                true, method.apply("\\one").contains("\\one"));
        assertEquals("Alias 1: contains \\un",
                true, method.apply("\\ein").contains("\\un"));
        assertEquals("Alias 1: contains \\een",
                true, method.apply("\\un").contains("\\een"));
        assertEquals("Alias 1: contains \\ein",
                true, method.apply("\\een").contains("\\ein"));

        assertEquals("Alias 2: contains \\two",
                true, method.apply("\\two").contains("\\two"));
        assertEquals("Alias 2: contains \\deux",
                true, method.apply("\\zwei").contains("\\deux"));
        assertEquals("Alias 2: contains \\twee",
                true, method.apply("\\deux").contains("\\twee"));
        assertEquals("Alias 2: contains \\zwei",
                true, method.apply("\\twee").contains("\\zwei"));
        assertEquals("Alias 3: only contains \\three",
                true, method.apply("\\three").contains("\\three") &&
                        method.apply("\\three").size() == 1);

        assertEquals("Alias 10: contains \\ten",
                true, method.apply("\\tien").contains("\\ten"));
        assertEquals("Alias 10: contains \\tien",
                true, method.apply("\\ten").contains("\\tien"));
    }

    private void checkDefaultAliasesNoSlash(Function<String, Set<String>> method) {
        assertEquals("Alias 1: contains one",
                true, method.apply("one").contains("\\one"));
        assertEquals("Alias 1: contains un",
                true, method.apply("ein").contains("\\un"));
        assertEquals("Alias 1: contains een",
                true, method.apply("un").contains("\\een"));
        assertEquals("Alias 1: contains ein",
                true, method.apply("een").contains("\\ein"));

        assertEquals("Alias 2: contains two",
                true, method.apply("two").contains("\\two"));
        assertEquals("Alias 2: contains deux",
                true, method.apply("zwei").contains("\\deux"));
        assertEquals("Alias 2: contains twee",
                true, method.apply("deux").contains("\\twee"));
        assertEquals("Alias 2: contains zwei",
                true, method.apply("twee").contains("\\zwei"));

        assertEquals("Alias 3: only contains three",
                true, method.apply("three").contains("\\three") &&
                        method.apply("three").size() == 1);

        assertEquals("Alias 10: contains ten",
                true, method.apply("tien").contains("\\ten"));
        assertEquals("Alias 10: contains tien",
                true, method.apply("ten").contains("\\tien"));
    }

    private String[][] getDefaultAliasGroups() {
        return new String[][] {
                new String[] {"\\een", "\\one", "\\un", "\\ein"},
                new String[] {"\\twee", "\\two", "\\deux", "\\zwei"},
                new String[] {"\\three"},
                new String[] {"\\tien", "\\ten"}
        };
    }

    @Test
    public void registerCommand() throws Exception {
        resetup(null);

        manager.registerCommand("\\begin");
        manager.registerCommand("\\alpha");
        manager.registerCommand("\\beta");
        manager.registerCommand("\\gamma");
        manager.registerCommand("\\delta");

        final int EXPECTED_SIZE = 5;

        assertEquals("New count", EXPECTED_SIZE, manager.originalSize());
        assertEquals("Contains begin", true, manager.isRegistered("\\begin"));
        assertEquals("Contains alpha", true, manager.isRegistered("\\alpha"));
        assertEquals("Contains beta", true, manager.isRegistered("\\beta"));
        assertEquals("Contains gamma", true, manager.isRegistered("\\gamma"));
        assertEquals("Contains delta", true, manager.isRegistered("\\delta"));
    }

    @Test
    public void registerCommandNoSlash() throws Exception {
        resetup(null);

        manager.registerCommandNoSlash("begin");
        manager.registerCommandNoSlash("alpha");
        manager.registerCommandNoSlash("beta");
        manager.registerCommandNoSlash("gamma");
        manager.registerCommandNoSlash("delta");

        final int EXPECTED_SIZE = 5;

        assertEquals("New count", EXPECTED_SIZE, manager.originalSize());
        assertEquals("Contains begin", true, manager.isRegistered("\\begin"));
        assertEquals("Contains alpha", true, manager.isRegistered("\\alpha"));
        assertEquals("Contains beta", true, manager.isRegistered("\\beta"));
        assertEquals("Contains gamma", true, manager.isRegistered("\\gamma"));
        assertEquals("Contains delta", true, manager.isRegistered("\\delta"));
    }

    @Test
    public void registerAlias() throws Exception {
        resetup(null);

        manager.registerCommand("\\one");
        manager.registerCommand("\\two");
        manager.registerCommand("\\three");
        manager.registerCommand("\\ten");

        manager.registerAlias("\\one", "\\een");
        manager.registerAlias("\\one", "\\un");
        manager.registerAlias("\\een", "\\ein");

        manager.registerAlias("\\two", "\\twee");
        manager.registerAlias("\\twee", "\\deux");
        manager.registerAlias("\\deux", "\\zwei");

        manager.registerAlias("\\three", "\\tien");
        manager.registerAlias("\\ten", "\\tien");
        manager.registerAlias("\\tien", "\\dix");

        checkDefaultAliases(manager::getAliases);
    }

    @Test
    public void registerAliasNoSlash() throws Exception {
        resetup(null);

        manager.registerCommand("\\one");
        manager.registerCommand("\\two");
        manager.registerCommand("\\three");
        manager.registerCommand("\\ten");

        manager.registerAliasNoSlash("one", "een");
        manager.registerAliasNoSlash("one", "un");
        manager.registerAliasNoSlash("een", "ein");

        manager.registerAliasNoSlash("two", "twee");
        manager.registerAliasNoSlash("twee", "deux");
        manager.registerAliasNoSlash("deux", "zwei");

        manager.registerAliasNoSlash("three", "tien");
        manager.registerAliasNoSlash("ten", "tien");
        manager.registerAliasNoSlash("tien", "dix");

        checkDefaultAliases(manager::getAliases);
    }

    @Test
    public void getAliases() throws Exception {
        resetup(getDefaultAliasGroups());

        checkDefaultAliases(manager::getAliases);
    }

    @Test
    public void getAliasesNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        checkDefaultAliasesNoSlash(manager::getAliasesNoSlash);
    }

    @Test
    public void getAliasesFromOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\two", "\\een");

        Set<String> aliases = manager.getAliasesFromOriginal("\\een");
        assertEquals("From original", true, aliases.contains("\\one"));
        assertEquals("From new set", false, aliases.contains("\\two"));
    }

    @Test
    public void getAliasesFromOriginalNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\twee", "\\een");

        Set<String> aliases = manager.getAliasesFromOriginalNoSlash("een");
        assertEquals("From original", true, aliases.contains("\\one"));
        assertEquals("From new set", false, aliases.contains("\\two"));
    }

    @Test
    public void getAllCommands() throws Exception {
        resetup(getDefaultAliasGroups());

        Set<String> commands = manager.getAllCommands();
        assertEquals("All commands 1", true, manager.getAllCommands().contains("\\one"));
        assertEquals("All commands 2", true, manager.getAllCommands().contains("\\two"));
        assertEquals("All commands 3", true, manager.getAllCommands().contains("\\three"));
        assertEquals("All commands 10", true, manager.getAllCommands().contains("\\tien"));
    }

    @Test
    public void isRegistered() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Is Registered \\one", true, manager.isRegistered("\\one"));
        assertEquals("Is Registered \\three", true, manager.isRegistered("\\three"));
        assertEquals("Is Not Registered (bullshit) \\ninety",
                false, manager.isRegistered("\\ninety"));
    }

    @Test
    public void isRegisteredNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Is Registered one", true, manager.isRegisteredNoSlash("one"));
        assertEquals("Is Registered three", true, manager.isRegisteredNoSlash("three"));
        assertEquals("Is Not Registered (alias) twee",
                false, manager.isRegistered("twee"));
        assertEquals("Is Not Registered (bullshit) ninety",
                false, manager.isRegistered("ninety"));
    }

    @Test
    public void isOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\two", "\\een");

        assertEquals("Is Registered \\een", true, manager.isOriginal("\\een"));
        assertEquals("Is Registered \\twee", true, manager.isOriginal("\\twee"));
        assertEquals("Is Not Registered (alias) \\two",
                false, manager.isOriginal("\\two"));
        assertEquals("Is Not Registered (bullshit) \\ninety",
                false, manager.isOriginal("\\ninety"));
    }

    @Test
    public void isOriginalNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\twee", "\\one");

        assertEquals("Is Registered \\een",
                true, manager.isOriginalNoSlash("een"));
        assertEquals("Is Registered \\twee",
                true, manager.isOriginalNoSlash("twee"));
        assertEquals("Is Not Registered (alias) \\two",
                false, manager.isOriginalNoSlash("two"));
        assertEquals("Is Not Registered (bullshit) \\ninety",
                false, manager.isOriginalNoSlash("ninety"));
    }

    @Test
    public void clear() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.clear();
        assertEquals("Cleared", 0, manager.size());
    }

    @Test
    public void size() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Count", 11, manager.size());
    }

    @Test
    public void originalSize() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\two", "\\one");
        manager.registerAlias("\\two", "\\een");
        manager.registerAlias("\\two", "\\un");
        manager.registerAlias("\\two", "\\ein");

        assertEquals("Original size", 4, manager.originalSize());
    }

    @Test
    public void stream() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.stream().filter(s -> s.equals("\\one") || s.equals("\\ten") ||
                s.equals("\\zwei") || s.equals("\\twee")).count();

        assertEquals("Alias Stream", 4, count);
    }

    @Test
    public void parallelStream() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.parallelStream().filter(s -> s.equals("\\one") || s.equals("\\ten") ||
                s.equals("\\zwei") || s.equals("\\twee")).count();

        assertEquals("Alias Parallel Stream", 4, count);
    }

    @Test
    public void iterator() throws Exception {
        resetup(getDefaultAliasGroups());

        int aliases = manager.size();
        int count = 0;
        for (String alias : manager) {
            count++;
        }
        assertEquals("Alias Iterator", aliases, count);
    }

    @Test
    public void streamOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.streamOriginal().filter(s -> s.equals("\\een") || s.equals("\\three"))
                .count();

        assertEquals("Original Stream", 2, count);
    }

    @Test
    public void parallelStreamOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.parallelStreamOriginal().filter(s -> s.equals("\\een") || s.equals("\\three"))
                .count();

        assertEquals("Original Parallel Stream", 2, count);
    }

    @Test
    public void iteratorOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        int originals = manager.originalSize();
        int count = 0;
        for (Iterator<String> it = manager.iteratorOriginal(); it.hasNext(); it.next()) {
            count++;
        }
        assertEquals("Original Iterator", originals, count);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionRegisterCommand() {
        resetup(getDefaultAliasGroups());

        manager.registerCommand("\\one");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionRegisterAlias() {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\GoodDaySir", "\\BullShitStuffs");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionGetAliases() {
        resetup(getDefaultAliasGroups());

        manager.getAliases("\\hihihahaheejheej");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionGetAliasesOriginal() {
        resetup(getDefaultAliasGroups());

        manager.getAliasesFromOriginal("\\hihihahaheejheej");
    }

    /**
     * The test case in {@link CommandManager#original}
     * <p>
     * Latex:<br>
     * {@code \let\goodepsilon\varepsilon}<br>
     * {@code \let\varepsilon\epsilon}<br>
     * {@code \let\epsilon\goodepsilon}
     * <p>
     * Definitions:<br>
     * {@code A := {\epsilon, \goodepsilon}}<br>
     * {@code B := {\varepsilon}}
     * <p>
     * Map:<br>
     * {@code \epsilon => B}<br>
     * {@code \varepsilon => A}
     */
    @Test
    public void epsilon() {
        resetup(null);

        // Original commands
        manager.registerCommandNoSlash("epsilon");
        manager.registerCommandNoSlash("varepsilon");

        // Set aliases
        manager.registerAliasNoSlash("varepsilon", "goodepsilon");
        manager.registerAliasNoSlash("epsilon", "varepsilon");
        manager.registerAliasNoSlash("goodepsilon", "epsilon");

        // Result checking
        Set<String> A = manager.getAliasesFromOriginalNoSlash("varepsilon");
        Set<String> B = manager.getAliasesFromOriginalNoSlash("epsilon");

        // The old 'varepsilon' should link to the nice epsilon, which is renamed to 'epsilon' with
        // 'goodepsilon' used in the process.
        assertEquals("A", new HashSet<String>() {{
            add("\\epsilon");
            add("\\goodepsilon");
        }}, A);

        // The old 'epsilon' should link to the ugly epsilon, which is put into 'varepsilon'.
        assertEquals("B", new HashSet<String>() {{
            add("\\varepsilon");
        }}, B);
    }

}