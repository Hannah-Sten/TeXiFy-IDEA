package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
object StyleFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "LaTeX style file"

    override fun getDescription() = "LaTeX style file"

    override fun getDefaultExtension() = "sty"

    override fun getIcon() = TexifyIcons.STYLE_FILE!!
}
