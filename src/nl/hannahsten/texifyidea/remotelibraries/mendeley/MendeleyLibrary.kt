package nl.hannahsten.texifyidea.remotelibraries.mendeley

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.util.CredentialAttributes.Mendeley
import nl.hannahsten.texifyidea.util.paginateViaLinkHeader

/**
 * [Mendeley](https://www.mendeley.com/reference-manager/library/all-references/) library.
 */
class MendeleyLibrary(override val identifier: String = NAME, override val displayName: String = "Mendeley") :
    RemoteBibLibrary(identifier, displayName) {

    /**
     * Client to make regular Mendeley requests with. The client contains all information to be able to authorize a
     * request, so individual requests don't have to take this into account.
     */
    private val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                bearer {
                    // Attempt to load the access token from memory.
                    loadTokens {
                        val token = PasswordSafe.instance.getPassword(Mendeley.tokenAttributes) ?: return@loadTokens null
                        val refreshToken = PasswordSafe.instance.getPassword(Mendeley.refreshTokenAttributes) ?: return@loadTokens null
                        BearerTokens(token, refreshToken)
                    }
                    // If the access token was not in memory or was expired, refresh it using the refreshtoken.
                    refreshTokens {
                        return@refreshTokens MendeleyAuthenticator.refreshAccessToken()
                    }
                }
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
        }
    }

    override suspend fun getBibtexString(): Either<RemoteLibraryRequestFailure, String> = either {
        MendeleyAuthenticator.getAccessToken()
        val (response, content) = client.get(urlString = "https://api.mendeley.com/documents") {
            header("Accept", "application/x-bibtex")
            parameter("view", "bib")
            parameter("limit", 50)
        }.paginateViaLinkHeader {
            client.get(it) {
                header("Accept", "application/x-bibtex")
            }
        }
        ensure(response.status.value in 200 until 300) {
            RemoteLibraryRequestFailure(displayName, "${response.status.value}: ${response.status.description}")
        }
        content
    }

    override fun destroyCredentials() {
        PasswordSafe.instance.set(Mendeley.tokenAttributes, null)
        PasswordSafe.instance.set(Mendeley.refreshTokenAttributes, null)
    }

    companion object {

        const val NAME = "Mendeley"
    }
}