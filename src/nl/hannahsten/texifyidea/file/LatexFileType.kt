package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Sten Wessel
 */
object LatexFileType : LanguageFileType(LatexLanguage) {

    override fun getName() = "LaTeX source file"

    override fun getDescription() = "LaTeX source file"

    override fun getDefaultExtension() = "tex"

    override fun getIcon() = TexifyIcons.LATEX_FILE
}
