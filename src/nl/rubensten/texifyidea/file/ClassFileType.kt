package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons

/**
 * @author Ruben Schellekens
 */
object ClassFileType : LanguageFileType(LatexLanguage.INSTANCE) {

    override fun getName() = "LaTeX class file"

    override fun getDescription() = "LaTeX class file"

    override fun getDefaultExtension() = "cls"

    override fun getIcon() = TexifyIcons.CLASS_FILE!!
}
