package nl.hannahsten.texifyidea.run.bibtex.logtab

object BibtexLogMagicRegex {

    val bibFileOpened = """^Database file #(\d+): (?<file>.+)$""".toRegex()

    /*
     * Errors
     */
    val auxErrPrint = """---line (?<line>\d+) of file (?<file>.+)$""".toRegex()
    val cleanUpAndLeave = """^Aborted at line (?<line>\d+) of file (?<file>.+)$""".toRegex()
    val auxEndErr = """^(?<message>I found no .+)---while reading file (?<file>.+)$""".toRegex()
    val bstExWarnPrint = """while executing$auxErrPrint""".toRegex() // line 6874

    /*
     * Warnings
     */
    val bibLnNumPrint = """--line (?<line>\d+) of file (?<file>.+)$""".toRegex()
    val warning = """^Warning--(?<message>.+)$""".toRegex()
}