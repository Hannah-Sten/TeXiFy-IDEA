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

class MendeleyLibrary : RemoteBibLibrary(NAME) {

    private val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            PasswordSafe.instance.getPassword(Mendeley.tokenAttributes)!!,
                            PasswordSafe.instance.getPassword(Mendeley.refreshTokenAttributes)!!
                        )
                    }
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
        }.body()
    }

    companion object {

        const val NAME = "Mendeley"
    }
}