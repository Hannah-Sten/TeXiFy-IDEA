package nl.hannahsten.texifyidea.remotelibraries.zotero

import com.intellij.ide.passwordSafe.PasswordSafe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibrary
import nl.hannahsten.texifyidea.remotelibraries.Temp
import nl.hannahsten.texifyidea.util.CredentialAttributes
import nl.hannahsten.texifyidea.util.createCredentialsAttributes

class ZoteroLibrary(private val userID: String = Temp.userID, private val userApiKey: String = Temp.userApiKey) : RemoteBibLibrary(
    NAME
) {

    private val client by lazy { HttpClient(CIO) }

    override suspend fun getBibtexString(): String {
        return client.get("https://api.zotero.org/users/$userID/items") {
            headers {
                append("Zotero-API-version", VERSION.toString())
                append("Zotero-API-key", userApiKey)
            }
            parameter("format", "bibtex")
        }.body()
    }

    companion object {

        const val VERSION = 3
        const val NAME = "Zotero"

        fun createFromPasswordSafe(): ZoteroLibrary? {
            val credentials = PasswordSafe.instance.get(CredentialAttributes.Zotero.userAttributes)
            return if (credentials?.userName == null || credentials.password == null) null
            else {
                ZoteroLibrary(credentials.userName.toString(), credentials.password.toString())
            }
        }
    }
}