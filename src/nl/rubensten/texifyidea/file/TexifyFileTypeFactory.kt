package nl.rubensten.texifyidea.file

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType

/**
 * @author Sten Wessel
 */
class TexifyFileTypeFactory : FileTypeFactory() {

    override fun createFileTypes(consumer: FileTypeConsumer) {
        consumer.register(
                LatexFileType,
                StyleFileType,
                ClassFileType,
                BibtexFileType,
                TikzFileType
        )
    }

    private fun FileTypeConsumer.register(vararg fileTypes: LanguageFileType) {
        fileTypes.forEach { consume(it, it.defaultExtension) }
    }
}