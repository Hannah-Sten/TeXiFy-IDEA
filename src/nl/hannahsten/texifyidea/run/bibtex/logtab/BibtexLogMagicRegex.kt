package nl.hannahsten.texifyidea.run.bibtex.logtab

object BibtexLogMagicRegex {

    val auxErrPrint = """^---line (?<line>\d+) of file (?<file>.+)$""".toRegex()
}