package nl.hannahsten.texifyidea.remotelibraries.zotero

import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.util.CredentialAttributes
import nl.hannahsten.texifyidea.util.paginateViaLinkHeader
import nl.hannahsten.texifyidea.util.parseLinkHeader

class ZoteroLibrary(override val identifier: String = NAME, override val displayName: String = "Zotero") :
    RemoteBibLibrary(identifier, displayName) {

    private lateinit var userID: String
    private lateinit var userApiKey: String

    private val client by lazy { HttpClient(CIO) }

    override suspend fun getBibtexString(): String {
        return client.get("$BASE_URL/users/$userID/items") {
            headers {
                append("Zotero-API-version", VERSION.toString())
                append("Zotero-API-key", userApiKey)
            }
            parameter("format", "bibtex")
            parameter("limit", PAGINATION_LIMIT)
        }.paginateViaLinkHeader {
            client.get(it) {
                headers {
                    append("Zotero-API-version", VERSION.toString())
                    append("Zotero-API-key", userApiKey)
                }
            }
        }
    }


    companion object {

        const val VERSION = 3
        const val BASE_URL = "https://api.zotero.org"
        const val PAGINATION_LIMIT = 50
        const val NAME = "Zotero"

        fun createFromPasswordSafe(identifier: String = NAME, displayName: String): ZoteroLibrary? {
            val credentials = PasswordSafe.instance.get(CredentialAttributes.Zotero.userAttributes)
            return if (credentials?.userName == null || credentials.password == null) null
            else {
                ZoteroLibrary(identifier = identifier, displayName = displayName).apply {
                    this.userID = credentials.userName.toString()
                    this.userApiKey = credentials.password.toString()
                }
            }
        }

        fun createWithGeneratedId(displayName: String, userID: String, userApiKey: String): ZoteroLibrary? {
            val library = RemoteBibLibraryFactory.create(displayName) as? ZoteroLibrary ?: return null
            library.apply {
                this.userID = userID
                this.userApiKey = userApiKey
            }

            return library
        }
    }
}