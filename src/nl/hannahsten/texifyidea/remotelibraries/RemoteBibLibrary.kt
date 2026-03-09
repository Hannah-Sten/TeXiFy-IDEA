package nl.hannahsten.texifyidea.remotelibraries

import arrow.core.Either
import arrow.core.raise.either
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter

/**
 * Remote library with a unique [identifier].
 */
abstract class RemoteBibLibrary(open val identifier: String, open val displayName: String) {

    companion object {

        fun showNotification(project: Project, libraryName: String, statusMessage: String) {
            val title = "Something went wrong when retrieving library from $libraryName"
            Notification("LaTeX", title, statusMessage, NotificationType.ERROR).notify(project)
        }
    }

    /**
     * Get the bib items from the remote library in bibtex format, then parse the bibtex to obtain all the bib entries.
     *
     * When the request has a non-OK status code, return a failure instead.
     */
    suspend fun getCollection(): Either<RemoteLibraryRequestFailure, List<BibtexEntry>> = either {
        val body = getBibtexString().bind()

        // Reading the dummy bib file needs to happen in a place where we have read access.
        runReadAction {
            try {
                BibtexEntryListConverter().fromString(body)
            } catch (e: Exception) {
                raise(RemoteLibraryRequestFailure(displayName, body))
            }
        }
    }

    /**
     * Get the bib items from the remote library in bibtex format.
     */
    abstract suspend fun getBibtexString(): Either<RemoteLibraryRequestFailure, String>

    /**
     * Remove any credentials from the password safe.
     *
     * Use `PasswordSafe.instance.set(key, null)` to remove credentials for `key` from the password safe.
     */
    abstract fun destroyCredentials()
}