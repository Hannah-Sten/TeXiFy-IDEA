package nl.rubensten.texifyidea.insight

import com.intellij.openapi.fileTypes.FileType
import nl.rubensten.texifyidea.file.BibtexFileType
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.file.StyleFileType

/**
 * @author Ruben Schellekens
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

    LATEX("LaTeX", "Latex", setOf(LatexFileType.INSTANCE, StyleFileType.INSTANCE)),
    BIBTEX("BibTeX", "Bibtex", setOf(BibtexFileType))
}