package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LatexLib

data class LatexSimpleDefinition(
    var name: String, var isEnv: Boolean,
    var arguments: List<LArgument> = emptyList(),
    var description: String = ""
)

/**
 *
 */
object LatexDtxDefinitionDataIndexer : DataIndexer<String, List<LatexSimpleDefinition>, FileContent> {

    //    private val regexRrovidesPackage = "(?<=\\\\ProvidesPackage\\{)(?<name>[a-zA-Z0-9_]+)(?=\\})".toRegex()
    private val regexProvidesPackage = """
        (?<=\\ProvidesPackage\{)(?<name>[a-zA-Z0-9_]+)(?=})
    """.trimIndent().toRegex()
    private val regexBeginMacro = """
        (?<=\\begin\{macro}\{)(?<name>\\[a-zA-Z@]+\*?)(?=})
    """.trimIndent().toRegex()
    private val regexBeginEnv = """
        (?<=\\begin\{environment}\{)(?<name>[a-zA-Z@]+\*?)(?=})
    """.trimIndent().toRegex()

    private data class DefStart(
        val name: String,
        val docLineStart: Int
    )

    private data class DtxDoc(
        val lib: LatexLib,
        val docLines: List<String>,
        val definitions: List<LatexSimpleDefinition>,
        val docs: String = docLines.joinToString("\n")
    )

    val maxLines = 20
    val maxChars = 1000

    private fun parseDtxLines(fileName: String, lines: Sequence<String>): DtxDoc {
        val docLines = mutableListOf<String>()
        val macroStarts = mutableListOf<DefStart>()
        var inMacroCode = false
        var packageName: String? = null
        val definitions = mutableListOf<LatexSimpleDefinition>()

        // extract code blocks between \begin{macrocode} and \end{macrocode}
        for (line in lines) {
            var text = line.trim()
            if (text.startsWith('%')) {
                text = text.substring(1)
                if (text.contains("\\begin{macrocode}")) {
                    inMacroCode = true
                    continue
                }
                if (text.contains("\\end{macrocode}")) {
                    inMacroCode = false
                    continue
                }
            }
            if (!inMacroCode) {
                if (text.contains("\\end{macro}") || text.contains("\\end{environment}")) {
                    if (macroStarts.isNotEmpty()) {
                        val defStart = macroStarts.removeLast()
                        val docString = docLines.subList(defStart.docLineStart, docLines.size).take(maxLines).joinToString("\n").take(maxChars)
                        val isEnv = text.contains("\\end{environment}")
                        var name = defStart.name
                        if (!isEnv) name = name.removePrefix("\\")
                        definitions.add(LatexSimpleDefinition(name, isEnv, description = docString))
                    }
                    continue
                }
                val match = regexBeginMacro.find(text) ?: regexBeginEnv.find(text)
                if (match != null) {
                    val name = match.groups["name"]!!.value
                    macroStarts.add(DefStart(name, docLines.size))
                    continue
                }

                docLines.add(text)
            }
            if (packageName == null) {
                regexProvidesPackage.find(text)?.let { match ->
                    // If the match is not null, it means we found a \ProvidesPackage command
                    // We can extract the package name from the match
                    packageName = match.groups["name"]?.value
                }
            }
        }
        val lib = LatexLib.Package(packageName ?: fileName.substringBeforeLast('.'))
        return DtxDoc(lib, docLines, definitions)
    }

    val regexDescribeMacro = """
        \\DescribeMacro\s*\{?(?<name>\\[a-zA-Z@]+\*?)}?(?<params>([\s\\]*\\(marg|oarg|parg|meta)\{[^}]+})*)?
    """.trimIndent().toRegex()
    val regexDescribeEnv = """
        \\DescribeMacro\{(?<name>[a-zA-Z@]+\*?)}(?<params>([\s\\]*\\(marg|oarg|parg|meta)\{[^}]+})*)?
    """.trimIndent().toRegex()
    val regexParam = """
        \\(?<type>(marg|oarg|parg|meta))\{(?<name>[^}]+)}
    """.trimIndent().toRegex()

    fun extractDescribeBlocks(lines: List<String>): List<String> {
        var describing = false
        var describeStart = 0
        val describeBlocks = mutableListOf<String>()
        for (i in lines.indices) {
            val line = lines[i]
            if (describing) {
                if (line.isBlank()) {
                    describing = false
                    describeBlocks.add(lines.subList(describeStart, i).joinToString("\n"))
                }
                continue
            }
            if (line.contains("\\DescribeMacro") || line.contains("\\DescribeEnv")) {
                describing = true
                describeStart = i
            }
        }
        if (describing) {
            describeBlocks.add(lines.subList(describeStart, lines.size).take(maxLines).joinToString("\n").take(maxChars))
        }
        return describeBlocks
    }

    private fun parseArguments(params: String?): List<LArgument> {
        if (params == null) return emptyList()
        return regexParam.findAll(params).mapTo(mutableListOf()) { match ->
            val typeText = match.groups["type"]!!.value
            val name = match.groups["name"]!!.value
            val type = if (typeText == "oarg") LArgumentType.OPTIONAL else LArgumentType.REQUIRED
            LArgument(name, type)
        }
    }

    fun parseDocDefinitions(lines: List<String>, lib: LatexLib): MutableList<LatexSimpleDefinition> {
        val describeBlocks = extractDescribeBlocks(lines)
        val result = mutableListOf<LatexSimpleDefinition>()
        for (block in describeBlocks) {
            val curSize = result.size
            var pos = 0
            regexDescribeMacro.findAll(block).forEach { match ->
                val name = match.groups["name"]?.value?.removePrefix("\\") ?: return@forEach
                val args = parseArguments(match.groups["params"]?.value)
                result.add(LatexSimpleDefinition(name, isEnv = false, args))
                pos = match.range.last
            }
            regexDescribeEnv.findAll(block).forEach { match ->
                val name = match.groups["name"]?.value ?: return@forEach
                val args = parseArguments(match.groups["params"]?.value)
                result.add(LatexSimpleDefinition(name, isEnv = true, args))
                pos = match.range.last
            }
            pos = block.indexOf('\n', pos) + 1 // move to the next line after the match
            val description = block.substring(pos).trim().take(maxChars)
            for (i in curSize until result.size) {
                result[i].description = description
            }
        }
        return result
    }

    override fun map(inputData: FileContent): Map<String, List<LatexSimpleDefinition>> {
        val lines = inputData.contentAsText.lineSequence()
        val dtxInfo = parseDtxLines(inputData.fileName, lines)
        val definitions = parseDocDefinitions(dtxInfo.docLines, dtxInfo.lib)
        val names = mutableSetOf<String>().apply { definitions.forEach { add(it.name) } }
        for (def in dtxInfo.definitions) {
            if (def.name in names) continue
            definitions.add(def)
        }
        return mapOf(dtxInfo.lib.name to definitions)
    }
}

class LatexDtxDefinitionIndexEx : FileBasedIndexExtension<String, List<LatexSimpleDefinition>>() {
    private val myInputFilter = DefaultFileTypeSpecificInputFilter(LatexSourceFileType) // only .dtx

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getName(): ID<String, List<LatexSimpleDefinition>> {
        return LatexFileBasedIndexKeys.DTX_DEFINITIONS
    }

    override fun getIndexer(): DataIndexer<String, List<LatexSimpleDefinition>, FileContent> {
        return LatexDtxDefinitionDataIndexer
    }

    override fun getValueExternalizer(): DataExternalizer<List<LatexSimpleDefinition>> {
        return MyValueExternalizer
    }

    override fun getVersion() = 4

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return myInputFilter
    }

    override fun dependsOnFileContent() = true

    override fun traceKeyHashToVirtualFileMapping() = false

    private object MyValueExternalizer : DataExternalizer<List<LatexSimpleDefinition>> {
        override fun save(out: java.io.DataOutput, value: List<LatexSimpleDefinition>) {
            out.writeInt(value.size)
            for (def in value) {
                out.writeUTF(def.name)
                out.writeBoolean(def.isEnv)
                out.writeInt(def.arguments.size)
                for (arg in def.arguments) {
                    out.writeUTF(arg.name)
                    out.writeBoolean(arg.type == LArgumentType.OPTIONAL)
                }
                out.writeUTF(def.description)
            }
        }

        override fun read(input: java.io.DataInput): List<LatexSimpleDefinition> {
            val size = input.readInt()
            val definitions = List(size) {
                val name = input.readUTF()
                val isEnv = input.readBoolean()
                val argSize = input.readInt()
                val arguments: List<LArgument> = if (argSize <= 0) {
                    emptyList()
                }
                else {
                    List(argSize) {
                        val argName = input.readUTF()
                        val argType = if (input.readBoolean()) LArgumentType.OPTIONAL else LArgumentType.REQUIRED
                        LArgument(argName, argType)
                    }
                }
                val description = input.readUTF()
                LatexSimpleDefinition(name, isEnv, arguments, description)
            }
            return definitions
        }
    }
}
