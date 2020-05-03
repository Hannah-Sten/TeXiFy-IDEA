package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Hannah Schellekens
 */
abstract class TexifyRegexInspectionTestBase(regexInspection: TexifyRegexInspection) {

    val pattern = regexInspection.pattern

    /**
     * All strings that must match the [pattern].
     */
    abstract val successfulMatches: List<String>

    /**
     * All strings that must not match the [pattern].
     */
    abstract val failingMatches: List<String>

    @Test
    fun `Test succesful regex matches`() = successfulMatches.forEach {
        assertTrue("Match <$it> against <$pattern> (success)") { pattern.matcher(it).find() }
    }

    @Test
    fun `Test failing regex matches`() = failingMatches.forEach {
        assertFalse("Match <$it> against <$pattern> (fail)") { pattern.matcher(it).find() }
    }
}