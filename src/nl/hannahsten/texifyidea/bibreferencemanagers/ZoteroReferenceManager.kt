package nl.hannahsten.texifyidea.bibreferencemanagers

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import nl.hannahsten.texifyidea.externallibrary.Temp

class ZoteroReferenceManager(val userID: Int = Temp.userID, val userApiKey: String = Temp.userApiKey) : ReferenceManager() {

    private val client by lazy { HttpClient(CIO) }

    override fun getCollection(): Set<String> {
        return emptySet()
    }

    private suspend fun getFromAPI() {
        val response: HttpResponse = client.get("https://api.zotero.org/users/$userID/items") {
            headers {
                append("Zotero-API-version", VERSION.toString())
                append("Zotero-API-key", userApiKey)
            }
            parameter("format", "bibtex")
        }
    }

    companion object {

        const val VERSION = 3
    }
}