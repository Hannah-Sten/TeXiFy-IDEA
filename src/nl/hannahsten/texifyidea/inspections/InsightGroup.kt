package nl.hannahsten.texifyidea.inspections

import com.intellij.openapi.fileTypes.FileType
import nl.hannahsten.texifyidea.file.BibtexFileType
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.file.StyleFileType

/**
 * @author Hannah Schellekens
 */
enum class InsightGroup(

    /**
     * The name that gets displayed in the inspection settings.
     */
    val displayName: String,

    /**
     * The prefix of all internal inspection names.
     */
    val prefix: String,

    /**
     * The filetypes to which the inspection must be applied.
     */
    val fileTypes: Set<FileType>
) {

    LATEX("LaTeX", "Latex", setOf(LatexFileType, StyleFileType)),
    BIBTEX("BibTeX", "Bibtex", setOf(BibtexFileType));

    companion object {

        @JvmStatic
        fun byFileType(fileType: FileType): List<InsightGroup> {
            return values().filter { fileType in it.fileTypes }
        }
    }
}