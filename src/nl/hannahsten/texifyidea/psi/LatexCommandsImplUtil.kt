package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.nextLeaf
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.lang.commands.RequiredFileArgument
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.reference.LatexLabelReference
import nl.hannahsten.texifyidea.util.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.requiredParameters
import nl.hannahsten.texifyidea.util.shrink
import java.util.*
import java.util.regex.Pattern

/**
 * Get the references for this command.
 */
fun getReferences(element: LatexCommands): Array<PsiReference> {
    val firstParam = readFirstParam(element)

    val references = mutableListOf<PsiReference>()

    // If it is a reference to a label (used for autocompletion, do not confuse with reference resolving from LatexParameterText)
    if (element.project.getLabelReferenceCommands().contains(element.commandToken.text) && firstParam != null) {
        references.addAll(extractLabelReferences(element, firstParam))
    }

    // If it is a reference to a file
    references.addAll(element.getFileArgumentsReferences())

    if (CommandMagic.urls.contains(element.name) && firstParam != null) {
        references.addAll(element.extractUrlReferences(firstParam))
    }

    // Else, we assume the command itself is important instead of its parameters,
    // and the user is interested in the location of the command definition
    val definitionReference = CommandDefinitionReference(element)
    // Only create a reference if there is something to resolve to, otherwise autocompletion won't work
    if (definitionReference.multiResolve(false).isNotEmpty()) {
        references.add(definitionReference)
    }

    return references.toTypedArray()
}

/**
 * Check if the command includes other files, and if so return [InputFileReference] instances for them.
 *
 * Do not use this method directly, use command.references.filterIsInstance<InputFileReference>() instead.
 */
private fun LatexCommands.getFileArgumentsReferences(): List<InputFileReference> {
    val inputFileReferences = mutableListOf<InputFileReference>()

    // There may be multiple commands with this name, just guess the first one
    val command = LatexCommand.lookup(this.name)?.firstOrNull() ?: return emptyList()

    // Arguments from the LatexCommand (so the command as hardcoded in e.g. LatexRegularCommand)
    val requiredArguments = command.arguments.mapNotNull { it as? RequiredArgument }

    // Find file references within required parameters and across required parameters (think \referencing{reference1,reference2}{reference3} )
    for (i in requiredParameters().indices) {

        // Find the corresponding requiredArgument
        val requiredArgument = if (i < requiredArguments.size) requiredArguments[i] else requiredArguments.lastOrNull { it is RequiredFileArgument } ?: continue

        // Check if the actual argument is a file argument or continue with the next argument
        val fileArgument = requiredArgument as? RequiredFileArgument ?: continue
        val extensions = fileArgument.supportedExtensions

        // Find text range of parameters, relative to command startoffset
        val requiredParameter = requiredParameters()[i]
        val subParamRanges = if (requiredArgument.commaSeparatesArguments) {
            extractSubParameterRanges(requiredParameter).map {
                it.shiftRight(requiredParameter.textOffset - this.textOffset)
            }
        }
        else {
            listOf(requiredParameter.textRange.shrink(1).shiftLeft(this.textOffset))
        }

        for (subParamRange in subParamRanges) {
            inputFileReferences.add(InputFileReference(this, subParamRange, extensions, fileArgument.defaultExtension))
        }
    }

    return inputFileReferences
}

/**
 * Create label references from the command parameter given.
 */
fun extractLabelReferences(element: LatexCommands, firstParam: LatexRequiredParam): List<PsiReference> {
    val subParamRanges = extractSubParameterRanges(firstParam)
    val references: MutableList<PsiReference> = ArrayList()
    for (range in subParamRanges) {
        references.add(
            LatexLabelReference(
                element, range.shiftRight(firstParam.textOffset - element.textOffset)
            )
        )
    }
    return references
}

fun readFirstParam(element: LatexCommands): LatexRequiredParam? {
    return ApplicationManager.getApplication().runReadAction(
        Computable {
            val params: List<LatexRequiredParam> = element.requiredParameters()
            if (params.isEmpty()) null else params[0]
        }
    )
}

fun extractSubParameterRanges(param: LatexRequiredParam): List<TextRange> {
    return splitToRanges(stripGroup(param.text), PatternMagic.parameterSplit)
        .map { r: TextRange -> r.shiftRight(1) }
}

fun splitToRanges(text: String, pattern: Pattern): List<TextRange> {
    val parts = pattern.split(text)
    val ranges: MutableList<TextRange> = ArrayList()
    var currentOffset = 0
    for (part in parts) {
        val partStartOffset = text.indexOf(part, currentOffset)
        ranges.add(TextRange.from(partStartOffset, part.length))
        currentOffset = partStartOffset + part.length
    }
    return ranges
}

fun stripGroup(text: String): String {
    if (text.length < 2) return ""
    return text.substring(1, text.length - 1)
}

/**
 * Generates a map of parameter names and values (assuming they are in the form []name=]value) for all optional parameters, comma-separated and separate optional parameters are treated equally.
 * If a value does not have a name, the value will be the key in the hashmap mapping to the empty string.
 */
// Explicitly use a LinkedHashMap to preserve iteration order
fun Map<LatexKeyvalKey, LatexKeyvalValue?>.toStringMap(): LinkedHashMap<String, String> {
    val parameterMap = LinkedHashMap<String, String>()
    this.forEach { (k, v) -> parameterMap[k.toString()] = v?.toString() ?: "" }
    return parameterMap
}

fun getOptionalParameterMap(parameters: List<LatexParameter>): LinkedHashMap<LatexKeyvalKey, LatexKeyvalValue?> {

    val parameterMap = LinkedHashMap<LatexKeyvalKey, LatexKeyvalValue?>()
    // Parameters can be defined using multiple optional parameters, like \command[opt1][opt2]{req1}
    // But within a parameter, there can be different content like [name={value in group}]
    parameters.mapNotNull { it.optionalParam }
        // extract the content of each parameter element
        .flatMap { param ->
            param.keyvalPairList
        }.forEach { pair ->
            parameterMap[pair.keyvalKey] = pair.keyvalValue
        }
    return parameterMap
}

fun getRequiredParameters(parameters: List<LatexParameter>): List<String> {
    return parameters.mapNotNull { it.requiredParam }
        .map { param ->
            param.text.dropWhile { it == '{' }.dropLastWhile { it == '}' }.trim()
        }
}

fun LatexCommands.extractUrlReferences(firstParam: LatexRequiredParam): Array<PsiReference> =
    extractSubParameterRanges(firstParam)
        .map { WebReference(this, it.shiftRight(firstParam.textOffset - textOffset)) }
        .toArray(emptyArray())

/**
 * Checks if the command is followed by a label.
 */
fun hasLabel(element: LatexCommands): Boolean {
    if (CommandMagic.labelAsParameter.contains(element.name)) {
        return getOptionalParameterMap(element.parameterList).toStringMap().containsKey("label")
    }

    // Next leaf is a command token, parent is LatexCommands
    val labelMaybe = element.nextLeaf { it !is PsiWhiteSpace }?.parent as? LatexCommands ?: return false
    return CommandManager.labelAliasesInfo.getOrDefault(labelMaybe.commandToken.text, null)?.labelsPreviousCommand == true
}

fun setName(element: LatexCommands, newName: String): PsiElement {
    val newText = element.text.replace(element.name ?: return element, newName)
    val newElement = LatexPsiHelper(element.project).createFromText(newText).firstChild
    val oldNode = element.node
    val newNode = newElement.node
    if (oldNode == null) {
        element.parent?.node?.addChild(newNode)
    }
    else {
        element.parent?.node?.replaceChild(oldNode, newNode)
    }
    return element
}

fun keyValContentToString(element: LatexKeyvalKey): String =
    keyValContentToString(element.keyvalContentList)

fun keyValContentToString(list: List<LatexKeyvalContent>): String =
    list.joinToString(separator = "") {
        when {
            it.parameterText != null -> it.parameterText!!.text
            it.parameterGroup != null -> it.parameterGroup!!.parameterGroupText!!.text
            else -> ""
        }
    }

fun keyValContentToString(element: LatexKeyvalValue): String =
    keyValContentToString(element.keyvalContentList)