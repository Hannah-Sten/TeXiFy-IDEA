package nl.hannahsten.texifyidea.util

import io.ktor.client.call.*
import io.ktor.client.statement.*

/**
 * Extracts urls from a Link header that follows [rfc5988](https://www.rfc-editor.org/rfc/pdfrfc/rfc5988.txt.pdf).
 */
fun String.parseLinkHeader(): Map<String, String> {
    return Regex("<(?<url>[^>]*)>; rel=\"(?<key>\\w+)\"").findAll(this)
        .filter { it.groups["key"] != null && it.groups["url"] != null }
        .map { it.groups["key"]!!.value to it.groups["url"]!!.value }
        .toMap()
}

/**
 * Use the Link header to handle a paginated response, building any subsequent request from [nextPageRequest].
 *
 * @return a Pair with the [HttpResponse] of the last executed request and the complete body.
 *  The last response can be used to handle failing requests appropriately.
 */
suspend fun HttpResponse.paginateViaLinkHeader(nextPageRequest: suspend (String) -> HttpResponse): Pair<HttpResponse, String> {
    val resultString = StringBuilder().append(body<String>())

    var lastResponse = this
    while (lastResponse.hasNextPage()) {
        lastResponse = lastResponse.getNextPage(nextPageRequest) ?: return Pair(lastResponse, resultString.toString())
        resultString.append(lastResponse.body<String>())
    }

    return Pair(lastResponse, resultString.toString())
}

/**
 * Get the next page from the next url in the Link header.
 */
suspend fun HttpResponse.getNextPage(nextPageRequest: suspend (String) -> HttpResponse): HttpResponse? {
    val nextUrl = headers["Link"]?.parseLinkHeader()?.get("next") ?: return null

    return nextPageRequest(nextUrl)
}

/**
 * Check if an [HttpResponse] has a Link header with a next url.
 *
 * If it does not, the current request is not paginated or the current pagination request does not have a next page.
 */
fun HttpResponse.hasNextPage(): Boolean = headers["Link"]?.let { it.parseLinkHeader()["next"] != null } ?: false
