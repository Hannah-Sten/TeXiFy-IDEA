package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Thomas Schouten
 */
object BiblatexDataModelFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "Biblatex data model file"

    override fun getDescription() = "Biblatex data model file"

    override fun getDefaultExtension() = "dbx"

    override fun getIcon() = TexifyIcons.STYLE_FILE!!
}
