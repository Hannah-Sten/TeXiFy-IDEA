package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.alias.CommandManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.function.Function

/**
 * Tests for [CommandManager].
 *
 * @author Hannah Schellekens
 */
@Suppress("unused")
class CommandManagerTest {

    private var manager: CommandManager? = null

    @Before
    fun setup() {
        manager = CommandManager
    }

    private fun resetup(aliasGroups: Array<Array<String>>?) {
        manager!!.clear()
        if (aliasGroups == null) {
            return
        }
        for (group in aliasGroups) {
            if (group.isEmpty()) {
                continue
            }
            registerCommandSwitchSlash(group[0])
            for (i in 1 until group.size) {
                registerAliasSwitchSlash(group[0], group[i])
            }
        }
    }

    private fun getAliasesFromOriginalSwitchSlash(command: String): Set<String> {
        return if (command.startsWith("\\")) {
            manager!!.getAliasesFromOriginal(command)
        }
        else {
            manager!!.getAliasesFromOriginalNoSlash(command)
        }
    }

    private fun getAliasesSwitchSlash(command: String): Set<String> {
        return if (command.startsWith("\\")) {
            manager!!.getAliases(command)
        }
        else {
            manager!!.getAliasesNoSlash(command)
        }
    }

    private fun isOriginalSwithSlash(command: String): Boolean {
        return if (command.startsWith("\\")) {
            manager!!.isOriginal(command)
        }
        else {
            manager!!.isOriginalNoSlash(command)
        }
    }

    private fun isRegisteredSwitchSlash(command: String): Boolean {
        return if (command.startsWith("\\")) {
            manager!!.isRegistered(command)
        }
        else {
            manager!!.isRegisteredNoSlash(command)
        }
    }

    private fun registerAliasSwitchSlash(command: String, alias: String) {
        if (command.startsWith("\\")) {
            manager!!.registerAlias(command, alias)
        }
        else {
            manager!!.registerAliasNoSlash(command, alias)
        }
    }

    private fun registerCommandSwitchSlash(command: String) {
        if (command.startsWith("\\")) {
            manager!!.registerCommand(command)
        }
        else {
            manager!!.registerCommandNoSlash(command)
        }
    }

    private fun checkDefaultAliases(method: Function<String, Set<String>>) {
        Assert.assertEquals(
            "Alias 1: contains \\one",
            true, method.apply("\\one").contains("\\one")
        )
        Assert.assertEquals(
            "Alias 1: contains \\un",
            true, method.apply("\\ein").contains("\\un")
        )
        Assert.assertEquals(
            "Alias 1: contains \\een",
            true, method.apply("\\un").contains("\\een")
        )
        Assert.assertEquals(
            "Alias 1: contains \\ein",
            true, method.apply("\\een").contains("\\ein")
        )
        Assert.assertEquals(
            "Alias 2: contains \\two",
            true, method.apply("\\two").contains("\\two")
        )
        Assert.assertEquals(
            "Alias 2: contains \\deux",
            true, method.apply("\\zwei").contains("\\deux")
        )
        Assert.assertEquals(
            "Alias 2: contains \\twee",
            true, method.apply("\\deux").contains("\\twee")
        )
        Assert.assertEquals(
            "Alias 2: contains \\zwei",
            true, method.apply("\\twee").contains("\\zwei")
        )
        Assert.assertEquals(
            "Alias 3: only contains \\three",
            true,
            method.apply("\\three").contains("\\three") &&
                method.apply("\\three").size == 1
        )
        Assert.assertEquals(
            "Alias 10: contains \\ten",
            true, method.apply("\\tien").contains("\\ten")
        )
        Assert.assertEquals(
            "Alias 10: contains \\tien",
            true, method.apply("\\ten").contains("\\tien")
        )
    }

    private fun checkDefaultAliasesNoSlash(method: Function<String, Set<String>>) {
        Assert.assertEquals(
            "Alias 1: contains one",
            true, method.apply("one").contains("\\one")
        )
        Assert.assertEquals(
            "Alias 1: contains un",
            true, method.apply("ein").contains("\\un")
        )
        Assert.assertEquals(
            "Alias 1: contains een",
            true, method.apply("un").contains("\\een")
        )
        Assert.assertEquals(
            "Alias 1: contains ein",
            true, method.apply("een").contains("\\ein")
        )
        Assert.assertEquals(
            "Alias 2: contains two",
            true, method.apply("two").contains("\\two")
        )
        Assert.assertEquals(
            "Alias 2: contains deux",
            true, method.apply("zwei").contains("\\deux")
        )
        Assert.assertEquals(
            "Alias 2: contains twee",
            true, method.apply("deux").contains("\\twee")
        )
        Assert.assertEquals(
            "Alias 2: contains zwei",
            true, method.apply("twee").contains("\\zwei")
        )
        Assert.assertEquals(
            "Alias 3: only contains three",
            true,
            method.apply("three").contains("\\three") &&
                method.apply("three").size == 1
        )
        Assert.assertEquals(
            "Alias 10: contains ten",
            true, method.apply("tien").contains("\\ten")
        )
        Assert.assertEquals(
            "Alias 10: contains tien",
            true, method.apply("ten").contains("\\tien")
        )
    }

    private val defaultAliasGroups: Array<Array<String>>
        get() = arrayOf(
            arrayOf("\\een", "\\one", "\\un", "\\ein"),
            arrayOf("\\twee", "\\two", "\\deux", "\\zwei"),
            arrayOf("\\three"),
            arrayOf("\\tien", "\\ten")
        )

    @Test
    @Throws(Exception::class)
    fun testRegisterCommand() {
        resetup(null)
        manager!!.registerCommand("\\begin")
        manager!!.registerCommand("\\alpha")
        manager!!.registerCommand("\\beta")
        manager!!.registerCommand("\\gamma")
        manager!!.registerCommand("\\delta")
        val expectedSize = 5
        Assert.assertEquals("New count", expectedSize.toLong(), manager!!.originalSize().toLong())
        Assert.assertEquals("Contains begin", true, manager!!.isRegistered("\\begin"))
        Assert.assertEquals("Contains alpha", true, manager!!.isRegistered("\\alpha"))
        Assert.assertEquals("Contains beta", true, manager!!.isRegistered("\\beta"))
        Assert.assertEquals("Contains gamma", true, manager!!.isRegistered("\\gamma"))
        Assert.assertEquals("Contains delta", true, manager!!.isRegistered("\\delta"))
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterCommandNoSlash() {
        resetup(null)
        manager!!.registerCommandNoSlash("begin")
        manager!!.registerCommandNoSlash("alpha")
        manager!!.registerCommandNoSlash("beta")
        manager!!.registerCommandNoSlash("gamma")
        manager!!.registerCommandNoSlash("delta")
        val expectedSize = 5
        Assert.assertEquals("New count", expectedSize.toLong(), manager!!.originalSize().toLong())
        Assert.assertEquals("Contains begin", true, manager!!.isRegistered("\\begin"))
        Assert.assertEquals("Contains alpha", true, manager!!.isRegistered("\\alpha"))
        Assert.assertEquals("Contains beta", true, manager!!.isRegistered("\\beta"))
        Assert.assertEquals("Contains gamma", true, manager!!.isRegistered("\\gamma"))
        Assert.assertEquals("Contains delta", true, manager!!.isRegistered("\\delta"))
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterAlias() {
        resetup(null)
        manager!!.registerCommand("\\one")
        manager!!.registerCommand("\\two")
        manager!!.registerCommand("\\three")
        manager!!.registerCommand("\\ten")
        manager!!.registerAlias("\\one", "\\een")
        manager!!.registerAlias("\\one", "\\un")
        manager!!.registerAlias("\\een", "\\ein", isRedefinition = true)
        manager!!.registerAlias("\\two", "\\twee")
        manager!!.registerAlias("\\twee", "\\deux")
        manager!!.registerAlias("\\deux", "\\zwei")
        manager!!.registerAlias("\\three", "\\tien")
        manager!!.registerAlias("\\ten", "\\tien", isRedefinition = true)
        manager!!.registerAlias("\\tien", "\\dix", isRedefinition = true)
        checkDefaultAliases(
            Function { command: String? ->
                manager!!.getAliases(
                    command!!
                )
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun testRegisterAliasNoSlash() {
        resetup(null)
        manager!!.registerCommand("\\one")
        manager!!.registerCommand("\\two")
        manager!!.registerCommand("\\three")
        manager!!.registerCommand("\\ten")
        manager!!.registerAliasNoSlash("one", "een")
        manager!!.registerAliasNoSlash("one", "un")
        manager!!.registerAliasNoSlash("een", "ein")
        manager!!.registerAliasNoSlash("two", "twee")
        manager!!.registerAliasNoSlash("twee", "deux")
        manager!!.registerAliasNoSlash("deux", "zwei")
        manager!!.registerAliasNoSlash("three", "tien")
        manager!!.registerAliasNoSlash("ten", "tien", isRedefinition = true)
        manager!!.registerAliasNoSlash("tien", "dix")
        checkDefaultAliases(
            Function { command: String? ->
                manager!!.getAliases(
                    command!!
                )
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun testGetAliases() {
        resetup(defaultAliasGroups)
        checkDefaultAliases(
            Function { command: String? ->
                manager!!.getAliases(
                    command!!
                )
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun testGetAliasesNoSlash() {
        resetup(defaultAliasGroups)
        checkDefaultAliasesNoSlash(
            Function { commandNoSlash: String? ->
                manager!!.getAliasesNoSlash(
                    commandNoSlash!!
                )
            }
        )
    }

    @Test
    @Throws(Exception::class)
    fun testGetAliasesFromOriginal() {
        resetup(defaultAliasGroups)
        manager!!.registerAlias("\\two", "\\een")
        val aliases = manager!!.getAliasesFromOriginal("\\een")
        Assert.assertEquals("From original", true, aliases.contains("\\one"))
        Assert.assertEquals("From new set", false, aliases.contains("\\two"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAliasesFromOriginalNoSlash() {
        resetup(defaultAliasGroups)
        manager!!.registerAlias("\\twee", "\\een")
        val aliases = manager!!.getAliasesFromOriginalNoSlash("een")
        Assert.assertEquals("From original", true, aliases.contains("\\one"))
        Assert.assertEquals("From new set", false, aliases.contains("\\two"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAllCommands() {
        resetup(defaultAliasGroups)
        manager!!.allCommands
        Assert.assertEquals("All commands 1", true, manager!!.allCommands.contains("\\one"))
        Assert.assertEquals("All commands 2", true, manager!!.allCommands.contains("\\two"))
        Assert.assertEquals("All commands 3", true, manager!!.allCommands.contains("\\three"))
        Assert.assertEquals("All commands 10", true, manager!!.allCommands.contains("\\tien"))
    }

    @Test
    @Throws(Exception::class)
    fun testIsRegistered() {
        resetup(defaultAliasGroups)
        Assert.assertEquals("Is Registered \\one", true, manager!!.isRegistered("\\one"))
        Assert.assertEquals("Is Registered \\three", true, manager!!.isRegistered("\\three"))
        Assert.assertEquals(
            "Is Not Registered (bullshit) \\ninety",
            false, manager!!.isRegistered("\\ninety")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testIsRegisteredNoSlash() {
        resetup(defaultAliasGroups)
        Assert.assertEquals("Is Registered one", true, manager!!.isRegisteredNoSlash("one"))
        Assert.assertEquals("Is Registered three", true, manager!!.isRegisteredNoSlash("three"))
        Assert.assertEquals(
            "Is Not Registered (alias) twee",
            false, manager!!.isRegistered("twee")
        )
        Assert.assertEquals(
            "Is Not Registered (bullshit) ninety",
            false, manager!!.isRegistered("ninety")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testIsOriginal() {
        resetup(defaultAliasGroups)
        manager!!.registerAlias("\\two", "\\een")
        Assert.assertEquals("Is Registered \\een", true, manager!!.isOriginal("\\een"))
        Assert.assertEquals("Is Registered \\twee", true, manager!!.isOriginal("\\twee"))
        Assert.assertEquals(
            "Is Not Registered (alias) \\two",
            false, manager!!.isOriginal("\\two")
        )
        Assert.assertEquals(
            "Is Not Registered (bullshit) \\ninety",
            false, manager!!.isOriginal("\\ninety")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testIsOriginalNoSlash() {
        resetup(defaultAliasGroups)
        manager!!.registerAlias("\\twee", "\\one")
        Assert.assertEquals(
            "Is Registered \\een",
            true, manager!!.isOriginalNoSlash("een")
        )
        Assert.assertEquals(
            "Is Registered \\twee",
            true, manager!!.isOriginalNoSlash("twee")
        )
        Assert.assertEquals(
            "Is Not Registered (alias) \\two",
            false, manager!!.isOriginalNoSlash("two")
        )
        Assert.assertEquals(
            "Is Not Registered (bullshit) \\ninety",
            false, manager!!.isOriginalNoSlash("ninety")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testClear() {
        resetup(defaultAliasGroups)
        manager!!.clear()
        Assert.assertEquals("Cleared", 0, manager!!.size().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testSize() {
        resetup(defaultAliasGroups)
        Assert.assertEquals("Count", 11, manager!!.size().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testOriginalSize() {
        resetup(defaultAliasGroups)
        manager!!.registerAlias("\\two", "\\one")
        manager!!.registerAlias("\\two", "\\een")
        manager!!.registerAlias("\\two", "\\un")
        manager!!.registerAlias("\\two", "\\ein")
        Assert.assertEquals("Original size", 4, manager!!.originalSize().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testStream() {
        resetup(defaultAliasGroups)
        val count = manager!!.stream()
            .filter { s: String? -> s == "\\one" || s == "\\ten" || s == "\\zwei" || s == "\\twee" }
            .count()
        Assert.assertEquals("Alias Stream", 4, count)
    }

    @Test
    @Throws(Exception::class)
    fun testParallelStream() {
        resetup(defaultAliasGroups)
        val count = manager!!.parallelStream()
            .filter { s: String? -> s == "\\one" || s == "\\ten" || s == "\\zwei" || s == "\\twee" }
            .count()
        Assert.assertEquals("Alias Parallel Stream", 4, count)
    }

    @Test
    @Throws(Exception::class)
    fun testIterator() {
        resetup(defaultAliasGroups)
        val aliases = manager!!.size()
        var count = 0
        for (alias in manager!!) {
            count++
        }
        Assert.assertEquals("Alias Iterator", aliases.toLong(), count.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testStreamOriginal() {
        resetup(defaultAliasGroups)
        val count = manager!!.streamOriginal()
            .filter { s: String -> s == "\\een" || s == "\\three" }
            .count()
        Assert.assertEquals("Original Stream", 2, count)
    }

    @Test
    @Throws(Exception::class)
    fun testParallelStreamOriginal() {
        resetup(defaultAliasGroups)
        val count = manager!!.parallelStreamOriginal()
            .filter { s: String -> s == "\\een" || s == "\\three" }
            .count()
        Assert.assertEquals("Original Parallel Stream", 2, count)
    }

    @Test
    @Throws(Exception::class)
    fun testIteratorOriginal() {
        resetup(defaultAliasGroups)
        val originals = manager!!.originalSize()
        var count = 0
        val it = manager!!.iteratorOriginal()
        while (it.hasNext()) {
            count++
            it.next()
        }
        Assert.assertEquals("Original Iterator", originals.toLong(), count.toLong())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionGetAliasesOriginal() {
        resetup(defaultAliasGroups)
        manager!!.getAliasesFromOriginal("\\hihihahaheejheej")
    }

    /**
     * The test case in [CommandManager.original]
     *
     *
     * Latex:<br></br>
     * `\let\goodepsilon\varepsilon`<br></br>
     * `\let\varepsilon\epsilon`<br></br>
     * `\let\epsilon\goodepsilon`
     *
     *
     * Definitions:<br></br>
     * `A := {\epsilon, \goodepsilon}`<br></br>
     * `B := {\varepsilon}`
     *
     *
     * Map:<br></br>
     * `\epsilon => B`<br></br>
     * `\varepsilon => A`
     */
    @Test
    fun testCommandRedefinition() {
        resetup(null)

        // Original commands
        manager!!.registerCommandNoSlash("epsilon")
        manager!!.registerCommandNoSlash("varepsilon")

        // Set aliases
        manager!!.registerAliasNoSlash("varepsilon", "goodepsilon")
        manager!!.registerAliasNoSlash("epsilon", "varepsilon", isRedefinition = true)
        manager!!.registerAliasNoSlash("goodepsilon", "epsilon", isRedefinition = true)

        // Result checking
        val a = manager!!.getAliasesFromOriginalNoSlash("varepsilon")
        val b = manager!!.getAliasesFromOriginalNoSlash("epsilon")

        // The old 'varepsilon' should link to the nice epsilon, which is renamed to 'epsilon' with
        // 'goodepsilon' used in the process.
        Assert.assertEquals(
            "A",
            object : HashSet<String?>() {
                init {
                    add("\\epsilon")
                    add("\\goodepsilon")
                }
            },
            a
        )

        // The old 'epsilon' should link to the ugly epsilon, which is put into 'varepsilon'.
        Assert.assertEquals(
            "B",
            object : HashSet<String?>() {
                init {
                    add("\\varepsilon")
                }
            },
            b
        )
    }
}