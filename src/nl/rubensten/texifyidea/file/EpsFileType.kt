package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons
import javax.swing.Icon

object EpsFileType : LanguageFileType(LatexLanguage.INSTANCE) {
    override fun getIcon(): Icon? = TexifyIcons.FILE!!

    override fun getName(): String = "EPS Image"

    override fun getDefaultExtension(): String = "eps"

    override fun getDescription(): String = "EPS Image"
}
