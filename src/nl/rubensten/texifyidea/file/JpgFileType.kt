package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons
import javax.swing.Icon

object JpgFileType : LanguageFileType(LatexLanguage.INSTANCE) {
    override fun getIcon(): Icon? = TexifyIcons.FILE!!

    override fun getName(): String = "JPG Image"

    override fun getDefaultExtension(): String = "jpg"

    override fun getDescription(): String = "JPG Image"
}
