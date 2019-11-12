package nl.hannahsten.texifyidea.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author Hannah Schellekens
 */
open class CollectionsKtTest {

    @Test
    fun `Collection anyMatchAll`() {
        val set = setOf(2, 5, 1, 6, 213, 12, -12, 3)
        val list = listOf("bambi", "test", "kameel", "broodje")

        assertTrue(set.anyMatchAll({ it == 3 }, { it == 5 }))
        assertTrue(set.anyMatchAll({ it == 3 }))
        assertTrue(set.anyMatchAll({ it == -12 }, { it == 12 }, { it == 1 }, { it == 1 }))
        assertFalse(set.anyMatchAll({ it == 3 }, { it == 9 }))
        assertFalse(set.anyMatchAll({ it == 3 }, { it == 9 }, { it == Integer.MAX_VALUE }))
        assertTrue(list.anyMatchAll({ it == "bambi" }, { it == "test" }, { it == "broodje" }, { it == "kameel" }))
        assertTrue(list.anyMatchAll({ it == "bambi" }, { it == "broodje" }))
        assertTrue(list.anyMatchAll({ it == "bambi" }))
        assertFalse(list.anyMatchAll({ it == "hello" }))
        assertFalse(list.anyMatchAll({ it == "test" }, { it == "kameels" }))
        assertFalse(list.anyMatchAll({ it == "asfasfd" }, { it == "asdfasf" }))
        assertFalse(list.anyMatchAll({ it == "asfasfd" }, { it == "broodje" }, { it == "broodje" }))
    }
}