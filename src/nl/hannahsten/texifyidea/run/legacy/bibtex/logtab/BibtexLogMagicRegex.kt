package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab

object BibtexLogMagicRegex {

    val bibFileOpened =
        """^Database file #(\d+): (?<file>.+)$""".toRegex()
    val biberFileOpened =
        """^INFO - Found BibTeX data source '(?<file>.+)'$""".toRegex()

    val bibtexNumberOfWarnings =
        """\(There (was 1 warning|were (?<number>\d+) warnings)\)""".toRegex()
    val bibtexNumberOfErrors =
        """\(There (was 1 error message|were (?<number>\d+) error messages)\)""".toRegex()

    val biberNumberOfWarnings =
        """INFO - WARNINGS: (?<number>\d+)""".toRegex()
    val biberNumberOfErrors =
        """INFO - ERRORS: (?<number>\d+)""".toRegex()

    /*
     * Errors
     */
    val auxErrPrint =
        """---line (?<line>\d+) of file (?<file>.+)$""".toRegex()
    val cleanUpAndLeave =
        """^Aborted at line (?<line>\d+) of file (?<file>.+)$""".toRegex()
    val auxEndErr =
        """^(?<message>I found no .+)---while reading file (?<file>.+)$""".toRegex()
    val bstExWarnPrint =
        """while executing$auxErrPrint""".toRegex() // line 6874
    val nonexistentCrossReferenceError =
        """^refers to entry .+, which doesn't exist$""".toRegex()

    /*
     * Warnings
     */
    val bibLnNumPrint =
        """--line (?<line>\d+) of file (?<file>.+)\s*$""".toRegex()
    val warning =
        """^Warning--(?<message>.+)\s*$""".toRegex()
    val noFields =
        """^Warning--(?<message>.+)$bibLnNumPrint\s*$""".toRegex()
    val nestedCrossReference =
        """^Warning--you've nested cross references--entry .+$""".toRegex()

    /*
     * Biber
     */
    val biberError =
        """^ERROR - (?<message>.+)$""".toRegex()
    val biberErrorBibtexSubsystem =
        """^ERROR - BibTeX subsystem: .+, line (?<line>\d+), (?<message>.+)$""".toRegex()
    val biberWarning =
        """^WARN - (?<message>.+)$""".toRegex()
    val biberWarningBibtexSubsystem =
        """^WARN - BibTeX subsystem: .+, line (?<line>\d+), warning: (?<message>.+)$""".toRegex()
    val biberWarningInFile =
        """^WARN - (?<message>.+) in file .+, skipping ...$""".toRegex()
}