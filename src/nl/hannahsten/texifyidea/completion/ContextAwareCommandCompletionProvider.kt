package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.handlers.LatexCommandInsertHandler
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.SourcedDefinition
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.TexifySettings.CompletionMode
import nl.hannahsten.texifyidea.util.files.isClassFile
import nl.hannahsten.texifyidea.util.files.isStyleFile

/**
 * Provides context-aware command completions.
 */
object ContextAwareCommandCompletionProvider : LatexContextAwareCompletionAdaptor() {

    override fun addContextAwareCompletions(parameters: CompletionParameters, contexts: LContextSet, defBundle: DefinitionBundle, result: CompletionResultSet) {
        val completionMode = TexifySettings.getState().completionMode
        val isClassOrStyleFile = parameters.originalFile.let { it.isClassFile() || it.isStyleFile() }
        addBundleCommands(
            parameters, result, defBundle, isClassOrStyleFile,
            checkCtx = completionMode == CompletionMode.SMART, contexts = contexts
        )
        if (completionMode == CompletionMode.ALL_PACKAGES) {
            addExternal(parameters) { addBundleCommands(parameters, result, it, isClassOrStyleFile, checkCtx = false) }
        }
    }

    private fun addBundleCommands(
        parameters: CompletionParameters, result: CompletionResultSet, defBundle: DefinitionBundle,
        isClassOrStyleFile: Boolean, checkCtx: Boolean = true, contexts: LContextSet = emptySet()
    ) {
        val lookupElements = mutableListOf<LookupElement>()
        for (sd in defBundle.sourcedDefinitions()) {
            val cmd = sd.entity as? LSemanticCommand ?: continue
            if (!isClassOrStyleFile && cmd.name.contains('@')) {
                // skip internal commands for regular files
                continue
            }
            if (checkCtx && !cmd.isApplicableIn(contexts)) continue // context check
            appendCommandLookupElements(cmd, sd, lookupElements, defBundle)
        }
        result.addAllElements(lookupElements)
    }

    private fun buildArgumentInformation(cmd: LSemanticCommand, args: List<LArgument>): String = args.joinToString("")

    private fun buildCommandDisplay(cmd: LSemanticCommand): String {
        if (cmd.display == null) {
            return cmd.commandWithSlash
        }
        return cmd.commandWithSlash + " " + cmd.display
    }

    private fun buildLookupString(cmd: LSemanticCommand, subArgs: List<LArgument>): String = buildString {
        append(cmd.commandWithSlash) // The command name with a slash, e.g. \newcommand/
        subArgs.joinTo(this, separator = "") {
            when (it.type) {
                LArgumentType.REQUIRED -> "{}"
                LArgumentType.OPTIONAL -> "[]"
            }
        }
    }

    private fun appendCommandLookupElements(cmd: LSemanticCommand, sourced: SourcedDefinition, result: MutableCollection<LookupElement>, defBundle: DefinitionBundle) {
        /*
        The presentation looks like:
        \newcommand{name}{definition}     (base)
        \alpha α                          amsmath.sty
        \mycommand[optional]{required}    main.tex
         */
        val typeText = buildDefinitionSourceStr(sourced) // type text is at the right
        val presentableText = buildCommandDisplay(cmd)
        val applicableCtxText = buildApplicableContextStr(cmd)
        cmd.arguments.optionalPowerSet().forEachIndexed { index, subArgs ->
            // Add spaces to the lookup text to distinguish different versions of commands within the same package (optional parameters).
            // Distinguishing between the same commands that come from different packages is already done by cmd
            // This 'extra' text will be automatically inserted by intellij and is removed by the LatexCommandArgumentInsertHandler after insertion.
            val tailText = buildArgumentInformation(cmd, subArgs) + applicableCtxText
            val lookupString = buildLookupString(cmd, subArgs)
            val element = SimpleWithDefLookupElement.create(
                sourced, lookupString,
                presentableText = presentableText, bold = true,
                typeText = typeText,
                tailText = tailText, tailTextGrayed = true,
                insertHandler = LatexCommandInsertHandler(cmd, subArgs),
                icon = TexifyIcons.DOT_COMMAND
            )
            result.add(element)
        }
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