package nl.hannahsten.texifyidea.index.file

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import nl.hannahsten.texifyidea.file.LatexSourceFileType
import nl.hannahsten.texifyidea.index.LatexFileBasedIndexKeys
import nl.hannahsten.texifyidea.index.file.LatexSimpleDefinition.Type.COMMAND
import nl.hannahsten.texifyidea.index.file.LatexSimpleDefinition.Type.ENVIRONMENT
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.util.Log
import java.io.DataInput
import java.io.DataOutput

/**
 * A simple definition of a LaTeX command or environment.
 */
data class LatexSimpleDefinition(
    /**
     * Without the leading backslash.
     */
    var name: String,
    var type: Type,
    var arguments: List<LArgument> = emptyList(),
    var description: String = ""
) {

    val isEnv: Boolean
        get() = type == ENVIRONMENT

    override fun toString(): String {
        val displayName = if (isEnv) name else "\\$name"
        return "Def('$displayName', ${arguments.joinToString("")}, desc='${description.take(20)}')"
    }

    enum class Type {
        COMMAND,
        ENVIRONMENT
    }
}

/**
 * The indexer for `.dtx` files, extracting command and environment definitions.
 */
object LatexDtxDefinitionDataIndexer : DataIndexer<String, List<LatexSimpleDefinition>, FileContent> {

    //    private val regexRrovidesPackage = "(?<=\\\\ProvidesPackage\\{)(?<name>[a-zA-Z0-9_]+)(?=\\})".toRegex()
    private val regexProvidesPackage = """
        \\ProvidesPackage\{(?<name>[a-zA-Z0-9_-]+)}
    """.trimIndent().toRegex()
    private val regexProvidesClass = """
        \\ProvidesClass\{(?<name>[a-zA-Z0-9_-]+)}
    """.trimIndent().toRegex()

    private val regexBeginMacro = """
        \\begin\{(macro|environment)}\{(?<name>\\?[a-zA-Z@]+\*?)}
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

    /**
     * Maximum number of characters to extract from documentation blocks.
     */
    const val MAX_CHARS = 100000

    private fun String.truncate(maxLength: Int, fileName: String): String {
        return if (this.length > maxLength) {
            Log.warn(("Truncating in $fileName: ${this.take(50)}"))
            this.take(maxLength)
        }
        else {
            this
        }
    }

    private fun parseDtxLines(fileName: String, lines: Sequence<String>): DtxDoc {
        val docLines = mutableListOf<String>()
        val macroStarts = mutableListOf<DefStart>()
        var inMacroCode = false
        var libName: LatexLib? = null
        val definitions = mutableListOf<LatexSimpleDefinition>()

        // extract code blocks between \begin{macrocode} and \end{macrocode}
        for (line in lines) {
            var text = line
            val inComment = text.startsWith('%')
            if (inComment) {
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
                if (macroStarts.isNotEmpty() && (text.contains("\\end{macro}") || text.contains("\\end{environment}"))) {
                    val defStart = macroStarts.removeLast()
                    val docString = docLines.subList(defStart.docLineStart, docLines.size).joinToString("\n").truncate(MAX_CHARS, fileName)
                    val type = if (text.contains("\\end{environment}")) ENVIRONMENT else COMMAND
                    var name = defStart.name
                    if (type == COMMAND) name = name.removePrefix("\\")
                    definitions.add(LatexSimpleDefinition(name, type, description = docString))
                    continue
                }
                val match = regexBeginMacro.find(text)
                if (match != null) {
                    val name = match.groups["name"]!!.value
                    macroStarts.add(DefStart(name, docLines.size))
                    continue
                }
                docLines.add(text)
            }
            if (!inComment && libName == null) {
                regexProvidesPackage.find(text)?.let { match ->
                    libName = LatexLib.Package(match.groups["name"]!!.value)
                    // If the match is not null, it means we found a \ProvidesPackage command
                    // We can extract the package name from the match
                } ?: regexProvidesClass.find(text)?.let { match ->
                    libName = LatexLib.Class(match.groups["name"]!!.value)
                }
            }
        }
        if(libName == null) {
            // we just assume it's a package with the same name as the file
            libName = LatexLib.Package(fileName.substringBeforeLast('.'))
        }
        return DtxDoc(libName, docLines, definitions)
    }

    private val regexSingleParam = """
        \\(?<type>(marg|oarg|parg|meta))\{(?<name>[^}]+)}
    """.trimIndent().toRegex()

    private val regexDescribeMacro = """
        \\DescribeMacro\s*\{?(?<name>\\[a-zA-Z@]+\*?)}?\s*(\\star|\*)?(?<params>([\s\\]*\\(marg|oarg|parg|meta)\{[^}]+})*)?
    """.trimIndent().toRegex()
    private val regexDescribeEnv = """
        \\DescribeEnv\s*\{(?<name>[a-zA-Z@]+\*?)}\s*(\\star|\*)?(?<params>([\s\\]*\\(marg|oarg|parg|meta)\{[^}]+})*)?
    """.trimIndent().toRegex()

    private fun extractDescribeBlocks(lines: List<String>, fileName: String): List<String> {
        var describing = false
        var describeStart = 0
        val describeBlocks = mutableListOf<String>()
        for (i in lines.indices) {
            val line = lines[i]
            if (describing) {
                if (line.isBlank()) {
                    describing = false
                    val text = lines.subList(describeStart, i).joinToString("\n").truncate(MAX_CHARS, fileName)
                    describeBlocks.add(text)
                }
                continue
            }
            if (line.contains("\\DescribeMacro") || line.contains("\\DescribeEnv")) {
                describing = true
                describeStart = i
            }
        }
        if (describing) {
            val text = lines.subList(describeStart, lines.size).joinToString("\n").truncate(MAX_CHARS, fileName)
            describeBlocks.add(text)
        }
        return describeBlocks
    }

    fun parseArguments(params: String?): List<LArgument> {
        if (params == null) return emptyList()
        var args = emptyList<LArgument>()
        /*
        We assume that the parameters are placed in one line
        We just select the line with the most arguments.
            \DescribeMacro\colorbox\marg{color}\marg{text}\\
              \oarg{model-list}\marg{spec-list}\marg{text}\\
         */
        params.split("\\\\").forEach { line ->
            val candidate = regexSingleParam.findAll(line).mapTo(mutableListOf()) { match ->
                val typeText = match.groups["type"]!!.value
                val name = match.groups["name"]!!.value
                val type = if (typeText == "oarg") LArgumentType.OPTIONAL else LArgumentType.REQUIRED
                LArgument(name, type)
            }
            if (candidate.size > args.size) {
                args = candidate
            }
        }
        return args
    }

    fun extractDefinitionsFromBlocks(describeBlocks: List<String>): List<LatexSimpleDefinition> {
        val result = mutableListOf<LatexSimpleDefinition>()
        for (block in describeBlocks) {
            val curSize = result.size
            var pos = 0
            regexDescribeMacro.findAll(block).forEach { match ->
                val name = match.groups["name"]?.value?.removePrefix("\\") ?: return@forEach
                val args = parseArguments(match.groups["params"]?.value)
                result.add(LatexSimpleDefinition(name, COMMAND, args))
                pos = match.range.last
            }
            regexDescribeEnv.findAll(block).forEach { match ->
                val name = match.groups["name"]?.value ?: return@forEach
                val args = parseArguments(match.groups["params"]?.value)
                result.add(LatexSimpleDefinition(name, ENVIRONMENT, args))
                pos = match.range.last
            }
            pos = block.indexOf('\n', pos) + 1 // move to the next line after the match
            val description = block.substring(pos).trim().take(MAX_CHARS)
            for (i in curSize until result.size) {
                result[i].description = description
            }
        }
        return result
    }

    private val regexOneLineArgs = """
        \\(?<type>(marg|oarg|parg|meta))\{(?<name>[^}]+)}
    """.trimIndent().toRegex()

    private val parameterSuffix = """
        \s*(\\star|\*)?(?<params>(\s*\\(marg|oarg|parg|meta)\{[^}]+})+)
    """.trimIndent()

    private val regexOneLineCmdDefinitions = listOf(
        """
            \|\\(?<name>[a-zA-Z@]+\*?)\|$parameterSuffix
        """.trimIndent().toRegex(),
        """
            \\cs\{(?<name>[a-zA-Z@]+\*?)}$parameterSuffix
        """.trimIndent().toRegex(),
    )

    private fun parseDocDefinitions(lines: List<String>, lib: LatexLib, fileName: String): Collection<LatexSimpleDefinition> {
        val describeBlocks = extractDescribeBlocks(lines, fileName)
        val map = mutableMapOf<String, LatexSimpleDefinition>()
        extractDefinitionsFromBlocks(describeBlocks).forEach {
            map[it.name] = it
        }
        // parse one-line definitions
        /*
        \cs{phy@define@key} \marg{module} \marg{key} \oarg{default value} \marg{code}
        |\bezier|\marg{N}\parg{AX,AY}\parg{BX,BY}\parg{CX,CY}\\
         */
        for (line in lines) {
            for (regex in regexOneLineCmdDefinitions) {
                val match = regex.find(line) ?: continue
                val name = match.groups["name"]?.value ?: continue
                val args = parseArguments(match.groups["params"]?.value)
                if (args.isEmpty()) continue
                val original = map[name]
                if (original == null) {
                    map[name] = LatexSimpleDefinition(name, COMMAND, args)
                }
                else {
                    if (original.arguments.size < args.size) {
                        // If the new definition has more arguments, replace the old one
                        original.arguments = args
                    }
                }
            }
        }

        return map.values
    }

    override fun map(inputData: FileContent): Map<String, List<LatexSimpleDefinition>> {
        val lines = inputData.contentAsText.lineSequence()
        val dtxInfo = parseDtxLines(inputData.fileName, lines)
        val definitions = parseDocDefinitions(dtxInfo.docLines, dtxInfo.lib, inputData.fileName).toMutableList()
        val names = mutableSetOf<String>().apply { definitions.forEach { add(it.name) } }
        for (def in dtxInfo.definitions) {
            if (def.name in names) continue
            definitions.add(def)
        }
        val filename = dtxInfo.lib.toFileName() ?: return emptyMap()
        return mapOf(filename to definitions)
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

    override fun getVersion() = 8

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return myInputFilter
    }

    override fun dependsOnFileContent() = true

    override fun traceKeyHashToVirtualFileMapping() = false

    private object MyValueExternalizer : DataExternalizer<List<LatexSimpleDefinition>> {
        override fun save(out: DataOutput, value: List<LatexSimpleDefinition>) {
            val buffer = IOUtil.allocReadWriteUTFBuffer()
            out.writeInt(value.size)
            for (def in value) {
                out.writeUTF(def.name)
                out.writeBoolean(def.isEnv)
                out.writeInt(def.arguments.size)
                for (arg in def.arguments) {
                    out.writeUTF(arg.name)
                    out.writeBoolean(arg.type == LArgumentType.OPTIONAL)
                }
                IOUtil.writeUTFFast(buffer, out, def.description)
            }
        }

        override fun read(input: DataInput): List<LatexSimpleDefinition> {
            val buffer = IOUtil.allocReadWriteUTFBuffer()
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
                val description = IOUtil.readUTFFast(buffer, input)
                val type = if (isEnv) ENVIRONMENT else COMMAND
                LatexSimpleDefinition(name, type, arguments, description)
            }
            return definitions
        }
    }
}
