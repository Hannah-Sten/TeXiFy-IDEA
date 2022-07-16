package nl.hannahsten.texifyidea.remotelibraries.mendeley

import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
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
                        BearerTokens(
                            PasswordSafe.instance.getPassword(Mendeley.tokenAttributes)!!,
                            PasswordSafe.instance.getPassword(Mendeley.refreshTokenAttributes)!!
                        )
                    }
                    // If the access token was not in memory or was expired, refresh it using the refreshtoken.
                    refreshTokens {
                        return@refreshTokens MendeleyAuthenticator.refreshAccessToken()
                    }
                }
            }
        }
    }

    override suspend fun getBibtexString(): String {
        MendeleyAuthenticator.getAccessToken()
        return client.get(urlString = "https://api.mendeley.com/documents") {
            header("Accept", "application/x-bibtex")
            parameter("view", "bib")
            parameter("limit", 50)
        }.paginateViaLinkHeader {
            client.get(it) {
                header("Accept", "application/x-bibtex")
            }
        }
    }

    companion object {

        const val NAME = "Mendeley"
    }
}