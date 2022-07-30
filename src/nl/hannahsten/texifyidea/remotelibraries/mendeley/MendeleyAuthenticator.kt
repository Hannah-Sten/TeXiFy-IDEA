package nl.hannahsten.texifyidea.remotelibraries.mendeley

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
import io.ktor.server.jetty.*
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
 * - On a future request, use the access token if it is still valid, otherwise use the refresh token to get a new access token.
 */
object MendeleyAuthenticator {

    init {
        createAuthenticationServer()
    }

    private const val port = 8080

    private const val redirectPath = "/"

    private const val redirectUrl = "http://localhost:$port$redirectPath"

    /**
     * See [Mendeley documentation](https://dev.mendeley.com/reference/topics/authorization_auth_code.html) for explanations
     * about these parameters.
     */
    private val authorizationParameters: String = Parameters.build {
        append("client_id", MendeleyCredentials.id)
        append("redirect_uri", redirectUrl)
        append("response_type", "code")
        append("scope", "all")
    }.formUrlEncode()

    val authorizationUrl = "https://api.mendeley.com/oauth/authorize?$authorizationParameters"

    /**
     * This server is a var because a stopped server cannot be restarted, so a new server is created before every
     * authentication attempt.
     *
     * @see [createAuthenticationServer]
     */
    private lateinit var authenticationServer: JettyApplicationEngine

    /**
     * Authentication code that can be exchanged for an access token.
     */
    private var authenticationCode: String? = null

    /**
     * Create a server that listens on the redirect url of the authorization request. Displays login confirmation to the user and
     * gets the authorization code from the response.
     */
    private fun createAuthenticationServer() {
        authenticationServer = embeddedServer(Jetty, port = port) {
            routing {
                get("/") {
                    this.call.respondText("You are now logged in to Mendeley. Click OK to continue.")
                    authenticationCode = call.parameters["code"]
                }
            }
        }.start(false)
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
     * Exchange the [authenticationCode] for an access token. Use the stored access token when available.
     */
    suspend fun getAccessToken(): Credentials? {
        return PasswordSafe.instance.get(tokenAttributes) ?: authenticationCode?.let {
            val token: AccessTokenInfo = authenticationClient.submitForm(
                url = "https://api.mendeley.com/oauth/token",
                formParameters = Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", it)
                    append("redirect_uri", redirectUrl)
                }) {
                basicAuth(MendeleyCredentials.id, MendeleyCredentials.secret.decipher())
            }.body()

            token.getCredentials().first
        }
    }

    /**
     * Use the refresh token to renew the access token.
     */
    suspend fun refreshAccessToken(): BearerTokens? {
        val refreshToken = PasswordSafe.instance.getPassword(refreshTokenAttributes) ?: return null
        val token: AccessTokenInfo = authenticationClient.submitForm(
            url = "https://api.mendeley.com/oauth/token",
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
                append("redirect_uri", "http://localhost:$port/")
            }) {
            basicAuth(MendeleyCredentials.id, MendeleyCredentials.secret.decipher())
        }.body()

        val (tokenCredentials, refreshTokenCredentials) = token.getCredentials()

        return BearerTokens(tokenCredentials.password.toString(), refreshTokenCredentials.password.toString())
    }

    /**
     * Destroy and recreate the authentication server.
     */
    fun reset() {
        authenticationServer.stop()
        createAuthenticationServer()
    }

    /**
     * Decipher the Mendeley secret...
     */
    private fun String.decipher() = toCharArray().map { it.minus(12) }.joinToString("")

    /**
     * Data class to deserialize Mendeley's response with the access token.
     */
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