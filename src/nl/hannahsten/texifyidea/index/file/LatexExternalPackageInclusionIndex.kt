package nl.hannahsten.texifyidea.index.file

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.ClassFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys

/**
 * Indexes which packages and classes load which other packages.
 * Note that with 'package' we mean here an actual .sty file as you would call in a \usepackage, which may be
 * named different from the ctan package it is contained in.
 *
 * Key: Package name, as it appears in a \RequirePackage-like statement
 * Value: not used. Could possibly be used for metadata about the inclusion (like optional parameters). Currently it is the file name for easier debugging, and as such it should be read package "key" is included by package "value".
 *
 * See [nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages.LatexMissingImportInspection].
 */
class LatexExternalPackageInclusionIndexEx : FileBasedIndexExtension<String, String>() {

    private val indexer = LatexExternalPackageInclusionDataIndexer()

    override fun getName(): ID<String, String> {
        return LatexFileBasedIndexKeys.PACKAGE_INCLUSIONS
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

    override fun getVersion() = 3

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(StyleFileType, ClassFileType)
    }

    override fun dependsOnFileContent() = true
}

object LatexExternalPackageIndex : FileBasedIndexRetriever<String, String>(LatexFileBasedIndexKeys.PACKAGE_INCLUSIONS) {

    fun getAllPackageInclusions(scope: GlobalSearchScope): Set<String> {
        return getAllKeys(scope)
    }
}