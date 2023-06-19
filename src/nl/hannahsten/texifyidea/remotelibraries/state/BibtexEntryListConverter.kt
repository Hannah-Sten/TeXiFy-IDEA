package nl.hannahsten.texifyidea.remotelibraries.state

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.util.xmlb.Converter
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.parser.childrenOfType

/**
 * Convert from String to [BibtexEntry], and the other way around, using PSI and the parser.
 * This implies that this converter has to be used from within a thread that has read access.
 */
class BibtexEntryListConverter : Converter<List<BibtexEntry>>() {

    override fun toString(value: List<BibtexEntry>): String {
        return value.joinToString("\n") { it.text }
    }

    override fun fromString(value: String): List<BibtexEntry> {
        val project = ProjectManager.getInstance().defaultProject
        val file = PsiFileFactory.getInstance(project).createFileFromText("DUMMY.bib", BibtexFileType, value)

        if (file.children.any { it is PsiErrorElement }) {
            Notification(
                "LaTeX",
                "Library could not be imported completely",
                "Some bib elements might be missing in the imported library. See the TeXiFy wiki for more information.",
                NotificationType.WARNING
            ).notify(project)
        }

        return file.childrenOfType<BibtexEntry>().toList()
    }
}