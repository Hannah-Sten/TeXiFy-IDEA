package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
object StyleFileType : LanguageFileType(LatexLanguage) {

    override fun getName() = "LaTeX style file"

    override fun getDescription() = "LaTeX style file"

    override fun getDefaultExtension() = "sty"

    override fun getIcon() = TexifyIcons.STYLE_FILE

    override fun getDisplayName() = name
}
