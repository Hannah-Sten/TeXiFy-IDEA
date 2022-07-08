package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.hannahsten.texifyidea.util.CredentialAttributes.Mendeley.refreshTokenAttributes
import nl.hannahsten.texifyidea.util.CredentialAttributes.Mendeley.tokenAttributes

/**
 * Authorization via OAuth:
 * - Get authentication code token by letting the user log in via a browser.
 * - Exchange the authentication code for an access token, store this access token and the refresh token so we can make
 *   future requests on behalf of the user.
 * - On a future request
 */
object MendeleyAuthenticator {

    private const val port = 8080

    private const val redirectPath = "/"

    private const val redirectUrl = "http://localhost:$port$redirectPath"

    private val authorizationParameters: String = Parameters.build {
        append("client_id", MendeleyCredentials.id)
        append("redirect_uri", redirectUrl)
        append("response_type", "code")
        append("scope", "all")
    }.formUrlEncode()

    val authorizationUrl = "https://api.mendeley.com/oauth/authorize?$authorizationParameters"

    var serverRunning = false

    private var authenticationCode: String? = null

    /**
     * Server that listens on the redirect url of the authorization request. Displays login confirmation to the user and
     * gets the authorization code from the response.
     */
    val authorizationServer: NettyApplicationEngine = embeddedServer(Netty, port = port) {
        routing {
            get("/") {
                this.call.respondText("You are now logged in to Mendeley.")
                authenticationCode = call.parameters["code"]
                serverRunning = false
                this.application.dispose()
            }
        }
    }.apply {
        environment.monitor.subscribe(ApplicationStarted) {
            serverRunning = true
        }
    }

    /**
     * Client that can make requests to Mendeley to retrieve access tokens.
     */
    private val authenticationClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    /**
     * Exchange the [authenticationCode] for an access token.
     */
    suspend fun getAccessToken(): Credentials? {
        return PasswordSafe.instance.get(tokenAttributes) ?:
            authenticationCode?.let {
                val token: AccessTokenInfo = authenticationClient.submitForm(
                    url = "https://api.mendeley.com/oauth/token",
                    formParameters = Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", it)
                        append("redirect_uri", redirectUrl)
                    }) {
                    basicAuth(Mendeley.id, Mendeley.secret)
                }.body()

                token.getCredentials().first
            }
    }

    /**
     * Use the refresh token to renew the access token.
     */
    suspend fun refreshAccessToken(): BearerTokens {
        val token: AccessTokenInfo = authenticationClient.submitForm(
            url = "https://api.mendeley.com/oauth/token",
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", PasswordSafe.instance.getPassword(refreshTokenAttributes)!!)
                append("redirect_uri", "http://localhost:$port/")
            }) {
            basicAuth(Mendeley.id, Mendeley.secret)
        }.body()

        val (tokenCredentials, refreshTokenCredentials) = token.getCredentials()

        return BearerTokens(tokenCredentials.password.toString(), refreshTokenCredentials.password.toString())

    }

    /**
     * Data class to deserialize Mendeley's response with the access token.
     */
    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class AccessTokenInfo(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Int,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("token_type") val tokenType: String,
    ) {
        fun getCredentials(): Pair<Credentials, Credentials> {
            val tokenCredentials = Credentials("token", accessToken)
            val refreshTokenCredentials = Credentials("refresh_token", refreshToken)

            PasswordSafe.instance.set(tokenAttributes, tokenCredentials)
            PasswordSafe.instance.set(refreshTokenAttributes, refreshTokenCredentials)

            return tokenCredentials to refreshTokenCredentials
        }
    }
}