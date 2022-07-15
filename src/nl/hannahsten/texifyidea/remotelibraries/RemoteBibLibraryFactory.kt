package nl.hannahsten.texifyidea.remotelibraries

import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest

object RemoteBibLibraryFactory {

    var count = 0

    /**
     * Create a remote library from the unique identifier and the display name.
     */
    fun fromStorage(identifier: String?): RemoteBibLibrary? {
        val libraryState = RemoteLibraryManager.getInstance().libraries[identifier ?: return null] ?: return null
        return when (libraryState.libraryType.simpleName) {
            ZoteroLibrary::class.simpleName -> ZoteroLibrary.createFromPasswordSafe(identifier, libraryState.displayName)
            MendeleyLibrary::class.simpleName -> MendeleyLibrary(identifier, libraryState.displayName)
            else -> null
        }
    }

    inline fun <reified T : RemoteBibLibrary> create(displayName: String): T? {
        val identifier = generateId()

        return when (T::class.simpleName) {
            ZoteroLibrary::class.simpleName -> ZoteroLibrary(identifier, displayName) as T
            MendeleyLibrary::class.simpleName -> MendeleyLibrary(identifier, displayName) as T
            else -> null
        }
    }

    fun generateId(): String {
        fun sha(str: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(str.toByteArray(UTF_8))
        fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

        count++

        // Prepend an a because an xml-tag cannot start with a number.
        return "a" + sha(count.toString()).toHex()
    }
}