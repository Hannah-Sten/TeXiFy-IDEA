package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import nl.hannahsten.texifyidea.util.createCredentialsAttributes

class MendeleyLibrary : RemoteBibLibrary(NAME) {

    private val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            PasswordSafe.instance.getPassword(tokenAttributes)!!,
                            PasswordSafe.instance.getPassword(refreshTokenAttributes)!!
                        )
                    }
                    refreshTokens {
                        val token: AddMendeleyAction.TokenInfo = authClient.submitForm(
                            url = "https://api.mendeley.com/oauth/token",
                            formParameters = Parameters.build {
                                append("grant_type", "refresh_token")
                                append("refresh_token", PasswordSafe.instance.getPassword(refreshTokenAttributes)!!)
                                append("redirect_uri", "http://localhost:80/")
                            }) {
                            basicAuth(Mendeley.id, Mendeley.secret)
                        }.body()

                        val tokenCredentials = Credentials("token", token.accessToken)
                        val refreshTokenCredentials = Credentials("refresh_token", token.refreshToken)

                        PasswordSafe.instance.set(tokenAttributes, tokenCredentials)
                        PasswordSafe.instance.set(refreshTokenAttributes, refreshTokenCredentials)

                        return@refreshTokens BearerTokens(tokenCredentials.password.toString(), refreshTokenCredentials.password.toString())
                    }
                }
            }
        }
    }

    val authClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    override suspend fun getBibtexString(): String {
        return client.get(urlString = "https://api.mendeley.com/documents") {
            header("Accept", "application/x-bibtex")
            parameter("view", "bib")
        }.body()
    }

    companion object {

        const val NAME = "Mendeley"
        val tokenAttributes = createCredentialsAttributes("$NAME-token")
        val refreshTokenAttributes = createCredentialsAttributes("$NAME-refresh-token")
    }
}