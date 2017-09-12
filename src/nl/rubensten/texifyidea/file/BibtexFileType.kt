package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.BibtexLanguage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
object BibtexFileType : LanguageFileType(BibtexLanguage) {

    override fun getName() = "BibTeX bibliography file"

    override fun getDescription() = "BibTeX bibliography file"

    override fun getDefaultExtension() = "bib"

    override fun getIcon() = TexifyIcons.BIBLIOGRAPHY_FILE!!
}