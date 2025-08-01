package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys

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
class LatexExternalCommandIndexEx : FileBasedIndexExtension<String, String>() {
    private val indexer = LatexExternalCommandDataIndexer()

    override fun getName(): ID<String, String> {
        return LatexFileBasedIndexKeys.EXTERNAL_COMMANDS
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

    override fun getVersion() = 1

    override fun getInputFilter() = LatexDocsRegexer.inputFilter

    override fun dependsOnFileContent() = true

    override fun traceKeyHashToVirtualFileMapping() = true
}

object LatexExternalCommandIndex : FileBasedIndexRetriever<String, String>(LatexFileBasedIndexKeys.EXTERNAL_COMMANDS)
