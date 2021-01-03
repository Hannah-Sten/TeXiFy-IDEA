package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent

/**
 * Get the content we need from LaTeX Package source files.
 * @author Thomas
 */
class LatexExternalCommandDataIndexer : DataIndexer<String, String, FileContent> {
    companion object {
        /**
         * Capture the command being defined with the macro environment and the part directly after that that probably contains docs.
         * We limit to a certain length to avoid matching very large strings if the regex fails.
         * Then we use a separate regex to get the actual docs out of that string, matching lines until a certain stopping criterion.
         * Use a positive lookahead to find overlapping matchings if they fall within the 'docs' range.
         */
        val macroWithDocsRegex =
            """(?=\\begin\{macro}\{(?<key>\\[a-zA-Z_:]+)}\s*(?<value>[\s\S]{0,500}))""".toRegex()

        /**
         * Documentation given by \DescribeMacro.
         */
        val describeMacroRegex =
            """(?=\\DescribeMacro(?:(?<key>\\[a-zA-Z_:]+\*?)|\{(?<key1>\\[a-zA-Z_:]+\*?)})\s*(?<value>[\s\S]{0,500}))""".toRegex()
    }

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()

        LatexDocsRegexer.getDocsByRegex(inputData, map, macroWithDocsRegex)
        LatexDocsRegexer.getDocsByRegex(inputData, map, describeMacroRegex)
        return map
    }
}