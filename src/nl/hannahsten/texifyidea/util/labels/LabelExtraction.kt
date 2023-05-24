package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.jetbrains.rd.util.first
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.identifier
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.psi.firstChildOfType
import nl.hannahsten.texifyidea.util.psi.requiredParameter
import nl.hannahsten.texifyidea.util.psi.toStringMap

/**
 * Extracts the label element (so the element that should be resolved to) from the PsiElement given that the PsiElement represents a label.
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
        is BibtexEntry -> firstChildOfType(BibtexId::class)
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
                    ?.firstChildOfType(LatexParameterText::class)
            }
        }
        is LatexEnvironment -> {
            if (EnvironmentMagic.labelAsParameter.contains(environmentName)) {
                getLabelParameterText(beginCommand)
            }
            else {
                null
            }
        }
        else -> null
    }
}

/**
 * Extracts the label name from the PsiElement given that the PsiElement represents a label.
 * This function should be quick to execute, because it may be called for all labels in the project very often.
 *
 * @param referencingFileSetCommands All the commands in the fileset containing the element referencing [this], which can make a different for what the label name should be, as using the xr package labels from other filesets can be included with a prefix.
 */
fun PsiElement.extractLabelName(referencingFileSetCommands: Collection<LatexCommands>? = null): String {
    return when (this) {
        is BibtexEntry -> identifier() ?: ""
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                getOptionalParameterMap().toStringMap()["label"] ?: ""
            }
            else {
                // For now just take the first label name (which may be multiple for user defined commands)
                val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
                val position = info?.positions?.firstOrNull() ?: 0
                var prefix = info?.prefix ?: ""

                // Check if there is any prefix given by the xr package
                if (referencingFileSetCommands != null) {
                    referencingFileSetCommands
                        // Don't think there can be multiple, at least I wouldn't know what prefix it would use
                        .firstOrNull { it.name == LatexGenericRegularCommand.EXTERNALDOCUMENT.commandWithSlash }
                        ?.parameterList?.firstNotNullOfOrNull { it.optionalParam }
                        ?.text?.trim('[', ']')
                        ?.let { prefix = it }
                }

                // Skip optional parameters for now (also below and in
                prefix + this.requiredParameter(position)
            }
        }
        is LatexEnvironment -> this.label ?: ""
        else -> text
    }
}