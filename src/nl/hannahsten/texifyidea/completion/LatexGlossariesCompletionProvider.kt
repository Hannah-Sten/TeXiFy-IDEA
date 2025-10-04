package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import nl.hannahsten.texifyidea.completion.handlers.MoveToEndOfCommandHandler
import nl.hannahsten.texifyidea.index.NewSpecialCommandsIndex
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.psi.LatexStrictKeyValPair
import nl.hannahsten.texifyidea.util.parser.toStringMap

object LatexGlossariesCompletionProvider : LatexContextAgnosticCompletionProvider() {

    private fun getOptionsMap(pairs: List<LatexStrictKeyValPair>): LinkedHashMap<String, String> {
        return pairs.associate { pair -> Pair(pair.keyValKey, pair.keyValValue) }.toStringMap()
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
        parameters: CompletionParameters, result: CompletionResultSet
    ) {
        val glossaryCommands = NewSpecialCommandsIndex.getAllGlossaryEntries(parameters.originalFile)
        val lookupItems = mutableListOf<LookupElement>()

        for (command in glossaryCommands) {
            val element = when (command.name) {
                CommandNames.NEW_ACRONYM, CommandNames.NEW_ABBREVIATION -> {
                    val params = command.requiredParametersText()
                    val label = params.getOrNull(0) ?: continue
                    val short = params.getOrNull(1) ?: continue
                    val description = command.requiredParameters().getOrNull(2) ?: continue
                    buildLookupElement(command, label, short, prettyPrintParameter(description))
                }

                CommandNames.NEW_GLOSSARY_ENTRY -> {
                    val label = command.requiredParametersText().getOrNull(0) ?: continue
                    val options =
                        command.requiredParameters().getOrNull(1)?.strictKeyValPairList ?: continue
                    val optionsMap = getOptionsMap(options)
                    val short = optionsMap.getOrDefault("name", "")
                    val description = optionsMap.getOrDefault("description", "")
                    buildLookupElement(command, label, short, description)
                }

                CommandNames.LONG_NEW_GLOSSARY_ENTRY -> {
                    val label = command.requiredParametersText().getOrNull(0) ?: continue
                    val options =
                        command.requiredParameters().getOrNull(1)?.strictKeyValPairList ?: continue
                    val optionsMap = getOptionsMap(options)
                    val description = command.requiredParameters().getOrNull(2) ?: continue
                    val short = optionsMap.getOrDefault("name", "")
                    buildLookupElement(command, label, short, prettyPrintParameter(description))
                }

                CommandNames.ACRO, CommandNames.NEW_ACRO, CommandNames.ACRO_DEF -> {
                    val acronym = command.requiredParametersText().getOrNull(0) ?: continue
                    val fullName = command.requiredParametersText().getOrNull(1) ?: continue
                    buildLookupElement(command, acronym, "", fullName)
                }

                else -> continue
            }
            lookupItems.add(element)
        }
        result.addAllElements(lookupItems)
    }
}