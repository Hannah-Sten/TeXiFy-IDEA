package nl.hannahsten.texifyidea.util.labels

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.FilesetData
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.index.NewBibtexEntryIndex
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.index.NewLatexEnvironmentIndex
import nl.hannahsten.texifyidea.index.restrictedByFileTypes
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
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

    fun extractLabelFromCommand(element: LatexCommands, customDef: Boolean = false): PsiElement? {
        val nameWithSlash = element.nameWithSlash
        if (nameWithSlash in CommandMagic.labels) {
            return element.firstRequiredParameterText()
        }
        if (nameWithSlash in CommandMagic.labelAsParameter) {
            return getLabelFromOptionalParam(element)
        }
        if (!customDef) return null
        val semantics = LatexDefinitionService.resolveCommand(element) ?: return element.firstRequiredParameterText()
        return extractLabelWithSemantics(element, semantics.arguments)
    }

    /**
     * Extracts the label from the environment if it is defined as a parameter.
     */
    fun extractLabelFromEnvironment(element: LatexEnvironment, customDef: Boolean = false): PsiElement? {
        val name = element.getEnvironmentName()
        if (name in EnvironmentMagic.labelAsParameter) {
            return getLabelFromOptionalParam(element.beginCommand)
        }
        if (!customDef) return null
        val semantics = LatexDefinitionService.resolveEnv(element) ?: return null
        return extractLabelWithSemantics(element.beginCommand, semantics.arguments)
    }

    fun extractLabelParamIn(element: PsiElement, withCustomized: Boolean = false): PsiElement? {
        return when (element) {
            is BibtexEntry -> element.findFirstChildTyped<BibtexId>()
            is LatexCommands -> extractLabelFromCommand(element, withCustomized)
            is LatexEnvironment -> extractLabelFromEnvironment(element, withCustomized)
            is LatexParameterText -> element
            else -> null
        }
    }

    fun extractLabelTextIn(element: PsiElement, customDef: Boolean = false): String? {
        return extractLabelParamIn(element, customDef)?.text
    }

    fun interface LabelProcessor {
        /**
         * Processes a label and the element it was found in.
         *
         * @param container The element containing the label, e.g. a \label command or a \bibitem command.
         * @param label The label text, e.g. "sec:introduction", including any prefix from external documents.
         * @param param The element containing the label text, e.g. the parameter text of a \label command or the id of a \bibitem command.
         */
        fun process(label: String, container: PsiElement, param: PsiElement)
    }

    private fun processCustomCommand(
        semantics: LSemanticCommand, prefix: String = "",
        project: Project, scope: GlobalSearchScope, processor: LabelProcessor
    ) {
        val nameWithSlash = semantics.nameWithSlash
        if (nameWithSlash in CommandMagic.labels) return
        if (nameWithSlash in CommandMagic.labelAsParameter) return
        NewCommandsIndex.forEachByName(nameWithSlash, project, scope) { command ->
            val paramText = extractLabelWithSemantics(command, semantics.arguments) ?: return@forEachByName
            val label = prefix + paramText.text
            processor.process(label, command, paramText)
        }
    }

    private fun processCustomEnv(
        semantics: LSemanticEnv, prefix: String = "",
        project: Project, scope: GlobalSearchScope, processor: LabelProcessor
    ) {
        if (semantics.name in EnvironmentMagic.labelAsParameter) return
        NewLatexEnvironmentIndex.forEachByName(semantics.name, project, scope) { env ->
            val paramText = extractLabelWithSemantics(env.beginCommand, semantics.arguments) ?: return@forEachByName
            val label = prefix + paramText.text
            processor.process(label, env, paramText)
        }
    }

    private fun processInFileset(
        project: Project, file: VirtualFile, prefix: String = "", withCustomCmd: Boolean, processor: LabelProcessor
    ) {
        val filesetData = LatexProjectStructure.getFilesetDataFor(file, project)
        val scope = filesetData?.filesetScope?.restrictedByFileTypes(LatexFileType) ?: GlobalSearchScope.fileScope(project, file)
        NewLabelsIndex.forEachKey(project, scope) { extractedLabel ->
            if (extractedLabel.isBlank()) return@forEachKey
            NewLabelsIndex.forEachByName(extractedLabel, project, scope) { labelingElement ->
                val label = prefix + extractedLabel
                val param = extractLabelParamIn(labelingElement, withCustomized = false) ?: return@forEachByName
                processor.process(label, labelingElement, param)
            }
        }

        // Custom commands
        if (!withCustomCmd || filesetData == null) return
        processCustomizedInFileset(project, filesetData, prefix, processor)
    }

    private fun processCustomizedInFileset(
        project: Project, filesetData: FilesetData, prefix: String, processor: LabelProcessor
    ) {
        val defService = LatexDefinitionService.getInstance(project)
        for (fileset in filesetData.filesets) {
            val defBundle = defService.getDefBundleForFileset(fileset)
            val entities = defBundle.findByRelatedContext(LatexContexts.LabelDefinition)
            val scope = fileset.texFileScope(project)
            for (entity in entities) {
                if (entity.dependency != LatexLib.CUSTOM) continue
                when (entity) {
                    is LSemanticCommand -> processCustomCommand(entity, prefix, project, scope, processor)
                    is LSemanticEnv -> processCustomEnv(entity, prefix, project, scope, processor)
                }
            }
        }
    }

    /**
     * Processes all labels in the fileset of the given file, including external documents and custom commands or environments if specified.
     */
    fun processAllLabelsInFileSet(
        file: PsiFile, withExternal: Boolean = true, withCustomized: Boolean = true, processor: LabelProcessor
    ) {
        val project = file.project
        val virtualFile = file.virtualFile ?: return
        processInFileset(project, virtualFile, prefix = "", withCustomized, processor)

        if (!withExternal) return
        val filesetData = LatexProjectStructure.getFilesetDataFor(file) ?: return
        filesetData.externalDocumentInfo.forEach { info ->
            val prefix = info.labelPrefix
            for (file in info.files) {
                processInFileset(project, file, prefix, withCustomized, processor)
            }
        }
    }

    private fun forEachLabelParamInFilesetByName(
        label: String, project: Project, file: VirtualFile, withCustomized: Boolean, processor: (PsiElement) -> Unit
    ) {
        val filesetData = LatexProjectStructure.getFilesetDataFor(file, project)
        val scope = filesetData?.filesetScope?.restrictedByFileTypes(LatexFileType) ?: GlobalSearchScope.fileScope(project, file)
        NewLabelsIndex.forEachByName(label, project, scope) { container ->
            val param = extractLabelParamIn(container, withCustomized = false) ?: return@forEachByName
            processor(param)
        }

        // Custom commands
        if (!withCustomized) return
        if(filesetData == null) return
        processCustomizedInFileset(project, filesetData, "") { customLabel, container, param ->
            if (label == customLabel) {
                processor(param)
            }
        }
    }

    /**
     * Finds all the labels by name in the fileset of the given file, including external documents and custom commands or environments if specified.
     *
     *
     * @param processor Processes the parameter element containing the label text.
     */
    fun forEachLabelParamByName(label: String, file: PsiFile, withExternal: Boolean = true, withCustomized: Boolean = true, processor: (PsiElement) -> Unit) {
        val project = file.project
        val virtualFile = file.virtualFile ?: return
        forEachLabelParamInFilesetByName(label, project, virtualFile, withCustomized, processor)

        if (!withExternal) return
        val filesetData = LatexProjectStructure.getFilesetDataFor(file) ?: return
        filesetData.externalDocumentInfo.forEach { info ->
            if (!label.startsWith(info.labelPrefix)) return@forEach
            val labelWithoutPrefix = label.removePrefix(info.labelPrefix)
            for (file in info.files) {
                forEachLabelParamInFilesetByName(labelWithoutPrefix, project, file, withCustomized, processor)
            }
        }
    }

    /**
     * Finds all the labels by name in the fileset of the given file, including external documents and custom commands or environments if specified.
     */
    fun getLabelParamsByName(label: String, file: PsiFile, withExternal: Boolean = true, withCustomized: Boolean = true): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        forEachLabelParamByName(label, file, withExternal, withCustomized) {
            result.add(it)
        }
        return result
    }
}