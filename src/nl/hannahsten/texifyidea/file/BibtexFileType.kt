package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
object BibtexFileType : LanguageFileType(BibtexLanguage) {

    override fun getName() = "BibTeX bibliography file"

    override fun getDescription() = "BibTeX bibliography file"

    override fun getDefaultExtension() = "bib"

    override fun getIcon() = TexifyIcons.BIBLIOGRAPHY_FILE

    override fun getDisplayName() = name
}