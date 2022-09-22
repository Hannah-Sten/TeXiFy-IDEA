package nl.hannahsten.texifyidea.remotelibraries.zotero

import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.util.CredentialAttributes
import nl.hannahsten.texifyidea.util.paginateViaLinkHeader
import nl.hannahsten.texifyidea.util.toURIWithProxy

class ZoteroLibrary(override val identifier: String = NAME, override val displayName: String = "Zotero") :
    RemoteBibLibrary(identifier, displayName) {

    private val client by lazy { HttpClient(CIO) }

    override suspend fun getBibtexString(): Pair<HttpResponse, String> {
        val credentials = PasswordSafe.instance.get(CredentialAttributes.Zotero.userAttributes)
        return client.get("$BASE_URL/users/${credentials?.userName}/items") {
            headers {
                append("Zotero-API-version", VERSION.toString())
                append("Zotero-API-key", credentials?.password.toString())
            }
            parameter("format", "bibtex")
            parameter("limit", PAGINATION_LIMIT)
        }.paginateViaLinkHeader {
            client.get(it) {
                headers {
                    append("Zotero-API-version", VERSION.toString())
                    append("Zotero-API-key", credentials?.password.toString())
                }
            }
        }
    }

    override fun destroyCredentials() {
        PasswordSafe.instance.set(CredentialAttributes.Zotero.userAttributes, null)
    }

    companion object {

        const val VERSION = 3
        val BASE_URL = "https://api.zotero.org".toURIWithProxy()
        const val PAGINATION_LIMIT = 50
        const val NAME = "Zotero"
    }
}