package nl.hannahsten.texifyidea.util.parser

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
import nl.hannahsten.texifyidea.util.getOriginalCommandFromAlias
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.shrink
import java.util.regex.Pattern
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Check if the command includes other files, and if so return [InputFileReference] instances for them.
 * This method is called continuously, so it should be really fast.
 *
 * Use this instead of command.references.filterIsInstance<InputFileReference>(), to avoid resolving references of types that will not be needed.
 */
fun LatexCommands.getFileArgumentsReferences(): List<InputFileReference> {
    val inputFileReferences = mutableListOf<InputFileReference>()

    // There may be multiple commands with this name, just guess the first one
    val command = LatexCommand.lookup(this.name)?.firstOrNull()
        // If not found, maybe it is an alias (user defined command) of a known command
        ?: getOriginalCommandFromAlias(this.name ?: return emptyList(), project)
        ?: return emptyList()

    // Arguments from the LatexCommand (so the command as hardcoded in e.g. LatexRegularCommand)
    val requiredArguments = command.arguments.mapNotNull { it as? RequiredArgument }

    // Find file references within required parameters and across required parameters (think \referencing{reference1,reference2}{reference3} )
    for (i in requiredParameters().indices) {
        // Find the corresponding requiredArgument
        val requiredArgument = if (i < requiredArguments.size) requiredArguments[i] else continue

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
        else if (requiredParameter.firstChildOfType(LatexParameterText::class)?.children?.size == 1 && requiredParameter.firstChildOfType(LatexCommands::class) != null) {
            // Special case if there is a single command instead of text, we ignore it. Example: \subfix from the subfiles package, can be ignored for our purposes
            val newRequiredParameter = requiredParameter.firstChildOfType(LatexCommands::class)?.requiredParameters()?.firstOrNull() ?: requiredParameter
            listOf(newRequiredParameter.textRange.shrink(1).shiftLeft(this.textOffset))
        }
        else {
            listOf(requiredParameter.textRange.shrink(1).shiftLeft(this.textOffset))
        }

        for (subParamRange in subParamRanges) {
            inputFileReferences.add(InputFileReference(this, subParamRange, extensions, supportsAnyExtension = fileArgument.supportsAnyExtension))
        }
    }

    // Special case for the subfiles package: the (only) mandatory optional parameter should be a path to the main file
    // We reference it because we include the preamble of that file, so it is in the file set (partially)
    if (name == LatexGenericRegularCommand.DOCUMENTCLASS.cmd && SUBFILES.name in getRequiredParameters() && getOptionalParameterMap().isNotEmpty()) {
        val range = this.firstChildOfType(LatexParameter::class)?.textRangeInParent
        if (range != null) {
            inputFileReferences.add(InputFileReference(this, range.shrink(1), listOf("tex"), supportsAnyExtension = true))
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
fun <K : PsiElement, V : PsiElement> Map<K, V?>.toStringMap(): LinkedHashMap<String, String> {
    val parameterMap = LinkedHashMap<String, String>()
    this.forEach { (k, v) -> parameterMap[k.toString()] = v?.toString() ?: "" }
    return parameterMap
}

fun getOptionalParameterMapFromParameters(parameters: List<LatexParameter>): LinkedHashMap<LatexOptionalKeyValKey, LatexKeyValValue?> {
    val parameterMap = LinkedHashMap<LatexOptionalKeyValKey, LatexKeyValValue?>()
    // Parameters can be defined using multiple optional parameters, like \command[opt1][opt2]{req1}
    // But within a parameter, there can be different content like [name={value in group}]
    parameters.mapNotNull { it.optionalParam }
        // extract the content of each parameter element
        .flatMap { param ->
            param.optionalKeyValPairList
        }.forEach { pair ->
            parameterMap[pair.optionalKeyValKey] = pair.keyValValue
        }
    return parameterMap
}

fun getRequiredParameters(parameters: List<LatexParameter>): List<String> {
//    return parameters.mapNotNull { it.requiredParam }
//        .map { param ->
//            param.text.dropWhile { it == '{' }.dropLastWhile { it == '}' }.trim()
//        }
    // A minor improvement
    return parameters.mapNotNull {
        val param = it.requiredParam ?: return@mapNotNull null
        val text = param.text
        text.trim { c ->
            c == '{' || c == '}' || c.isWhitespace()
        }
    }
}

fun LatexCommands.extractUrlReferences(firstParam: LatexRequiredParam): Array<PsiReference> =
    extractSubParameterRanges(firstParam)
        .map { WebReference(this, it.shiftRight(firstParam.textOffset - textOffset)) }
        .toArray(emptyArray())
