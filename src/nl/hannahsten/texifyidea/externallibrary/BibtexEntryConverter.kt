package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiFileFactory
import com.intellij.util.xmlb.Converter
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.childrenOfType

class BibtexEntryListConverter : Converter<List<BibtexEntry>>() {

    override fun toString(value: List<BibtexEntry>): String {
        return value.joinToString("\n") { it.text }
    }

    override fun fromString(value: String): List<BibtexEntry> {
        return PsiFileFactory.getInstance(ProjectManager.getInstance().defaultProject)
            .createFileFromText("DUMMY.bib", BibtexFileType, value)
            .childrenOfType<BibtexEntry>()
            .toList()
    }
}

class LibraryMapConverter : Converter<Map<String, List<BibtexEntry>>>() {

    override fun toString(value: Map<String, List<BibtexEntry>>): String {
        return value.entries.joinToString(",\n") {
            "${it.key}>${BibtexEntryListConverter().toString(it.value)}"
        }
    }

    override fun fromString(value: String): Map<String, List<BibtexEntry>>? {
        return null
    }

}