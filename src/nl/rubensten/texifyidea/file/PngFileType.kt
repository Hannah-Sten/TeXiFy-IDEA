package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons
import javax.swing.Icon

object PngFileType : LanguageFileType(LatexLanguage.INSTANCE) {
    override fun getIcon(): Icon? = TexifyIcons.FILE!!

    override fun getName(): String = "PNG Image"

    override fun getDefaultExtension(): String = "png"

    override fun getDescription(): String = "PNG Image"
}
