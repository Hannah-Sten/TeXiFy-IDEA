package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Thomas Schouten
 */
object TikzFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "TikZ picture file"

    override fun getDescription() = "TikZ picture file"

    override fun getDefaultExtension() = "tikz"

    override fun getIcon() = TexifyIcons.LATEX_FILE!!
}
