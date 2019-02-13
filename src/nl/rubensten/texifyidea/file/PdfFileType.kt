package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.LanguageFileType
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.TexifyIcons
import javax.swing.Icon

object PdfFileType : LanguageFileType(LatexLanguage.INSTANCE) {
    override fun getIcon(): Icon? = TexifyIcons.PDF_FILE

    override fun getName(): String = "PDF Document"

    override fun getDefaultExtension(): String = "pdf"

    override fun getDescription(): String = "PDF Document"
}
