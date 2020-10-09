package nl.hannahsten.texifyidea.lang.magic

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Hannah Schellekens
 */
open class TextBasedMagicCommentParserTest {

    @Test
    fun `Empty comment`() {
        "%!".parse().apply {
            assertEquals(0, size(), "Empty comment having no key-value pairs.")
        }
        "%! TeX  \n%!\n% !TEX    ".parse().apply {
            assertEquals(0, size(), "Empty comment having no key-value pairs.")
        }
    }

    @Test
    fun `Single key`() {
        "%! somename".assertValue("somename")
        "% !TeX some Name".assertValue("some")
        "%!\n%! test".assertValue("test", null, 0)
    }

    @Test
    fun `Key-value pair`() {
        "%! key = Value".assertValue("key", "Value")
        "% ! TEX       somekey   =  Value".assertValue("somekey", "Value")
        "%! broodje =\n%!TeX     Saus".assertValue("broodje", "Saus")
        "%!\n%! kameel = Saus\n%!".assertValue("kameel", "Saus")
    }

    @Test
    fun `Multiple key-value pairs`() {
        val comment = "%! test = Broodje\n%! kameel = Saus".parse()
        comment.assertValue("test", "Broodje")
        comment.assertValue("kameel", "Saus")
    }

    @Test
    fun `Mixed case keys`() {
        "%! teStWitHCAsE = Value".assertValue("testwithcase", "Value")
    }

    @Test
    fun `Multiple values per key`() {
        (
            "%! param = zeroth param\n" +
                "%! param = first param\n" +
                "%! optional = zeroth optional\n" +
                "%! optional = first optional\n" +
                "%! optional = second optional\n" +
                "%! single = sadlife"
            ).parse().apply {
            assertValue("param", "first param", 1)
            assertValue("optional", "zeroth optional", 0)
            assertValue("param", "zeroth param", 0)
            assertValue("optional", "second optional", 2)
            assertValue("single", "sadlife")
            assertValue("optional", "first optional", 1)
        }
    }

    @Test
    fun `Continuation value`() {
        "%! info = Some text\n%!    TeX with overflow to\n%! new lines."
            .assertValue("info", "Some text with overflow to new lines.")

        "%! info = some\n%! broken\n%! up \n%! info\n%! eurovision = cool\n%!\n%! single\n".apply {
            assertValue("info", "some broken up info")
            assertValue("eurovision", "cool")
            assertValue("single")
        }

        "%! info = a \n %! b \n %! \n %! c \n %! info = d".apply {
            assertValue("info", "a b", 0)
            assertValue("c")
            assertValue("info", "d", 1)
        }
    }

    @Test
    fun `Multiple assignments on one line`() {
        "%! test =       broodje =  saus ".assertValue("test", "broodje =  saus")
    }

    private fun String.asInput() = trimIndent().trim().split("\n")

    private fun String.parser() = asInput().textBasedMagicCommentParser()

    private fun String.parse() = parser().parse()

    private fun String.assertValue(key: String, actualValue: String? = null, index: Int = 0) {
        parse().assertValue(key, actualValue, index)
    }
}