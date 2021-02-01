package nl.hannahsten.texifyidea.psi

import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/*
* LatexEnvironment
*/
/**
 * Find the label of the environment. The method finds labels inside the environment content as well as labels
 * specified via an optional parameter
 *
 * @return the label name if any, null otherwise
 */
fun getLabel(element: LatexEnvironment): String? {
    val stub = element.stub
    if (stub != null) return stub.label
    return if (EnvironmentMagic.labelAsParameter.contains(element.environmentName)) {
        // See if we can find a label option
        val optionalParameters = getOptionalParameterMap(element.beginCommand.parameterList).toStringMap()
        optionalParameters.getOrDefault("label", null)
    }
    else {
        if (!EnvironmentMagic.labeled.containsKey(element.environmentName)) return null
        val content = element.environmentContent ?: return null

        // See if we can find a label command inside the environment
        val children = PsiTreeUtil.findChildrenOfType(content, LatexCommands::class.java)
        if (!children.isEmpty()) {
            // We cannot include user defined labeling commands, because to get them we need the index,
            // but this code is used to create the index (for environments)
            val labelCommands = CommandMagic.labelDefinitionsWithoutCustomCommands
            val labelCommand =
                children.firstOrNull { c: LatexCommands -> labelCommands.contains(c.name) } ?: return null
            val requiredParameters = labelCommand.requiredParameters
            if (requiredParameters.isEmpty()) return null
            val info = CommandManager.labelAliasesInfo.getOrDefault(labelCommand.name, null) ?: return null
            if (!info.labelsPreviousCommand) return null
            val parameterPosition = info.positions.firstOrNull() ?: 0
            return if (parameterPosition > requiredParameters.size - 1 || parameterPosition < 0) null else requiredParameters[parameterPosition]
        }
        null
    }
}

fun getEnvironmentName(element: LatexEnvironment): String? {
    val stub = element.stub
    if (stub != null) return stub.environmentName
    val parameters = element.beginCommand.parameterList
    if (parameters.isEmpty()) return ""
    val environmentNameParam = parameters[0]
    val requiredParam = environmentNameParam.requiredParam ?: return ""
    val contentList = requiredParam.requiredParamContentList
    if (contentList.isEmpty()) return ""
    val paramText = contentList[0].parameterText ?: return ""
    return paramText.text
}