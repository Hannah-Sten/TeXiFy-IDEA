package nl.hannahsten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.TexifyIcons

/**
 * @author Hannah Schellekens
 */
object ClassFileType : LanguageFileType(LatexLanguage) {

    override fun getName() = "LaTeX class file"

    override fun getDescription() = "LaTeX class file"

    override fun getDefaultExtension() = "cls"

    override fun getIcon() = TexifyIcons.CLASS_FILE

    override fun getDisplayName() = name
}
