package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandInsertHandler
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile

/**
 * Provides context-aware command completions.
 */
object ContextAwareCommandCompletionProvider : LatexContextAwareCompletionAdaptor() {

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        val isClassOrStyleFile = parameters.originalFile.let { it.isClassFile() || it.isStyleFile() }
        val lookupElements = mutableListOf<LookupElementBuilder>()
        for (sd in defBundle.sourcedDefinitions()) {
            val cmd = sd.entity as? LSemanticCommand ?: continue
            if (!isClassOrStyleFile && cmd.name.contains('@')) {
                // skip internal commands for regular files
                continue
            }
            if (!cmd.isApplicableIn(contexts)) continue // context check
            appendCommandLookupElements(cmd, sd, lookupElements, defBundle)
        }
        result.addAllElements(lookupElements)
    }

    private fun buildArgumentInformation(cmd: LSemanticCommand, args: List<LArgument>): String {
        return args.joinToString("")
    }

    private fun buildCommandDisplay(cmd: LSemanticCommand, defBundle: DefinitionBundle): String {
        if (cmd.display == null) {
            return cmd.nameWithSlash
        }
        return cmd.nameWithSlash + " " + cmd.display
    }

    private fun buildLookupString(cmd: LSemanticCommand, subArgs: List<LArgument>): String = buildString {
        append(cmd.nameWithSlash) // The command name with a slash, e.g. \newcommand/
        subArgs.joinTo(this, separator = "") {
            when (it.type) {
                LArgumentType.REQUIRED -> "{}"
                LArgumentType.OPTIONAL -> "[]"
            }
        }
    }

    private fun appendCommandLookupElements(cmd: LSemanticCommand, sourced: SourcedDefinition, result: MutableCollection<LookupElementBuilder>, defBundle: DefinitionBundle) {
        /*
        The presentation looks like:
        \newcommand{name}{definition}     (base)
        \alpha α                          amsmath.sty
        \mycommand[optional]{required}    main.tex
         */
        val typeText = buildCommandSourceStr(sourced) // type text is at the right
        val presentableText = buildCommandDisplay(cmd, defBundle)
        val applicableCtxText = buildApplicableContextStr(cmd)
        cmd.arguments.optionalPowerSet().forEachIndexed { index, subArgs ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Distinguishing between the same commands that come from different packages is already done by cmd
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val tailText = buildArgumentInformation(cmd, subArgs) + applicableCtxText
            val lookupString = buildLookupString(cmd, subArgs)
            val l = LookupElementBuilder.create(cmd, lookupString)
                .withPresentableText(presentableText)
                .bold()
                .withTailText(tailText, true)
                .withTypeText(typeText)
                .withInsertHandler(createInsertHandler(cmd, subArgs, defBundle))
                .withIcon(TexifyIcons.DOT_COMMAND)
            result.add(l)
        }
    }

    fun createInsertHandler(semantics: LSemanticCommand, subArgs: List<LArgument>, defBundle: DefinitionBundle): InsertHandler<LookupElement> {
        return LatexCommandInsertHandler(semantics, subArgs, defBundle)
    }

    /**
     * Generates all possible subsets of the argument list, ensuring that:
     * - All required argument  are included in every subset.
     * - Optional arguments are only included in prefix form for consecutive optional segments,
     *   preventing "skipping" (e.g., for `[opt1, opt2]`, valid combinations are `[], [ opt1 ], [opt1, opt2]`; not `[ opt2 ]`).
     * This handles multiple segments of consecutive optional arguments separated by required ones.
     */
    private fun List<LArgument>.optionalPowerSet(): List<List<LArgument>> {
        if (this.isEmpty()) return listOf(emptyList())
        if (this.all { it.isRequired }) return listOf(this)

        val result = mutableListOf<MutableList<LArgument>>(mutableListOf())
        val currentOptional = mutableListOf<LArgument>()

        for (arg in this) {
            if (arg.isRequired) {
                // process the current accumulated optional segment's prefix combinations
                addOptionalPrefixes(result, currentOptional, this.size)
                currentOptional.clear()
                result.forEach { it.add(arg) }
            }
            else {
                currentOptional.add(arg)
            }
        }
        addOptionalPrefixes(result, currentOptional, this.size)
        return result
    }

    /**
     * 辅助函数：为当前 result 中的每个子集，添加 optional 段的所有前缀组合（包括空组合），生成新的子集。
     * 这确保了连续 optional 参数不能跳跃（只允许前缀形式）。
     */
    private fun addOptionalPrefixes(
        result: MutableList<MutableList<LArgument>>,
        optionalSegment: List<LArgument>, totalSize: Int
    ) {
        if (optionalSegment.isEmpty()) return

        // generate all prefix combinations of the optional segment: [], [opt1], [opt1, opt2], ...
        val combinations = (1..optionalSegment.size).map {
            optionalSegment.take(it)
        }
        // build new result by appending each combination to each existing subset
        val appendedResult = mutableListOf<MutableList<LArgument>>()
        for (subset in result) {
            for (combo in combinations) {
                val newSubset = ArrayList<LArgument>(totalSize).apply {
                    addAll(subset)
                    addAll(combo)
                }
                appendedResult.add(newSubset)
            }
        }
        result.addAll(appendedResult)
    }
}