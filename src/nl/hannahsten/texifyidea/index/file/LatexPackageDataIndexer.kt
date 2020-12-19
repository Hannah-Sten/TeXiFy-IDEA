package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent

class LatexPackageDataIndexer : DataIndexer<String, String, FileContent> {
    override fun map(inputData: FileContent): MutableMap<String, String> {
        return mutableMapOf(inputData.file.name to "test docs 3")
    }
}