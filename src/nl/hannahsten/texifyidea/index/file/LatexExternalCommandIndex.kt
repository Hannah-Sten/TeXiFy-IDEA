package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

/**
 * Index of all defined commands in source files (dtx) of LaTeX packages.
 * The actual indexing is done by [LatexExternalCommandDataIndexer].
 * The paths that have to be indexed are given by [LatexIndexableSetContributor].
 *
 * When developing, the index is present in build/idea-sandbox/system-test/index
 *
 * Key: LaTeX command (with backslash).
 * Value: Documentation string.
 *
 * @author Thomas
 */
class LatexExternalCommandIndex : FileBasedIndexExtension<String, String>() {

    companion object {

        val id = ID.create<String, String>("nl.hannahsten.texifyidea.external.commands")
    }

    private val indexer = LatexExternalCommandDataIndexer()

    override fun getName(): ID<String, String> {
        return id
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

    override fun getVersion() = 2

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return LatexDocsRegexer.inputFilter
    }

    override fun dependsOnFileContent() = true
}