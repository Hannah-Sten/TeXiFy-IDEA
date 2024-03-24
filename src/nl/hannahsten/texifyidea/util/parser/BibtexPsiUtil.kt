package nl.hannahsten.texifyidea.util.parser

import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.evaluate

fun BibtexEntry.getTitle(): String {
    val stub = this.stub
    return stub?.title ?: this.getTagContent("title")
}

fun BibtexEntry.getAuthors(): List<String> {
    val stub = this.stub
    if (stub != null) return stub.authors.filterNotNull()
    val authorList = this.getTagContent("author")
    return listOf(*authorList.split(" and ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
}

fun BibtexEntry.getYear(): String {
    val stub = this.stub
    return stub?.year ?: this.getTagContent("year")
}

fun BibtexEntry.getIdentifier(): String {
    val stub = this.stub
    return stub?.identifier
        ?: firstChildOfType(BibtexId::class)?.text
        ?: this.entryContent?.tagList?.firstOrNull()?.key?.text
        ?: this.preamble?.text
        ?: ""
}

fun BibtexEntry.getAbstract(): String {
    return this.getTagContent("abstract")
}

fun BibtexEntry.getTagContent(tagName: String?): String {
    val entryContent = this.entryContent ?: return ""

    entryContent.tagList.forEach {
        if (tagName.equals(it.key.text, ignoreCase = true)) {
            return it.content?.evaluate() ?: ""
        }
    }

    return ""
}
