package nl.hannahsten.texifyidea.util.parser

import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import java.util.regex.Pattern

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

fun LatexCommands.extractUrlReferences(firstParam: LatexRequiredParam): Array<PsiReference> =
    extractSubParameterRanges(firstParam)
        .map { WebReference(this, it.shiftRight(firstParam.textOffset - textOffset)) }
        .toArray(emptyArray())
