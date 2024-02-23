package nl.hannahsten.texifyidea.remotelibraries

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import java.io.File

/**
 * A remote bibtex library, with as source a local (but possibly out of project) bibtex file.
 * Useful for example if you have a really large library (e.g. managed with JabRef) for which you want to automatically put references into the project bib file.
 */
class BibtexFileLibrary(val path: String?, override val identifier: String = NAME, override val displayName: String = "BibTeX File") : RemoteBibLibrary(identifier, displayName) {
    companion object {
        const val NAME = "BibTeX File"
    }

    override suspend fun getBibtexString(): Either<RemoteLibraryRequestFailure, String> = either {
        ensureNotNull(path) { RemoteLibraryRequestFailure(displayName, "No path to bibtex file specified") }
        ensure(File(path).exists()) { RemoteLibraryRequestFailure(displayName, "Bibtex file $path not found") }
        File(path).readText()
    }

    override fun destroyCredentials() {}
}