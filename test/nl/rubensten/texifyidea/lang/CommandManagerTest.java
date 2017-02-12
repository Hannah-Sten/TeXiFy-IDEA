package nl.rubensten.texifyidea.lang;

import org.junit.Before;
import org.junit.Test;

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
                method.apply("\\one").contains("\\one"), true);
        assertEquals("Alias 1: contains \\un",
                method.apply("\\ein").contains("\\un"), true);
        assertEquals("Alias 1: contains \\een",
                method.apply("\\un").contains("\\een"), true);
        assertEquals("Alias 1: contains \\ein",
                method.apply("\\een").contains("\\ein"), true);

        assertEquals("Alias 2: contains \\two",
                method.apply("\\two").contains("\\two"), true);
        assertEquals("Alias 2: contains \\deux",
                method.apply("\\zwei").contains("\\deux"), true);
        assertEquals("Alias 2: contains \\twee",
                method.apply("\\deux").contains("\\twee"), true);
        assertEquals("Alias 2: contains \\zwei",
                method.apply("\\twee").contains("\\zwei"), true);

        assertEquals("Alias 3: only contains \\three",
                method.apply("\\three").contains("\\three") &&
                        method.apply("\\three").size() == 1, true);

        assertEquals("Alias 10: contains \\ten",
                method.apply("\\tien").contains("\\ten"), true);
        assertEquals("Alias 10: contains \\tien",
                method.apply("\\ten").contains("\\tien"), true);
    }

    private void checkDefaultAliasesNoSlash(Function<String, Set<String>> method) {
        assertEquals("Alias 1: contains one",
                method.apply("one").contains("\\one"), true);
        assertEquals("Alias 1: contains un",
                method.apply("ein").contains("\\un"), true);
        assertEquals("Alias 1: contains een",
                method.apply("un").contains("\\een"), true);
        assertEquals("Alias 1: contains ein",
                method.apply("een").contains("\\ein"), true);

        assertEquals("Alias 2: contains two",
                method.apply("two").contains("\\two"), true);
        assertEquals("Alias 2: contains deux",
                method.apply("zwei").contains("\\deux"), true);
        assertEquals("Alias 2: contains twee",
                method.apply("deux").contains("\\twee"), true);
        assertEquals("Alias 2: contains zwei",
                method.apply("twee").contains("\\zwei"), true);

        assertEquals("Alias 3: only contains three",
                method.apply("three").contains("\\three") &&
                        method.apply("three").size() == 1, true);

        assertEquals("Alias 10: contains ten",
                method.apply("tien").contains("\\ten"), true);
        assertEquals("Alias 10: contains tien",
                method.apply("ten").contains("\\tien"), true);
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
        assertEquals("Contains begin", manager.isRegistered("\\begin"), true);
        assertEquals("Contains alpha", manager.isRegistered("\\alpha"), true);
        assertEquals("Contains beta", manager.isRegistered("\\beta"), true);
        assertEquals("Contains gamma", manager.isRegistered("\\gamma"), true);
        assertEquals("Contains delta", manager.isRegistered("\\delta"), true);
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
        assertEquals("Contains begin", manager.isRegistered("\\begin"), true);
        assertEquals("Contains alpha", manager.isRegistered("\\alpha"), true);
        assertEquals("Contains beta", manager.isRegistered("\\beta"), true);
        assertEquals("Contains gamma", manager.isRegistered("\\gamma"), true);
        assertEquals("Contains delta", manager.isRegistered("\\delta"), true);
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

        manager.registerAliasNoSlash("\\one", "\\een");
        manager.registerAliasNoSlash("\\one", "\\un");
        manager.registerAliasNoSlash("\\een", "\\ein");

        manager.registerAliasNoSlash("\\two", "\\twee");
        manager.registerAliasNoSlash("\\twee", "\\deux");
        manager.registerAliasNoSlash("\\deux", "\\zwei");

        manager.registerAliasNoSlash("\\three", "\\tien");
        manager.registerAliasNoSlash("\\ten", "\\tien");
        manager.registerAliasNoSlash("\\tien", "\\dix");

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
        assertEquals("From original", aliases.contains("\\een"), true);
        assertEquals("From new set", aliases.contains("\\two"), false);
    }

    @Test
    public void getAliasesFromOriginalNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\twee", "\\een");

        Set<String> aliases = manager.getAliasesFromOriginalNoSlash("een");
        assertEquals("From original", aliases.contains("\\een"), true);
        assertEquals("From new set", aliases.contains("\\two"), false);
    }

    @Test
    public void getAllCommands() throws Exception {
        resetup(getDefaultAliasGroups());

        Set<String> commands = manager.getAllCommands();
        assertEquals("All commands 1", manager.getAllCommands().contains("\\one"), true);
        assertEquals("All commands 2", manager.getAllCommands().contains("\\two"), true);
        assertEquals("All commands 3", manager.getAllCommands().contains("\\three"), true);
        assertEquals("All commands 10", manager.getAllCommands().contains("\\tien"), true);

        assertEquals("All commands !2 (no Germans allowed).",
                manager.getAllCommands().contains("\\zwei"), false);
        assertEquals("All commands !3 (no Germans allowed).",
                manager.getAllCommands().contains("\\drei"), false);
    }

    @Test
    public void isRegistered() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Is Registered \\one", manager.isRegistered("\\one"), true);
        assertEquals("Is Registered \\three", manager.isRegistered("\\three"), true);
        assertEquals("Is Not Registered (alias) \\twee",
                manager.isRegistered("\\twee"), false);
        assertEquals("Is Not Registered (bullshit) \\ninety",
                manager.isRegistered("\\ninety"), false);
    }

    @Test
    public void isRegisteredNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Is Registered one", manager.isRegistered("one"), true);
        assertEquals("Is Registered three", manager.isRegistered("three"), true);
        assertEquals("Is Not Registered (alias) twee",
                manager.isRegistered("twee"), false);
        assertEquals("Is Not Registered (bullshit) ninety",
                manager.isRegistered("ninety"), false);
    }

    @Test
    public void isOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\two", "\\een");

        assertEquals("Is Registered \\een", manager.isOriginal("\\een"), true);
        assertEquals("Is Registered \\twee", manager.isOriginal("\\twee"), true);
        assertEquals("Is Not Registered (alias) \\two",
                manager.isOriginal("\\two"), false);
        assertEquals("Is Not Registered (bullshit) \\ninety",
                manager.isOriginal("\\ninety"), false);
    }

    @Test
    public void isOriginalNoSlash() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\twee", "\\one");

        assertEquals("Is Registered \\one",
                manager.isOriginalNoSlash("\\one"), true);
        assertEquals("Is Registered \\two",
                manager.isOriginalNoSlash("\\two"), true);
        assertEquals("Is Not Registered (alias) \\twee",
                manager.isOriginalNoSlash("\\twee"), false);
        assertEquals("Is Not Registered (bullshit) \\ninety",
                manager.isOriginalNoSlash("\\ninety"), false);
    }

    @Test
    public void clear() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.clear();
        assertEquals("Cleared", manager.size(), 0);
    }

    @Test
    public void size() throws Exception {
        resetup(getDefaultAliasGroups());

        assertEquals("Count", manager.size(), 11);
    }

    @Test
    public void originalSize() throws Exception {
        resetup(getDefaultAliasGroups());

        manager.registerAlias("\\two", "\\one");
        manager.registerAlias("\\two", "\\een");
        manager.registerAlias("\\two", "\\un");
        manager.registerAlias("\\two", "\\ein");

        assertEquals("Original size", manager.originalSize(), 4);
    }

    @Test
    public void stream() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.stream().filter(s -> s.equals("\\one") || s.equals("\\ten") ||
                s.equals("\\zwei") || s.equals("\\twee")).count();

        assertEquals("Alias Stream", count, 4);
    }

    @Test
    public void parallelStream() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.parallelStream().filter(s -> s.equals("\\one") || s.equals("\\ten") ||
                s.equals("\\zwei") || s.equals("\\twee")).count();

        assertEquals("Alias Parallel Stream", count, 4);
    }

    @Test
    public void iterator() throws Exception {
        resetup(getDefaultAliasGroups());

        int aliases = manager.size();
        int count = 0;
        for (String alias : manager) {
            count++;
        }
        assertEquals("Alias Iterator", count, aliases);
    }

    @Test
    public void streamOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.streamOriginal().filter(s -> s.equals("\\een") || s.equals("\\three"))
                .count();

        assertEquals("Original Stream", count, 2);
    }

    @Test
    public void parallelStreamOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        long count = manager.parallelStreamOriginal().filter(s -> s.equals("\\een") || s.equals("\\three"))
                .count();

        assertEquals("Original Parallel Stream", count, 4);
    }

    @Test
    public void iteratorOriginal() throws Exception {
        resetup(getDefaultAliasGroups());

        int originals = manager.originalSize();
        int count = 0;
        for (Iterator<String> it = manager.iteratorOriginal(); it.hasNext(); it.next()) {
            count++;
        }
        assertEquals("Original Iterator", count, originals);
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

}