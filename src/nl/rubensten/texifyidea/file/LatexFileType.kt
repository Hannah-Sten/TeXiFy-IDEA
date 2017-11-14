package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Sten Wessel
 */
object LatexFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "LaTeX source file"

    override fun getDescription() = "LaTeX source file"

    override fun getDefaultExtension() = "tex"

    override fun getIcon() = TexifyIcons.LATEX_FILE!!
}
