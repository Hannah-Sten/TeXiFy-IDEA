package nl.hannahsten.texifyidea.util

/**
 * Extracts urls from a Link header that follows [rfc5988](https://www.rfc-editor.org/rfc/pdfrfc/rfc5988.txt.pdf).
 */
fun String.parseLinkHeader(): Map<String, String> {
    return Regex("<(?<url>[^>]*)>; rel=\"(?<key>\\w+)\"").findAll(this)
        .filter { it.groups["key"] != null && it.groups["url"] != null }
        .map { it.groups["key"]!!.value to it.groups["url"]!!.value }
        .toMap()
}