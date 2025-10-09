package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped

/**
 * All labels in the fileset.
 * May contain duplicates.
 */
fun PsiFile.findLatexLabelingElementsInFileSet(): Sequence<PsiElement> {
    // TODO: Better implementation
    val fileset = LatexProjectStructure.getFilesetScopeFor(this)
    return NewLabelsIndex.getAllLabels(fileset).asSequence().flatMap {
        NewLabelsIndex.getByName(it, fileset)
    }
}

object LatexLabelUtil {

    fun isDefinedLabelOrBibtexLabel(label: String, project: Project, scope: GlobalSearchScope): Boolean {
        return NewLabelsIndex.existsByName(label, project, scope) || NewBibtexEntryIndex.existsByName(label, project, scope)
    }

    /**
     * Generate a unique label name based on the original label by appending or incrementing a number at the end.
     * If the original label does not exist, it is returned as is.
     *
     * @param originalLabel The original label name to base the new label on.
     * @param file The file in which to check for existing labels.
     * @return A unique label name that does not exist in the project scope.
     */
    fun getUniqueLabelName(originalLabel: String, file: PsiFile): String {
        val project = file.project
        val fileset = LatexProjectStructure.getFilesetScopeFor(file)
        var counter = 2
        var candidate = originalLabel
        while (isDefinedLabelOrBibtexLabel(candidate, project, fileset)) {
            candidate = originalLabel + counter
            counter++
        }
        return candidate
    }

    private fun getLabelFromOptionalParam(command: LatexCommandWithParams): LatexParameterText? {
        val optionalParameters = command.getOptionalParameterMap()
        for ((k, v) in optionalParameters) {
            if (k.toString() != "label") continue
            val contentList = v?.keyValContentList ?: continue
            for (c in contentList) {
                c.parameterText?.let { return it }
                c.parameterGroup?.parameterGroupText?.parameterTextList?.firstOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun extractLabelWithSemantics(element: LatexCommandWithParams, arguments: List<LArgument>): PsiElement? {
        LatexPsiUtil.processArgumentsWithSemantics(element, arguments) { param, arg ->
            if (arg != null && arg.contextSignature.introduces(LatexContexts.LabelDefinition)) {
                return param.findFirstChildTyped<LatexParameterText>()
            }
        }
        return null
    }

    private fun LatexCommands.firstRequiredParameterText(): LatexParameterText? {
        return firstRequiredParameter()?.findFirstChildTyped<LatexParameterText>()
    }

    fun extractLabelFromCommand(element: LatexCommands): PsiElement? {
        val nameWithSlash = element.nameWithSlash
        if (nameWithSlash in CommandMagic.labels) {
            return element.firstRequiredParameterText()
        }
        if (nameWithSlash in CommandMagic.labelAsParameter) {
            return getLabelFromOptionalParam(element)
        }
        val semantics = LatexDefinitionService.resolveCommand(element) ?: return element.firstRequiredParameterText()
        return extractLabelWithSemantics(element, semantics.arguments)
    }

    /**
     * Extracts the label from the environment if it is defined as a parameter.
     */
    fun extractLabelFromEnvironment(element: LatexEnvironment): PsiElement? {
        val name = element.getEnvironmentName()
        if (name in EnvironmentMagic.labelAsParameter) {
            return getLabelFromOptionalParam(element.beginCommand)
        }
        val semantics = LatexDefinitionService.resolveEnv(element) ?: return null
        return extractLabelWithSemantics(element.beginCommand, semantics.arguments)
    }

    fun extractLabelElementIn(element: PsiElement): PsiElement? {
        return when (element) {
            is BibtexEntry -> element.findFirstChildTyped<BibtexId>()
            is LatexCommands -> extractLabelFromCommand(element)
            is LatexEnvironment -> extractLabelFromEnvironment(element)
            is LatexParameterText -> element
            else -> null
        }
    }

    fun extractLabelTextIn(element: PsiElement): String? {
        return extractLabelElementIn(element)?.text
    }

    fun processAllLabelsInFileSet(file: PsiFile, processor: (String) -> Unit) {
        val project = file.project
        val fileset = LatexProjectStructure.getFilesetScopeFor(file)
        NewLabelsIndex.getAllLabels(fileset).forEach {
            processor(it)
        }
    }
}