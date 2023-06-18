package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.hannahsten.texifyidea.index.LatexGlossaryEntryIndex
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.requiredParameters
import nl.hannahsten.texifyidea.util.parser.toStringMap

object LatexGlossariesCompletionProvider : CompletionProvider<CompletionParameters>() {

    private fun getOptionsMap(pairs: List<LatexStrictKeyValPair>): LinkedHashMap<String, String> {
        val map = HashMap<LatexKeyValKey, LatexKeyValValue?>()
        pairs.forEach { p -> map[p.keyValKey] = p.keyValValue }
        return pairs.associate<LatexKeyValuePair, LatexKeyValKey, LatexKeyValValue?> { Pair(it.keyValKey, it.keyValValue) }.toStringMap()
    }

    private fun prettyPrintParameter(param: LatexRequiredParam): String {
        return if (param.requiredParamContentList.isNotEmpty()) {
            param.requiredParamContentList.joinToString { c -> c.text }
        }
        else {
            param.strictKeyValPairList.joinToString { p -> p.text }
        }
    }

    private fun buildLookupElement(command: LatexCommands, label: String, short: String, long: String) =
        LookupElementBuilder.create(label, label)
            .withPsiElement(command)
            .withPresentableText(label)
            .withTailText(" ${long.replace("\n", " ")}", true)
            .withTypeText(short)
            .bold()
            .withInsertHandler(MoveToEndOfCommandHandler)

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val glossaryCommands = LatexGlossaryEntryIndex.getItemsInFileSet(parameters.originalFile)
        val lookupItems = glossaryCommands.mapNotNull { command: LatexCommands ->
            when (command.name) {
                NEWACRONYM.cmd, NEWABBREVIATION.cmd -> {
                    val params = command.getRequiredParameters()
                    val label = params.getOrNull(0) ?: return@mapNotNull null
                    val short = params.getOrNull(1) ?: return@mapNotNull null
                    val description = command.requiredParameters().getOrNull(2) ?: return@mapNotNull null
                    buildLookupElement(command, label, short, prettyPrintParameter(description))
                }
                NEWGLOSSARYENTRY.cmd -> {
                    val label = command.getRequiredParameters().getOrNull(0) ?: return@mapNotNull null
                    val options =
                        command.requiredParameters().getOrNull(1)?.strictKeyValPairList ?: return@mapNotNull null
                    val optionsMap = getOptionsMap(options)
                    val short = optionsMap.getOrDefault("name", "")
                    val description = optionsMap.getOrDefault("description", "")
                    buildLookupElement(command, label, short, description)
                }
                LONGNEWGLOSSARYENTRY.cmd -> {
                    val label = command.getRequiredParameters().getOrNull(0) ?: return@mapNotNull null
                    val options =
                        command.requiredParameters().getOrNull(1)?.strictKeyValPairList ?: return@mapNotNull null
                    val optionsMap = getOptionsMap(options)
                    val description = command.requiredParameters().getOrNull(2) ?: return@mapNotNull null
                    val short = optionsMap.getOrDefault("name", "")
                    buildLookupElement(command, label, short, prettyPrintParameter(description))
                }
                else -> {
                    null
                }
            }
        }
        result.addAllElements(lookupItems)
    }
}