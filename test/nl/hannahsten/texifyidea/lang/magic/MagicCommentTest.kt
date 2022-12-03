package nl.hannahsten.texifyidea.lang.magic

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Hannah Schellekens
 */
class MagicCommentTest {

    private val debugCommentString: MagicComment<String, String> =
        """
            %! single
            %! info = Some text
            %!    TeX with overflow to
            %!  new lines.
            %! param = first value
            %! param =  second value
            %! year = 2019
            %! param = third value
        """.trimIndent().trim().run {
            TextBasedMagicCommentParser(this.split("\n")).parse()
        }

    @Test
    fun `Merge regular`() {
        val source = debugCommentString
        val mergeWithText = "%! single\n%! param = fourth value\n%! newkey = yas\n%! param = first value"
        val mergeWith = TextBasedMagicCommentParser(mergeWithText.split("\n")).parse()

        val result = source.merge(mergeWith)
        result.apply {
            assertValue("single", null)
            assertValue("info", "Some text with overflow to new lines.")
            assertValue("param", "first value", 0)
            assertValue("param", "second value", 1)
            assertValue("param", "third value", 2)
            assertValue("param", "fourth value", 3)
            assertValue("param", "first value", 4)
            assertValue("year", "2019")
            assertValue("newkey", "yas")
        }

        // Check if there are no extra keys present.
        assertEquals(5, result.size())

        val resultOperator = source + mergeWith
        assertEquals(result, resultOperator, "Operator gives a different result than direct invokation.")
    }

    @Test
    fun `Convert to string list`() {
        val comment = debugCommentString
        val lines = comment.toCommentString()
        assertTrue("info = Some text with overflow to new lines." in lines)
        assertTrue("param = first value" in lines)
        assertTrue("param = second value" in lines)
        assertTrue("param = third value" in lines)
        assertTrue("year = 2019" in lines)
        assertTrue("single" in lines)
        assertEquals(6, lines.size, "Expected exactly 5 lines.")
    }
}

/**
 * Asserts if `key` has value `expectedValue` (or `null` when there is no value) at index `index`.
 */
fun MagicComment<String, String>.assertValue(key: String, expectedValue: String? = null, index: Int = 0) {
    val magicKey = CustomMagicKey(key)
    assertTrue(magicKey in this, "<$magicKey> is in not in the comment <$this>.")

    val values = values(magicKey)
    if (expectedValue != null) {
        assertNotNull(values, "<$magicKey> is not present as key in comment <$this>.")
        assertTrue(
            index < values!!.size,
            "Index <$index> is not present in value list <$values> of comment <$this>."
        )
    }

    val actualValue = values?.getOrNull(index)
    assertEquals(
        expectedValue, actualValue,
        "Check if the key has the right first value at index <$index> of comment <$this>."
    )
}