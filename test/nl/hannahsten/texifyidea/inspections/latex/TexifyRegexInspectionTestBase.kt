package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.inspections.TexifyContextAwareRegexInspectionBase
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Hannah Schellekens
 */
abstract class TexifyRegexInspectionTestBase(regexInspection: TexifyContextAwareRegexInspectionBase) {

    val regex = regexInspection.regex

    /**
     * All strings that must match the [regex].
     */
    abstract val successfulMatches: List<String>

    /**
     * All strings that must not match the [regex].
     */
    abstract val failingMatches: List<String>

    @Test
    fun `Test succesful regex matches`() = successfulMatches.forEach {
        assertTrue("Match <$it> against <$regex> (success)") { regex.containsMatchIn(it) }
    }

    @Test
    fun `Test failing regex matches`() = failingMatches.forEach {
        assertFalse("Match <$it> against <$regex> (fail)") { regex.containsMatchIn(it) }
    }
}