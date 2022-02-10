package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.file.StyleFileType
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.startsWithAny
import java.io.File
import java.nio.file.Paths

/**
 * Extract docs and do some basic formatting on documentation strings found in dtx files.
 *
 * @author Thomas
 */
object LatexDocsRegexer {

    /**
     * Regexes and replacements which clean up the documentation.
     *
     * **test**
     * Arguments \[mop\]arg should be left in, because they are needed when adding to the autocomplete
     */
    private val formattingReplacers = listOf(
        // Commands to remove entirely,, making sure to capture in the argument nested braces
        Pair("""\\(cite|footnote)\{(\{[^}]*}|[^}])+?}\s*""".toRegex()) { "" },
        // \cs command from the doctools package
        Pair("""(?<pre>[^|]|^)\\c[sn]\{(?<command>[^}]+?)}""".toRegex()) { result -> result.groups["pre"]?.value + "<tt>\\" + result.groups["command"]?.value + "</tt>" },
        // Other commands, except when in short verbatim
        Pair("""(?<pre>[^|]|^)\\(?:textsf|textsc|cmd|pkg|env)\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> result.groups["pre"]?.value + "<tt>" + result.groups["argument"]?.value + "</tt>" },
        // Replace \textbf with <b> tags
        Pair("""\\textbf\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> "<b>${result.groups["argument"]?.value}</b>" },
        // Replace \emph and \textit with <i> tags
        Pair<Regex, (MatchResult) -> String>("""\\(textit|emph)\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> "<i>${result.groups["argument"]?.value}</i>" },
        // Short verbatim, provided by ltxdoc
        Pair("""\|""".toRegex()) { "" },
        // While it is true that text reflows in the documentation popup, so we don't need linebreaks, often package authors include an environment or something else
        // which does depend on linebreaks to be readable, and therefore we keep linebreaks by default.
        Pair("""\n""".toRegex()) { "<br>" },
    )

    /**
     * Commands that indicate that the documentation of a macro has stopped (besides a newline).
     */
    private val stopsDocs = setOf("\\begin{macrocode}", "\\DescribeMacro")

    /**
     * Skip lines that start with one of these strings.
     */
    private val skipLines = arrayOf("\\changes")

    /**
     * Regex for the documentation lines itself.
     * Some things to note:
     * - Docs do not necessarily start on their own line
     * - We do use empty lines to guess where the docs end
     */
    private val docsAfterMacroRegex = """(?:%?\h*(?<line>.+\n))""".toRegex()

    /**
     * Should format to valid HTML as used in the docs popup.
     * Only done when indexing, but it should still be fast because it can be done up to 28714 times for full TeX Live.
     */
    fun format(docs: String): String {
        var formatted = docs.trim()
        formattingReplacers.forEach { formatted = it.first.replace(formatted, it.second).trim() }
        return formatted.trim()
    }

    /**
     * Extract documentation keys and values from [inputData] based on [regex] and put them in [map].
     * Assumes that the regex contains groups with the name "key" or "key1" and "value".
     */
    fun getDocsByRegex(inputData: FileContent, map: MutableMap<String, String>, regex: Regex) {
        val macrosBeingOverloaded = mutableSetOf<String>()

        regex.findAll(inputData.contentAsText).forEach loop@{ macroWithDocsResult ->
            val key = macroWithDocsResult.groups["key"]?.value ?: macroWithDocsResult.groups["key1"]?.value ?: return@loop
            // The string that hopefully contains some documentation about the macro
            val containsDocs = macroWithDocsResult.groups["value"]?.value ?: ""

            // If we are overloading macros, just save this one to fill with documentation later.
            if (containsDocs.trim(' ', '%').startsWithAny("\\begin{macro}", "\\DescribeMacro")) {
                macrosBeingOverloaded.add(key)
            }
            else {
                var docs = ""
                // Strip the line prefixes and guess until where the documentation goes.
                run breaker@{
                    docsAfterMacroRegex.findAll(containsDocs).forEach { lineResult ->
                        val line = lineResult.groups["line"]?.value ?: return@forEach
                        if (line.trim().startsWithAny(*skipLines)) {
                            return@forEach
                        }
                        else if (!line.containsAny(stopsDocs) && line.trim(' ', '%').isNotBlank()) {
                            docs += " $line"
                        }
                        else {
                            return@breaker
                        }
                    }
                }

                // Avoid overwriting existing docs with an empty string
                val formatted = format(docs.trim())
                if (key in map.keys && formatted.isBlank()) return@loop

                map[key] = formatted
                if (macrosBeingOverloaded.isNotEmpty()) {
                    macrosBeingOverloaded.forEach { map[it] = format(docs.trim()) }
                    macrosBeingOverloaded.clear()
                }
            }
        }
    }

    /**
     * Determine which files to index.
     */
    val inputFilter = FileBasedIndex.InputFilter { file ->
        if (file.fileType is LatexSourceFileType) return@InputFilter true
        if (file.fileType !is StyleFileType) return@InputFilter false
        // Some packages don't have a dtx file, so for those we include the sty files (which won't have documentation)
        // This is some work, but it saves us indexing these packages and filtering out the duplicates later, so it's probably worth it
        // Also it's not always easy to find out in which package a certain environment is defined (for example, the proof environment from the amscls/amsthm package is defined in amscls/amsclass.dtx) so we also include the sty files to not miss anything,
        // unless we know that we have the dtx file of the package, then we assume there's no need to index the sty and it will just introduce unnecessary duplicates in the index
        """(?<root>.+)tex.latex.(?<package>.+)[\\/](?<filename>[^\\/\.]+)\.sty""".toRegex().matchEntire(file.path)?.let { match ->
            val root = match.groups["root"]?.value ?: return@InputFilter false
            val packageName = match.groups["package"]?.value ?: return@InputFilter false
            val fileName = match.groups["filename"]?.value ?: return@InputFilter false
            val dtxFilePath = File(Paths.get(root, "source", "latex", packageName, "$fileName.dtx").toUri())
            if (!dtxFilePath.exists()) {
                return@InputFilter true
            }
        }
        return@InputFilter false
    }
}