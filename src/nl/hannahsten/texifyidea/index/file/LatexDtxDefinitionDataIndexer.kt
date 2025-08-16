package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.SingleEntryIndexer

abstract class LatexDtxDefinitionDataIndexer : SingleEntryIndexer<List<String>>(false) {

    override fun computeValue(inputData: FileContent): List<String> {
        val lines = inputData.contentAsText.lineSequence()
        val definitions = mutableListOf<String>()
        val codeBuffer = StringBuilder()
        var inMacroCode = false

        // extract code blocks between \begin{macrocode} and \end{macrocode}
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith('%')) {
                val content = trimmed.substring(1)
                if (content == "\\begin{macrocode}") {
                    inMacroCode = true
                    continue
                }
                if (content == "\\end{macrocode}") {
                    inMacroCode = false
                    parseCode(codeBuffer.toString(), definitions)
                    codeBuffer.clear()
                    continue
                }
            }
            else if (inMacroCode) {
                codeBuffer.appendLine(line)
            }
        }

        return definitions
    }

    protected abstract fun parseCode(code: String, definitions: MutableList<String>)
}

// object LatexDtxCommandDefinitionDataIndexer : LatexDtxDefinitionDataIndexer() {
//
//
//    override fun parseCode(code: String, definitions: MutableList<String>) {
//        DIRECT_COMMAND_REGEX.findAll(code).forEach { matchResult ->
//            val commandName = matchResult.groups["name"]?.value
//            if (commandName != null) {
//                definitions.add(commandName)
//            }
//        }
//    }
// }
//
// object LatexDtxEnvDefinitionDataIndexer : LatexDtxDefinitionDataIndexer() {
//    private val ENVIRONMENT_REGEX = Regex(
//    )
//
//    override fun parseCode(code: String, definitions: MutableList<String>) {
//        ENVIRONMENT_REGEX.findAll(code).forEach { matchResult ->
//            val envName = matchResult.groups["name"]?.value
//            if (envName != null) {
//                definitions.add(envName)
//            }
//        }
//    }
// }