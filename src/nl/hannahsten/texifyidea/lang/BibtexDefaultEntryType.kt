package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.Package.Companion.BIBLATEX
import nl.hannahsten.texifyidea.lang.Package.Companion.DEFAULT

/**
 * @author Hannah Schellekens
 */
enum class BibtexDefaultEntryType(
        override val fieldName: String,
        override val description: String,
        override val dependency: Package = DEFAULT
) : BibtexEntryField {

    ADDRESS("address", "Publisher's address (usually just the city, but can be the full address for lesser-known publishers)"),
    ANNOTE("annote", "An annotation for annotated bibliography styles (not typical)"),
    AUTHOR("author", "The name(s) of the author(s) (in the case of more than one author, separated by and)"),
    BOOKTITLE("booktitle", "The title of the book, if only part of it is being cited"),
    CHAPTER("chapter", "The chapter number"),
    CROSSREF("crossref", "The key of the cross-referenced entry"),
    EDITION("edition", "The edition of a book, long form (such as \"First\" or \"Second\")"),
    EDITOR("editor", "The name(s) of the editor(s)"),
    HOWPUBLISHED("howpublished", "How it was published, if the publishing method is nonstandard"),
    INSTITUTION("institution", "The institution that was involved in the publishing, but not necessarily the publisher"),
    JOURNAL("journal", "The journal or magazine the work was published in"),
    KEY("key", "A hidden field used for specifying or overriding the alphabetical order of entries (when the \"author\" and \"editor\" fields are missing). Note that this is very different from the key (mentioned just after this list) that is used to cite or cross-reference the entry."),
    MONTH("month", "The month of publication (or, if unpublished, the month of creation)"),
    NOTE("note", "Miscellaneous extra information"),
    NUMBER("number", "The \"(issue) number\" of a journal, magazine, or tech-report, if applicable. (Most publications have a \"volume\", but no \"number\" field.)"),
    ORGANISATION("organization", "The conference sponsor"),
    PAGES("pages", "Page numbers, separated either by commas or double-hyphens (en dash)."),
    PUBLISHER("publisher", "The publisher's name"),
    SCHOOL("school", "The school where the thesis was written"),
    SERIES("series", "The series of books the book was published in (e.g. \"The Hardy Boys\" or \"Lecture Notes in Computer Science\")"),
    TITLE("title", "The title of the work"),
    TYPE("type", "The field overriding the default type of publication (e.g. \"Research Note\" for techreport, \"{PhD} dissertation\" for phdthesis, \"Section\" for inbook/incollection)"),
    VOLUME("volume", "The volume of a journal or multi-volume book"),
    YEAR("year", "The year of publication (or, if unpublished, the year of creation)"),

    // BibLaTeX
    DATE("date", "The publication date.", BIBLATEX),
    URL("url", "The url of an online publication. If it is not URL-escaped (no '%' chars) it will be URI-escaped according to RFC 3987, that is, even Unicode chars will be correctly escaped.", BIBLATEX);
}