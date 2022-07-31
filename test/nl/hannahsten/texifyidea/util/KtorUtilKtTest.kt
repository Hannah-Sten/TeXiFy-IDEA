package nl.hannahsten.texifyidea.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KtorUtilKtTest {

    @Test
    fun testZoteroLinkHeader() {
        val header =
            "<https://api.zotero.org/users/8789020/items?format=bibtex&limit=4&start=4>; rel=\"next\", <https://api.zotero.org/users/8789020/items?format=bibtex&limit=4&start=4>; rel=\"last\", <https://www.zotero.org/users/8789020/items>; rel=\"alternate\""

        val resultMap = header.parseLinkHeader()

        val expected = mapOf(
            "next" to "https://api.zotero.org/users/8789020/items?format=bibtex&limit=4&start=4",
            "last" to "https://api.zotero.org/users/8789020/items?format=bibtex&limit=4&start=4",
            "alternate" to "https://www.zotero.org/users/8789020/items"
        )

        assertEquals(expected, resultMap)
    }
}