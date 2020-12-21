package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.LatexSourceFileType

/**
 * Index of source files of LaTeX packages.
 * The actual indexing is done by [LatexPackageDataIndexer].
 * The paths that have to be indexed are given by [LatexIndexableSetContributor].
 * todo add commands from index to autocompletion
 *
 * Key: LaTeX command (without backslash).
 * Value: Documentation string.
 *
 * @author Thomas
 */
class LatexPackageIndex : FileBasedIndexExtension<String, String>() {
    private val indexer = LatexPackageDataIndexer()

    override fun getName(): ID<String, String> {
        return ID.create("nl.hannahsten.texifyidea.LatexPackageIndex")
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
        return FileBasedIndex.InputFilter {
            it.fileType is LatexSourceFileType
        }
    }

    override fun dependsOnFileContent() = true
}