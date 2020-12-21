package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent

/**
 * Get the content we need from LaTeX Package source files.
 */
class LatexPackageDataIndexer : DataIndexer<String, String, FileContent> {
    private val macroWithDocsRegex = """\\begin\{macro\}\{(?<command>\\.+)\}\s*(?:%\s*(?!.*(\\begin|\\end))(?<docs>.*)\s){0,4}""".toRegex()

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        macroWithDocsRegex.findAll(inputData.contentAsText).forEach {
            val command = it.groups["command"]?.value
            if (command != null) {
                map[command] = it.groups["docs"]?.value ?: ""
            }
        }
        return map
    }
}