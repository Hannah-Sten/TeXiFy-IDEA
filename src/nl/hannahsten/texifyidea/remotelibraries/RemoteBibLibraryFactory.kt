package nl.hannahsten.texifyidea.remotelibraries

import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import kotlin.random.Random

/**
 * Factory to create remote bib libraries. There is one library for all libraries, because they have to use the same id
 * generator, and all libraries should be able to be constructed from the same information (the id, display name, and optionally
 * sensitive information that is stored in the password safe).
 */
object RemoteBibLibraryFactory {

    /**
     * Create a remote library from the unique identifier and the display name. Reads from the password safe if needed.
     */
    fun fromStorage(identifier: String?): RemoteBibLibrary? {
        val libraryState = RemoteLibraryManager.getInstance().getLibraries()[identifier ?: return null] ?: return null
        return when (libraryState.libraryType.simpleName) {
            ZoteroLibrary::class.simpleName -> ZoteroLibrary(identifier, libraryState.displayName)
            MendeleyLibrary::class.simpleName -> MendeleyLibrary(identifier, libraryState.displayName)
            BibtexFileLibrary::class.simpleName -> BibtexFileLibrary(identifier, libraryState.displayName)
            else -> null
        }
    }

    /**
     * Create a new library with a random unique id. This doesn't use the password safe.
     */
    inline fun <reified T : RemoteBibLibrary> create(displayName: String): T? {
        val identifier = generateId()

        return when (T::class.simpleName) {
            ZoteroLibrary::class.simpleName -> ZoteroLibrary(identifier, displayName) as T
            MendeleyLibrary::class.simpleName -> MendeleyLibrary(identifier, displayName) as T
            BibtexFileLibrary::class.simpleName -> BibtexFileLibrary(identifier, displayName) as T
            else -> null
        }
    }

    /**
     * Generate a random unique id by hashing some integer.
     */
    fun generateId(): String {
        fun sha(str: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(str.toByteArray(UTF_8))
        fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

        // Prepend an a because an xml-tag cannot start with a number.
        fun candidate() = "a" + sha(Random.nextInt(100, 10000).toString()).toHex()

        var cand = candidate()
        while (cand in RemoteLibraryManager.getInstance().getLibraries().keys) {
            cand = candidate()
        }

        return cand
    }
}