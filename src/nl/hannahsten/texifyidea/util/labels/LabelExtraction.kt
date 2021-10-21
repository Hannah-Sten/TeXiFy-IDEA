package nl.hannahsten.texifyidea.util.labels

import com.intellij.psi.PsiElement
import com.jetbrains.rd.util.first
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.identifier
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.requiredParameter


/**
 * Extracts the label element (so the element that should be resolved to) from the PsiElement given that the PsiElement represents a label.
 */
fun PsiElement.extractLabelElement(): PsiElement? {
    fun getLabelParameterText(command: LatexCommandWithParams): LatexParameterText {
        val optionalParameters = command.optionalParameterMap
        val labelEntry = optionalParameters.filter { pair -> pair.key.toString() == "label" }.first()
        val contentList = labelEntry.value.keyvalContentList
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
 */
fun PsiElement.extractLabelName(): String {
    return when (this) {
        is BibtexEntry -> identifier() ?: ""
        is LatexCommands -> {
            if (CommandMagic.labelAsParameter.contains(name)) {
                optionalParameterMap.toStringMap()["label"]!!
            }
            else {
                // For now just take the first label name (may be multiple for user defined commands)
                val info = CommandManager.labelAliasesInfo.getOrDefault(name, null)
                val position = info?.positions?.firstOrNull() ?: 0
                val prefix = info?.prefix ?: ""
                // Skip optional parameters for now (also below and in
                prefix + this.requiredParameter(position)
            }
        }
        is LatexEnvironment -> this.label ?: ""
        else -> text
    }
}