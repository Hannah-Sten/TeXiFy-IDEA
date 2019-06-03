package nl.rubensten.texifyidea.lang.magic

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Ruben Schellekens
 */
class MagicCommentTest {

    @Test
    fun toCommentString() {
        val magicCommentSource = """
            %! single
            %! info = Some text
            %!    TeX with overflow to
            %!  new lines.
            %! param = first value
            %! param =  second value
            %! year = 2019
            %! param = third value
        """.trimIndent().trim()

        val parser = TextBasedMagicCommentParser(magicCommentSource.split("\n"))
        val comment = parser.parse()

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