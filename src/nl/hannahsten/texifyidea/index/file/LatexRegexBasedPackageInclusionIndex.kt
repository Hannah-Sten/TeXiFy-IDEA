package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.SingleEntryFileBasedIndexExtension
import com.intellij.util.indexing.SingleEntryIndexer
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.externalizer.StringCollectionExternalizer.STRING_LIST_EXTERNALIZER
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex.PACKAGE_FILE_INPUT_FILTER

class LatexRegexBasedPackageInclusionIndex : SingleEntryFileBasedIndexExtension<List<String>>() {

    override fun getName(): ID<Int, List<String>> = LatexFileBasedIndexKeys.REGEX_PACKAGE_INCLUSIONS

    override fun getInputFilter(): FileBasedIndex.InputFilter = PACKAGE_FILE_INPUT_FILTER

    override fun getValueExternalizer(): DataExternalizer<List<String>?> = STRING_LIST_EXTERNALIZER

    override fun getVersion(): Int = 1

    override fun getIndexer(): SingleEntryIndexer<List<String>> = Indexer

    private object Indexer : SingleEntryIndexer<List<String>>(false) {

        val regex = Regex("""\\(RequirePackage|usepackage)(\[[^]]+])?\{(?<package>[^}]+)}""")

        override fun computeValue(inputData: FileContent): List<String> {
            val lines = inputData.contentAsText.lineSequence()
            val names = mutableSetOf<String>()
            for (line in lines) {
                if (line.startsWith('%')) continue
                regex.findAll(line).forEach { match ->
                    val text = match.groups["package"]?.value ?: return@forEach
                    for (name in text.split(",")) {
                        names.add(name.trim())
                    }
                }
            }
            return names.toList()
        }
    }
}