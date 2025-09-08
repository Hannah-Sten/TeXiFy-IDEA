package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.jetbrains.rd.util.first
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
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
                val semantics = LatexDefinitionService.resolveEnv(this)
                val position = semantics?.arguments?.indexOfFirst { it.contextSignature.introduces(LatexContexts.LabelDefinition) }
                // Check for user defined environments
                if (position != null) {
                    this.beginCommand.parameterList.getOrNull(position)?.findFirstChildOfType(LatexParameterText::class)
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
                ?: run {
                    val semantics = LatexDefinitionService.resolveEnv(this)
                    val position = semantics?.arguments?.indexOfFirst { it.contextSignature.introduces(LatexContexts.LabelDefinition) }
                    position?.let {
                        this.beginCommand.parameterList.getOrNull(position)?.findFirstChildOfType(LatexParameterText::class)?.text
                    }
                } ?: ""
        }
    }
    return text
}
