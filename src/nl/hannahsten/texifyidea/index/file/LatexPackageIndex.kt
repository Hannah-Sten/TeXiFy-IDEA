package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.StyleFileType

/**
 * Index of source files of LaTeX packages.
 *
 * Key: LaTeX command (without backslash).
 * Value: Documentation string.
 */
class LatexPackageIndex : FileBasedIndexExtension<String, String>() {
    private val indexer = LatexPackageDataIndexer()

    override fun getName(): ID<String, String> {
        return ID.create(" nl.hannahsten.texifyidea.LatexPackageIndex")
    }

    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return indexer
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getVersion() = 0

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(StyleFileType)
    }

    override fun dependsOnFileContent() = true
}