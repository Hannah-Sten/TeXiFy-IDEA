package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.RequiredArgument
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.reference.LatexLabelReference
import nl.hannahsten.texifyidea.settings.TexifySettings.Companion.getInstance
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.requiredParameters
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.regex.Pattern

/**
 * Get the references for this command.
 * For example for a \ref{label1,label2} command, then label1 and label2 are the references.
 */
fun getReferences(element: LatexCommands): Array<PsiReference> {
    val firstParam = readFirstParam(element)

    // If it is a reference to a label
    if (Magic.Command.reference.contains(element.commandToken.text) && firstParam != null) {
        val references = extractLabelReferences(element, firstParam)
        return references.toTypedArray()
    }

    // If it is a reference to a file
    val references: List<PsiReference> = element.getFileArgumentsReferences()
    if (firstParam != null && references.isNotEmpty()) {
        return references.toTypedArray()
    }

    if (Magic.Command.urls.contains(element.name) && firstParam != null) {
        return element.extractUrlReferences(firstParam)
    }

    // Else, we assume the command itself is important instead of its parameters,
    // and the user is interested in the location of the command definition
    val reference = CommandDefinitionReference(element)
    // Only create a reference if there is something to resolve to, otherwise autocompletion won't work
    return if (reference.multiResolve(false).isEmpty()) {
        emptyArray()
    }
    else {
        arrayOf(reference)
    }
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
        val subParamRanges = extractSubParameterRanges(requiredParameters()[i])

        for (subParamRange in subParamRanges) {

            // Find the corresponding requiredArgument
            val requiredArgument = if (i < requiredArguments.size) requiredArguments[i] else requiredArguments.lastOrNull { it is RequiredFileArgument } ?: continue

            // Check if the actual argument is a file argument or continue with the next argument
            val fileArgument = requiredArgument as? RequiredFileArgument ?: continue
            val extensions = fileArgument.supportedExtensions

            val range = subParamRange.shiftRight(requiredParameters()[i].textOffset - this.textOffset)

            inputFileReferences.add(InputFileReference(this, range, extensions, fileArgument.defaultExtension))
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
    return splitToRanges(stripGroup(param.text), Magic.Pattern.parameterSplit)
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
 * Generates a map of parameter names and values for all optional parameters
 */
// Explicitly use a LinkedHashMap to preserve iteration order
fun getOptionalParameters(parameters: List<LatexParameter>): LinkedHashMap<String, String> {
    val parameterMap = LinkedHashMap<String, String>()
    val parameterString = parameters.mapNotNull { it.optionalParam }
            // extract the content of each parameter element
            .flatMap { param ->
                param.optionalParamContentList
            }
            .mapNotNull { content: LatexOptionalParamContent ->
                // the content is either simple text
                val text = content.parameterText
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
    return parameters.mapNotNull { it.requiredParam }
            .map {
                it.requiredParamContentList.map { content: LatexRequiredParamContent ->
                    if (content.commands != null && content.parameterText == null) {
                        content.commands!!.commandToken.text
                    }
                    else if (content.parameterText != null) {
                        content.parameterText!!.text
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


/**
 * Checks if the command is followed by a label.
 */
fun hasLabel(element: LatexCommands): Boolean {
    val grandparent = element.parent.parent
    val sibling = LatexPsiUtil.getNextSiblingIgnoreWhitespace(grandparent) ?: return false
    val children = PsiTreeUtil.findChildrenOfType(sibling, LatexCommands::class.java)
    if (children.isEmpty()) {
        return false
    }
    val labelMaybe = children.iterator().next()
    return getInstance().labelPreviousCommands.containsKey(labelMaybe.commandToken.text)
}