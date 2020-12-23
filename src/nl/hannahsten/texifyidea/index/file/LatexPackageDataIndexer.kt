package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.util.containsAny

/**
 * Get the content we need from LaTeX Package source files.
 */
class LatexPackageDataIndexer : DataIndexer<String, String, FileContent> {
    /**
     * Capture the command being defined with the macro environment and the part directly after that that probably contains docs.
     * We limit to a certain length to avoid matching very large strings if the regex fails.
     * Then we use a separate regex to get the actual docs out of that string, matching lines until a certain stopping criterion.
     */
    private val macroWithDocsRegex = """\\begin\{macro\}\{(?<command>\\.+)\}\s*(?<docs>[\s\S]{0,100})""".toRegex()
    private val docsAfterMacroRegex = """(?:%\s*(?<line>.*))""".toRegex()

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        // Get the command and possible documentation
        macroWithDocsRegex.findAll(inputData.contentAsText).forEach loop@{
            val command = it.groups["command"]?.value ?: return@loop
            val containsDocs = it.groups["docs"]?.value ?: return@loop
            var docs = ""
            // Strip the line prefixes and guess until where the documentation  goes.
            docsAfterMacroRegex.findAll(containsDocs).forEach { line ->
                if (line.groups["line"]?.value?.containsAny(setOf("\\begin", "\\end")) == false) {
                    docs += " " + line.groups["line"]?.value
                }
            }
            map[command] = docs.trim()
        }
        return map
    }
}