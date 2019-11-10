package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Thomas Schouten
 */
object BiblatexStyleFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "Biblatex style file"

    override fun getDescription() = "Biblatex style file"

    override fun getDefaultExtension() = "bbx"

    override fun getIcon() = TexifyIcons.STYLE_FILE!!
}
