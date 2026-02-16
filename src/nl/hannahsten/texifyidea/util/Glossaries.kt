package nl.hannahsten.texifyidea.util

import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexParameterText
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

/**
 * Extract the label text from a glossary entry command
 */
fun extractGlossaryLabel(command: LatexCommands): String? {
    if (!CommandMagic.glossaryEntry.contains(command.name) &&
        !CommandMagic.glossaryReference.contains(command.name)
    ) return null
    return command.requiredParametersText()[0]
}

/**
 * Extract the label element from a glossary entry command
 */
fun extractGlossaryLabelElement(command: LatexCommands): PsiElement? {
    if (!CommandMagic.glossaryEntry.contains(command.name)) return null
    return command.requiredParameters()[0].findFirstChildOfType(LatexParameterText::class)
}

private val glossaryEntryCommands: Set<String> = setOf(CommandNames.NEW_GLOSSARY_ENTRY, CommandNames.LONG_NEW_GLOSSARY_ENTRY)
private val acronymEntryCommands: Set<String> = setOf(CommandNames.NEW_ACRONYM, CommandNames.NEW_ABBREVIATION)
private val acroEntryCommands: Set<String> = setOf(
    CommandNames.NEW_ACRO,
    CommandNames.ACRO,
    CommandNames.ACRO_DEF,
    CommandNames.DECLARE_ACRONYM
)

/**
 * Find the name, which is the text that will appear in the document, from the given glossary entry definition.
 */
fun extractGlossaryName(command: LatexCommands): String? {
    when(command.name) {
        in glossaryEntryCommands -> {
            val keyValueList = command.requiredParameterText(1) ?: return null
            return "name=\\{([^}]+)}".toRegex().find(keyValueList)?.groupValues?.getOrNull(1)
        }
        in acronymEntryCommands -> {
            return command.requiredParameterText(1)
        }
        in acroEntryCommands -> {
            // For acro commands, the name is the first parameter
            return command.requiredParameterText(0)
        }
    }
    return null
}
