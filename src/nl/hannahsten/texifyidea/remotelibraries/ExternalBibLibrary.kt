package nl.hannahsten.texifyidea.remotelibraries

abstract class ExternalBibLibrary(
    /**
     * Has to be unique.
     */
    open val identifier: String,
    /**
     * How to display the name of the library.
     */
    open val displayName: String
)