package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab

abstract class BibtexMessageHandler {

    /**
     * Find a message from the given window, last added line is last in the list.
     *
     * @param currentFile: Currently open bib file.
     */
    abstract fun findMessage(window: List<String>, currentFile: String): BibtexLogMessage?
}