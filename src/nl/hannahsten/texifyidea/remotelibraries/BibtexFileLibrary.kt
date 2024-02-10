package nl.hannahsten.texifyidea.remotelibraries

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.intellij.ide.passwordSafe.PasswordSafe
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.util.CredentialAttributes
import java.io.File

class BibtexFileLibrary(override val identifier: String = NAME, override val displayName: String = "BibTeX File") : RemoteBibLibrary(identifier, displayName) {
    companion object {
        const val NAME = "BibTeX File"
    }

    override suspend fun getBibtexString(): Either<RemoteLibraryRequestFailure, String> = either {
        val path = PasswordSafe.instance.get(CredentialAttributes.BibtexFile.path)?.userName
        ensure(path != null) { RemoteLibraryRequestFailure(displayName, "No path to bibtex file found") }
        File(path).readText()
    }

    override fun destroyCredentials() {
        PasswordSafe.instance.set(CredentialAttributes.BibtexFile.path, null)
    }
}