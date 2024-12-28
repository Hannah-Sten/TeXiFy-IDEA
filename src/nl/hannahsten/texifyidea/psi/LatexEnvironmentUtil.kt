package nl.hannahsten.texifyidea.psi

import com.intellij.psi.util.PsiTreeUtil
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsSettingsManager
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.getOptionalParameterMapFromParameters
import nl.hannahsten.texifyidea.util.parser.toStringMap

/**
 * Find the label of the environment. The method finds labels inside the environment content as well as labels
 * specified via an optional parameter
 * Similar to LabelExtraction#extractLabelElement, but we cannot use the index here
 *
 * @return the label name if any, null otherwise
 */
fun LatexEnvironment.getLabel(): String? {
    val stub = this.stub
    if (stub != null) return stub.label
    return if (EnvironmentMagic.labelAsParameter.contains(this.getEnvironmentName())) {
        // See if we can find a label option
        val optionalParameters = getOptionalParameterMapFromParameters(this.beginCommand.parameterList).toStringMap()
        optionalParameters.getOrDefault("label", null)
    }
    else {
        // Not very clean. We don't really need the conventions here, but determine which environments *can* have a
        // label. However, if we didn't use the conventions, we would have to duplicate the information in
        // EnvironmentMagic
        val conventionSettings = TexifyConventionsSettingsManager.getInstance(this.project).getSettings()
        if (conventionSettings.getLabelConvention(
                this.getEnvironmentName(),
                LabelConventionType.ENVIRONMENT
            ) == null
        ) return null

        val content = this.environmentContent ?: return null

        // See if we can find a label command inside the environment
        val children = PsiTreeUtil.findChildrenOfType(content, LatexCommands::class.java)
        if (!children.isEmpty()) {
            // We cannot include user defined labeling commands, because to get them we need the index,
            // but this code is used to create the index (for environments)
            val labelCommands = CommandMagic.labelDefinitionsWithoutCustomCommands
            val labelCommand =
                children.firstOrNull { c: LatexCommands -> labelCommands.contains(c.name) } ?: return null
            val requiredParameters = labelCommand.getRequiredParameters()
            if (requiredParameters.isEmpty()) return null
            val info = CommandManager.labelAliasesInfo.getOrDefault(labelCommand.name, null) ?: return null
            if (!info.labelsPreviousCommand) return null
            val parameterPosition = info.positions.firstOrNull() ?: 0
            return if (parameterPosition > requiredParameters.size - 1 || parameterPosition < 0) null else requiredParameters[parameterPosition]
        }
        null
    }
}

fun LatexEnvironment.getEnvironmentName(): String {
    val stub = this.stub
    if (stub != null) return stub.environmentName
    val parameters = this.beginCommand.parameterList
    if (parameters.isEmpty()) return ""
    val environmentNameParam = parameters[0]
    val requiredParam = environmentNameParam.requiredParam ?: return ""
    val contentList = requiredParam.requiredParamContentList
    if (contentList.isEmpty()) return ""
    val paramText = contentList[0].parameterText ?: return ""
    return paramText.text
}