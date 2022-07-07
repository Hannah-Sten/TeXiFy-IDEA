package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.jcef.JBCefBrowser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
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
import nl.hannahsten.texifyidea.psi.BibtexEntry
import javax.swing.JComponent

class AddMendeleyAction : AddLibraryAction<MendeleyLibrary, AddMendeleyAction.AddMendeleyDialogWrapper>() {

    private val port = 8080

    private val redirectUrl = "http://localhost:$port/"

    private var serverRunning = false

    private var authenticationCode: String? = null

    private val server: NettyApplicationEngine = embeddedServer(Netty, port = port) {
        routing {
            get("/") {
                // Get the user code here
                this.call.respondText("test")
                authenticationCode = call.parameters["code"]
                this.application.dispose()
            }
        }
    }.apply {
        environment.monitor.subscribe(ApplicationStarted) {
            serverRunning = true
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

    override suspend fun createLibrary(
        dialogWrapper: AddMendeleyDialogWrapper,
        project: Project
    ): Pair<MendeleyLibrary, List<BibtexEntry>> {
        if (PasswordSafe.instance.get(MendeleyLibrary.tokenAttributes) == null) {
            authenticationCode?.let {
                val token: TokenInfo = authClient.submitForm(
                    url = "https://api.mendeley.com/oauth/token",
                    formParameters = Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", it)
                        append("redirect_uri", redirectUrl)
                    }) {
                    basicAuth(Mendeley.id, Mendeley.secret)
                }.body()

                val tokenCredentials = Credentials("token", token.accessToken)
                val refreshTokenCredentials = Credentials("refresh_token", token.refreshToken)

                PasswordSafe.instance.set(MendeleyLibrary.tokenAttributes, tokenCredentials)
                PasswordSafe.instance.set(MendeleyLibrary.refreshTokenAttributes, refreshTokenCredentials)
            }
        }
        val library = MendeleyLibrary()
        val bibItems = library.getCollection()
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        return library to bibItems
    }

    override fun getDialog(project: Project): AddMendeleyDialogWrapper {
        return AddMendeleyDialogWrapper(project, server, serverRunning, redirectUrl, port)
    }

    class AddMendeleyDialogWrapper(
        val project: Project,
        private val server: NettyApplicationEngine,
        val serverRunning: Boolean,
        val redirectUrl: String,
        val port: Int
    ) : DialogWrapper(true) {

        private val authorizationUrl = Parameters.build {
            append("client_id", Mendeley.id)
            append("redirect_uri", redirectUrl)
            append("response_type", "code")
            append("scope", "all")
        }.formUrlEncode()

        private val browser = JBCefBrowser("https://api.mendeley.com/oauth/authorize?$authorizationUrl")

        init {
            init()
            if (!serverRunning) server.start(false)
        }

        override fun createCenterPanel(): JComponent {
            return browser.component
        }
    }

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class TokenInfo(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Int,
        @SerialName("refresh_token") val refreshToken: String? = null,
        @SerialName("token_type") val tokenType: String,
    )
}