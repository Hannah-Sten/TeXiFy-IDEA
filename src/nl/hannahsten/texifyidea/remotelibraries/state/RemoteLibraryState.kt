package nl.hannahsten.texifyidea.remotelibraries.state

import com.intellij.util.xmlb.annotations.OptionTag
import nl.hannahsten.texifyidea.psi.BibtexEntry

/**
 * State that can read and write library contents to and from disk.
 *
 * This is (de)serialized with [LibraryStateConverter], which converts this class to xml. Therefore, the keys in [libraries]
 * should be valid xml tags, e.g., they must start with a letter and cannot contain colons (xml namespaces are not supported).
 */
data class RemoteLibraryState(
    @OptionTag(converter = LibraryStateConverter::class)
    var libraries: Map<String, LibraryState> = emptyMap()
)

/**
 * State of a library that stores all the necessary information to be able to reconstruct a library when loading it from disk.
 *
 * Note that user information (like passwords) is not stored here, but in the [password safe](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html).
 */
data class LibraryState(
    /**
     * Store the display name of the library.
     */
    val displayName: String,
    /**
     * Store the type of the library, so we can reconstruct it when loading the state from disk.
     */
    val libraryType: Class<*>,
    /**
     * Store the entries from the library, so we don't have to query the libraries on startup.
     */
    var entries: List<BibtexEntry>
)