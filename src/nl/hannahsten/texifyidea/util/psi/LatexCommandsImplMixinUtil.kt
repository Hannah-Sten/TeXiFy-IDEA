package nl.hannahsten.texifyidea.util.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.SUBFILES
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.reference.LatexLabelReference
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.shrink
import java.util.regex.Pattern
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Check if the command includes other files, and if so return [InputFileReference] instances for them.
 *
 * Do not use this method directly, use command.references.filterIsInstance<InputFileReference>() instead.
 */
fun LatexCommands.getFileArgumentsReferences(): List<InputFileReference> {
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

    // Special case for the subfiles package: the (only) mandatory optional parameter should be a path to the main file
    // We reference it because we include the preamble of that file, so it is in the file set (partially)
    if (name == LatexGenericRegularCommand.DOCUMENTCLASS.cmd && SUBFILES.name in requiredParameters && optionalParameterMap.isNotEmpty()) {
        val range = this.firstChildOfType(LatexParameter::class)?.textRangeInParent
        if (range != null) {
            inputFileReferences.add(InputFileReference(this, range.shrink(1), setOf("tex"), "tex"))
        }
    }

    return inputFileReferences
}

/**
 * Create label references from the command parameter given, assuming it is a known command with label referencing parameters.
 */
fun extractLabelReferences(element: LatexCommands, requiredParameters: List<LatexRequiredParam>): List<PsiReference> {
    // Assume that any possible label reference is a required parameter
    val defaultParameter = requiredParameters.getOrNull(0) ?: return emptyList()

    // Find the command parameters which are a label reference
    return (
        LatexCommand.lookup(element.name)
            ?.firstOrNull()
            ?.arguments
            ?.withIndex()
            ?.filter { it.value.type == Argument.Type.LABEL }
            // Use the known parameter indices to match with the actual parameters
            ?.mapNotNull { requiredParameters.getOrNull(it.index) }
            ?.ifEmpty { listOf(defaultParameter) }
            ?: listOf(defaultParameter)
        )
        .flatMap { param ->
            extractSubParameterRanges(param).map { range ->
                LatexLabelReference(
                    element,
                    range.shiftRight(param.textOffset - element.textOffset)
                )
            }
        }
}

fun getRequiredParameters(element: LatexCommands): List<LatexRequiredParam> {
    return ApplicationManager.getApplication().runReadAction(
        Computable {
            element.requiredParameters()
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
 * Generates a map of parameter names and values (assuming they are in the form name=value) for all optional parameters, comma-separated and separate optional parameters are treated equally.
 * If a value does not have a name, the value will be the key in the hashmap mapping to the empty string.
 */
// Explicitly use a LinkedHashMap to preserve iteration order
fun Map<LatexKeyValKey, LatexKeyValValue?>.toStringMap(): LinkedHashMap<String, String> {
    val parameterMap = LinkedHashMap<String, String>()
    this.forEach { (k, v) -> parameterMap[k.toString()] = v?.toString() ?: "" }
    return parameterMap
}

fun getOptionalParameterMap(parameters: List<LatexParameter>): LinkedHashMap<LatexKeyValKey, LatexKeyValValue?> {
    val parameterMap = LinkedHashMap<LatexKeyValKey, LatexKeyValValue?>()
    // Parameters can be defined using multiple optional parameters, like \command[opt1][opt2]{req1}
    // But within a parameter, there can be different content like [name={value in group}]
    parameters.mapNotNull { it.optionalParam }
        // extract the content of each parameter element
        .flatMap { param ->
            param.keyValPairList
        }.forEach { pair ->
            parameterMap[pair.keyValKey] = pair.keyValValue
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

fun setName(element: LatexCommands, newName: String): PsiElement {
    var newText = element.text.replace(element.name ?: return element, newName)
    if (!newText.startsWith("\\"))
        newText = "\\" + newText
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

fun keyValKeyToString(element: LatexKeyValKey): String {
    // This is ugly, but element.children returns only composite children and other querying methods are recursive
    val result = ArrayList<PsiElement>()
    var psiChild = element.firstChild
    while (psiChild != null) {
        result.add(psiChild)
        psiChild = psiChild.nextSibling
    }
    return result.joinToString(separator = "") {
        when (it) {
            is LatexGroup -> it.content?.text ?: ""
            else -> it.text
        }
    }
}

fun keyValContentToString(list: List<LatexKeyValContent>): String =
    list.joinToString(separator = "") {
        when {
            it.parameterText != null -> it.parameterText!!.text
            it.parameterGroup != null -> it.parameterGroup!!.parameterGroupText!!.text
            else -> ""
        }
    }

fun keyValContentToString(element: LatexKeyValValue): String =
    keyValContentToString(element.keyValContentList)