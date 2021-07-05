package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.file.StyleFileType

/**
 * Indexes which packages load which other packages.
 * Note that with 'package' we mean here an actual .sty file as you would call in a \usepackage, which may be
 * named different from the ctan package it is contained in.
 *
 * Key: Package name, as it appears in a \RequirePackage-like statement
 * Value: not used. Could possibly be used for metadata about the inclusion (like optional parameters).
 *
 * See [nl.hannahsten.texifyidea.inspections.latex.LatexMissingImportInspection].
 */
class LatexExternalPackageInclusionIndex : FileBasedIndexExtension<String, String>() {

    companion object {

        val id = ID.create<String, String>("nl.hannahsten.texifyidea.external.package.inclusions")
    }

    private val indexer = LatexExternalPackageInclusionDataIndexer()

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
        return DefaultFileTypeSpecificInputFilter(StyleFileType)
    }

    override fun dependsOnFileContent() = true
}