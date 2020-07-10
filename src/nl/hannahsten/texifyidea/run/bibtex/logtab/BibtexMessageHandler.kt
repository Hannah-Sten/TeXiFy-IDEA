package nl.hannahsten.texifyidea.run.bibtex.logtab

abstract class BibtexMessageHandler {
    /**
     * Find a message from the given window, last added line is last in the list.
     */
    abstract fun findMessage(window: List<String>): BibtexLogMessage?
}