package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.createCredentialsAttributes

class MendeleyLibrary : RemoteBibLibrary(NAME) {

    val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            PasswordSafe.instance.getPassword(tokenAttributes)!!,
                            PasswordSafe.instance.getPassword(refreshTokenAttributes)!!
                        )
                    }
                    // TODO refresh tokens
                }
            }

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    override suspend fun getCollection(project: Project): List<BibtexEntry> {
        val body: String = client.get(urlString = "https://api.mendeley.com/documents") {
            header("Accept", "application/x-bibtex")
            parameter("view", "bib")
        }.body()

        // Reading the dummy bib file needs to happen in a place where we have read access.
        return runReadAction {
            BibtexEntryListConverter().fromString(body)
        }
    }

    companion object {

        const val NAME = "Mendeley"
        val tokenAttributes = createCredentialsAttributes("$NAME-token")
        val refreshTokenAttributes = createCredentialsAttributes("$NAME-refresh-token")
    }
}