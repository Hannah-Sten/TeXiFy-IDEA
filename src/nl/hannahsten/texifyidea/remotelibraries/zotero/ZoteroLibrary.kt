package nl.hannahsten.texifyidea.remotelibraries.zotero

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.util.CredentialAttributes
import nl.hannahsten.texifyidea.util.paginateViaLinkHeader

class ZoteroLibrary(val url: String?, override val identifier: String = NAME, override val displayName: String = "Zotero") :
    RemoteBibLibrary(identifier, displayName) {

    private val client by lazy {
        HttpClient(CIO) {
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
        }
    }

    override suspend fun getBibtexString(): Either<RemoteLibraryRequestFailure, String> = either {
        val credentials = PasswordSafe.instance.get(CredentialAttributes.Zotero.userAttributes)
        // Backwards compatibility, url may be null
        val cleanedUrl = if (url.isNullOrBlank()) DEFAULT_URL else url
        val (response, content) = client.get(cleanedUrl.replace(USER_ID_MACRO, credentials?.userName ?: "")) {
            headers {
                append("Zotero-API-version", VERSION.toString())
                append("Zotero-API-key", credentials?.password.toString())
            }
            parameter("limit", PAGINATION_LIMIT)
        }.paginateViaLinkHeader {
            client.get(it) {
                headers {
                    append("Zotero-API-version", VERSION.toString())
                    append("Zotero-API-key", credentials?.password.toString())
                }
            }
        }

        ensure(response.status.value in 200 until 300) {
            RemoteLibraryRequestFailure(displayName, "${response.status.value}: ${response.status.description}")
        }
        content
    }

    override fun destroyCredentials() {
        PasswordSafe.instance.set(CredentialAttributes.Zotero.userAttributes, null)
    }

    companion object {

        const val VERSION = 3
        const val PAGINATION_LIMIT = 50
        const val NAME = "Zotero"
        const val USER_ID_MACRO = $$"$USER_ID$"
        const val DEFAULT_URL = "https://api.zotero.org/users/$USER_ID_MACRO/items?format=bibtex"
    }
}