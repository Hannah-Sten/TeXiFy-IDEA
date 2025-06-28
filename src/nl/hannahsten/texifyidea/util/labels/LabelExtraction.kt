package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.jetbrains.rd.util.first
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.lang.alias.EnvironmentManager
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.collectSubtreeOf
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import nl.hannahsten.texifyidea.util.parser.toStringMap

/**
 * Extracts the label element (so the element that should be resolved to) from the PsiElement given that the PsiElement represents a label.
 * Also see LatexEnvironmentUtil#getLabel()
 */
fun PsiElement.extractLabelElement(): PsiElement? {
    fun getLabelParameterText(command: LatexCommandWithParams): LatexParameterText {
        val optionalParameters = command.getOptionalParameterMap()
        val labelEntry = optionalParameters.filter { pair -> pair.key.toString() == "label" }.first()
        val contentList = labelEntry.value?.keyValContentList ?: emptyList()
        return contentList.firstOrNull { c -> c.parameterText != null }?.parameterText
            ?: contentList.first { c -> c.parameterGroup != null }.parameterGroup!!.parameterGroupText!!.parameterTextList.first()
    }

    return when (this) {
        is BibtexEntry -> findFirstChildOfType(BibtexId::class)
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                return getLabelParameterText(this)
            }
            else {
                // For now just take the first label name (may be multiple for user defined commands)
                val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
                val position = info?.positions?.firstOrNull() ?: 0

                // Skip optional parameters for now
                this.parameterList.mapNotNull { it.requiredParam }.getOrNull(position)
                    ?.findFirstChildOfType(LatexParameterText::class)
            }
        }
        is LatexEnvironment -> {
            if (EnvironmentMagic.labelAsParameter.contains(getEnvironmentName())) {
                getLabelParameterText(beginCommand)
            }
            else {
                // Check for user defined environments
                val labelPositions = EnvironmentManager.labelAliasesInfo.getOrDefault(getEnvironmentName(), null)
                if (labelPositions != null) {
                    this.beginCommand.parameterList.getOrNull(labelPositions.positions.first())?.findFirstChildOfType(LatexParameterText::class)
                }
                else {
                    null
                }
            }
        }
        else -> null
    }
}

/**
 * Extracts the label name from the PsiElement given that the PsiElement represents a label.
 * This function should be quick to execute, because it may be called for all labels in the project very often.
 *
 *
 * Use `PsiFile.findCommandInFileSet(LatexGenericRegularCommand.EXTERNALDOCUMENT)` to get the optional parameter
 *
 *
 * @param externalDocumentCommand the command that is used to include labels from other filesets, such as `\externaldocument`.
 *
 */
fun PsiElement.extractLabelName(externalDocumentCommand: LatexCommands? = null): String {
    when (this) {
        is BibtexEntry -> return this.getIdentifier()
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                return getOptionalParameterMap().toStringMap()["label"] ?: ""
            }
            // For now just take the first label name (which may be multiple for user defined commands)
            val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
            val position = info?.positions?.firstOrNull() ?: 0
            var prefix = info?.prefix ?: ""

            // Check if there is any prefix given by the xr package
            externalDocumentCommand?.parameterList?.firstNotNullOfOrNull { it.optionalParam }
                ?.text?.trim('[', ']')
                ?.let { prefix = it }
            // Skip optional parameters for now (also below and in
            return prefix + this.requiredParameterText(position)
        }

        is LatexEnvironment -> {
            return this.getLabel()
                // Check if it is a user defined alias of a labeled environment
                ?: EnvironmentManager.labelAliasesInfo.getOrDefault(getEnvironmentName(), null)?.let {
                    this.beginCommand.parameterList.getOrNull(it.positions.first())?.findFirstChildOfType(LatexParameterText::class)?.text
                }
                ?: ""
        }
    }
    return text
}

object LabelExtraction {

    /**
     * Extracts the label names from a [LatexRequiredParam] element.
     */
    fun extractLabelNames(parameter: LatexRequiredParam): List<String> {
        return parameter.collectSubtreeOf { if(it is LatexParameterText) it.text else null }
    }
}