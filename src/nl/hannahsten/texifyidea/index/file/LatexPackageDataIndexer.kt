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
     * Use a positive lookahead to find overlapping matchings if they fall within the 'docs' range.
     */
    private val macroWithDocsRegex = """(?=\\begin\{macro}\{(?<command>\\[a-zA-Z_:]++)}\s*(?<docs>[\s\S]{0,500}))""".toRegex()
    private val docsAfterMacroRegex = """(?:%\s*(?<line>.*))""".toRegex()

    /**
     * Commands that indicate that the documentation of a macro has stopped.
     */
    private val stopsDocs = setOf("\\begin", "\\end", "\\changes")

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val macrosBeingOverloaded = mutableSetOf<String>()

        // Get the command and possible documentation
        macroWithDocsRegex.findAll(inputData.contentAsText).forEach loop@{ macroWithDocsResult ->
            val command = macroWithDocsResult.groups["command"]?.value ?: return@loop
            // The string that hopefully contains some documentation about the macro
            val containsDocs = macroWithDocsResult.groups["docs"]?.value ?: return@loop

            // If we are overloading macros, just save this one to fill with documentation later.
            if (containsDocs.trim(' ', '%').startsWith("\\begin{macro}")) {
                macrosBeingOverloaded.add(command)
            }
            else {
                var docs = ""
                // Strip the line prefixes and guess until where the documentation goes.
                run breaker@{
                    docsAfterMacroRegex.findAll(containsDocs).forEach { line ->
                        if (line.groups["line"]?.value?.containsAny(stopsDocs) == false) {
                            docs += " " + line.groups["line"]?.value
                        }
                        else {
                            return@breaker
                        }
                    }
                }
                map[command] = docs.trim()
                if (macrosBeingOverloaded.isNotEmpty()) {
                    macrosBeingOverloaded.forEach { map[it] = docs.trim() }
                    macrosBeingOverloaded.clear()
                }
            }
        }
        return map
    }
}