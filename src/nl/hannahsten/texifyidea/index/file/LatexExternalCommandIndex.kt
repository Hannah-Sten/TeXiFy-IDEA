package nl.hannahsten.texifyidea.index.file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import java.io.File
import java.nio.file.Paths

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

    override fun getVersion() = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file ->
            if (file.fileType is LatexSourceFileType) return@InputFilter true
            if (file.fileType !is StyleFileType) return@InputFilter false
            // Some packages don't have a dtx file, so for those we include the sty files (which won't have documentation)
            // This is some work, but it saves us indexing these packages and filtering out the duplicates later, so it's probably worth it
            """(?<root>.+)tex.latex.(?<package>[^\\/]+)[\\/].+""".toRegex().matchEntire(file.path)?.let { match ->
                val root = match.groups["root"]?.value ?: return@InputFilter false
                val packageName = match.groups["package"]?.value ?: return@InputFilter false
                val sourceRoot = File(Paths.get(root, "source", "latex", packageName).toUri())
                if (sourceRoot.isDirectory && sourceRoot.listFiles().any { it.extension == LatexSourceFileType.defaultExtension }) {
                    return@InputFilter true
                }
            }
            return@InputFilter false
        }
    }

    override fun dependsOnFileContent() = true
}