package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.application.runReadAction
import io.ktor.client.statement.*
import nl.hannahsten.texifyidea.RemoteLibraryRequestTeXception
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter

/**
 * Remote library with a unique [identifier].
 */
abstract class RemoteBibLibrary(identifier: String, displayName: String) : ExternalBibLibrary(identifier, displayName) {

    /**
     * Get the bib items from the remote library in bibtex format, then parse the bibtex to obtain all the bib entries.
     *
     * @throws RemoteLibraryRequestTeXception When the request has a non-OK status code so the user can be notified.
     */
    @Throws(RemoteLibraryRequestTeXception::class)
    suspend fun getCollection(): List<BibtexEntry> {
        val (response, body) = getBibtexString()

        if (response.status.value !in 200 until 300) {
            throw RemoteLibraryRequestTeXception(displayName, response)
        }

        // Reading the dummy bib file needs to happen in a place where we have read access.
        return runReadAction {
            BibtexEntryListConverter.fromString(body)
        }
    }

    /**
     * Get the bib items from the remote library in bibtex format.
     */
    abstract suspend fun getBibtexString(): Pair<HttpResponse, String>

    /**
     * Remove any credentials from the password safe.
     *
     * Use `PasswordSafe.instance.set(key, null)` to remove credentials for `key` from the password safe.
     */
    abstract fun destroyCredentials()
}