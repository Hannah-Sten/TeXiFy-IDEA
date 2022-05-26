package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.createCredentialsAttributes

class ZoteroLibrary(val userID: String = Temp.userID, private val userApiKey: String = Temp.userApiKey) : RemoteBibLibrary(NAME) {

    private val client by lazy { HttpClient(CIO) }

    override suspend fun getCollection(project: Project): List<BibtexEntry> {
        val body: String = getFromAPI().receive()

        // Reading the dummy bib file needs to happen in a place where we have read access.
        return runReadAction {
            BibtexEntryListConverter().fromString(body)
        }
    }

    private suspend fun getFromAPI(): HttpResponse = client.get("https://api.zotero.org/users/$userID/items") {
        headers {
            append("Zotero-API-version", VERSION.toString())
            append("Zotero-API-key", userApiKey)
        }
        parameter("format", "bibtex")
    }

    companion object {

        const val VERSION = 3
        const val NAME = "Zotero"
        val credentialAttributes = createCredentialsAttributes(NAME)
    }
}