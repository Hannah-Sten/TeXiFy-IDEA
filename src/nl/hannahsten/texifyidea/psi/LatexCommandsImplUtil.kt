package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.reference.LatexLabelReference
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.requiredParameters
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Create file references from the command parameter given.
 */
fun extractIncludes(element: LatexCommands, firstParam: LatexRequiredParam): List<PsiReference> {
    val subParamRanges = extractSubParameterRanges(firstParam)
    val references: MutableList<PsiReference> = ArrayList()
    for (range in subParamRanges) {
        references.add(InputFileReference(
                element, range.shiftRight(firstParam.textOffset - element.textOffset)
        ))
    }
    return references
}


/**
 * Create label references from the command parameter given.
 */
fun extractLabelReferences(element: LatexCommands, firstParam: LatexRequiredParam): List<PsiReference> {
    val subParamRanges = extractSubParameterRanges(firstParam)
    val references: MutableList<PsiReference> = ArrayList()
    for (range in subParamRanges) {
        references.add(LatexLabelReference(
                element, range.shiftRight(firstParam.textOffset - element.textOffset)
        ))
    }
    return references
}

fun readFirstParam(element: LatexCommands): LatexRequiredParam? {
    return ApplicationManager.getApplication().runReadAction(Computable {
        val params: List<LatexRequiredParam> = element.requiredParameters()
        if (params.isEmpty()) null else params[0]
    })
}

fun extractSubParameterRanges(param: LatexRequiredParam): List<TextRange> {
    return splitToRanges(stripGroup(param.text), Magic.Pattern.parameterSplit).stream()
            .map { r: TextRange -> r.shiftRight(1) }.collect(Collectors.toList())
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
    return text.substring(1, text.length - 1)
}


/**
 * Generates a map of parameter names and values for all optional parameters
 */
// Explicitly use a LinkedHashMap to preserve iteration order

fun getOptionalParameters(parameters: List<LatexParameter>): LinkedHashMap<String, String> {
    val parameterMap = LinkedHashMap<String, String>()
    val parameterString = parameters.mapNotNull { it.optionalParam }
            // extract the content of each parameter element
            .flatMap { param ->
                param.openGroup.contentList.map { it.noMathContent }
            }
            .mapNotNull { content: LatexNoMathContent ->
                // the content is either simple text
                val text = content.normalText
                if (text != null) return@mapNotNull text.text
                // or a group like in param={some value}
                if (content.group == null) return@mapNotNull null
                content.group!!.contentList.joinToString { it.text }
            }
            .joinToString(separator = "")

    if (parameterString.trim { it <= ' ' }.isNotEmpty()) {
        for (parameter in parameterString.split(",")) {
            val parts = parameter.split("=".toRegex()).toTypedArray()
            parameterMap[parts[0].trim()] = if (parts.size > 1) parts[1].trim() else ""
        }
    }
    return parameterMap
}

fun getRequiredParameters(parameters: List<LatexParameter>): List<String>? {
    return parameters.mapNotNull { it.requiredParam?.group }
            .map {
                it.contentList.map { c: LatexContent ->
                    val content = c.noMathContent
                    if (content.commands != null && content.normalText == null) {
                        content.commands!!.commandToken.text
                    }
                    else if (content.normalText != null) {
                        content.normalText!!.text
                    }
                    else {
                        null
                    }
                }.joinToString(separator = "")
            }
}


fun LatexCommands.extractUrlReferences(firstParam: LatexRequiredParam): Array<PsiReference> =
        extractSubParameterRanges(firstParam)
                .map { WebReference(this, it.shiftRight(firstParam.textOffset - textOffset)) }
                .toArray(emptyArray())