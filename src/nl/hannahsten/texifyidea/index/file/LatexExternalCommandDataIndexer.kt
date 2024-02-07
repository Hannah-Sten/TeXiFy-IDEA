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
         * Supports a definition with or without braces.
         */
        val describeMacroRegex =
            """(?=\\DescribeMacro(?:(?<key>\\[a-zA-Z_:]+\*?)|\{(?<key1>\\[a-zA-Z_:]+\*?)})\s*(?<value>[\s\S]{0,500}))""".toRegex()

        // Avoid matching commands with @ in it by using an atomic group
        val directCommandDefinitionRegex = """\\(DeclareRobustCommand|newcommand|def)\*?(?:(?<key>(?>\\[a-zA-Z_:]+\*?)(?!@))|\{(?<key1>\\[a-zA-Z_:]+\*?)})(?<value>)""".toRegex()

        /**
         * See sourc2e.pdf:
         *
         * ```\DeclareTextCommand{command}{encoding}[number][default]{commands}```
         * This command is like \newcommand, except that it defines a command which is specific to one encoding.
         *
         * Similar: \DeclareTextSymbol, \DeclareTextAccent, \DeclareTextComposite, \DeclareTextCompositeCommand
         */
        val declareTextSymbolRegex = """\\DeclareText(?:Symbol|Accent)\{(?<key>[^}]+)}(?<value>\{(?<encoding>[^}]+)}(?:.+)*?\{(?<slot>[^}]+)})""".toRegex()
        val declareTextCommandRegex = """\\DeclareTextCommand\{(?<key>[^}]+)}\{(?<value>[^}]+)}""".toRegex()
    }

    override fun map(inputData: FileContent): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()

        listOf(
            macroWithDocsRegex,
            describeMacroRegex,
            directCommandDefinitionRegex,
            directCommandDefinitionRegex,
            declareTextSymbolRegex,
            declareTextCommandRegex,
        ).forEach { LatexDocsRegexer.getDocsByRegex(inputData, map, it) }

        return map
    }
}