package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent

/**
 * Index file contents for [LatexExternalPackageInclusionIndex].
 */
class LatexExternalPackageInclusionDataIndexer : DataIndexer<String, String, FileContent> {

    companion object {

        val packageInclusionsRegex = """\\(RequirePackage|usepackage)(\[[^]]+])?\{(?<package>[^}]+)}""".toRegex()
    }

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()

        packageInclusionsRegex.findAll(inputData.contentAsText).forEach {
            val packages = it.groups["package"]?.value ?: return@forEach
            for (key in packages.split(",")) {
                result[key] = inputData.fileName
            }
        }

        return result
    }
}