package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Thomas Schouten
 */
object TikzFileType : LanguageFileType(LatexLanguage) {

    override fun getName() = "TikZ picture file"

    override fun getDescription() = "TikZ picture file"

    override fun getDefaultExtension() = "tikz"

    override fun getIcon() = TexifyIcons.TIKZ_FILE

    override fun getDisplayName() = name
}
