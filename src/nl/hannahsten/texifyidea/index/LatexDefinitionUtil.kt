package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.requiredParamAt
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdDefinitions
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdGeneric
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverse
import nl.hannahsten.texifyidea.editor.folding.MathStyle

object LatexDefinitionUtil {

    /**
     * Parses command definitions in the library file, only recognizing command names but no semantics (because they can be very complex).
     *
     * The semantics of the command definitions in latex libraries can be manually specified in the [nl.hannahsten.texifyidea.lang.predefined.AllPredefined].
     *
     */
    fun collectDefinitionsInLib(libInfo: LatexLibraryInfo, project: Project): List<SourcedDefinition> {
        val psiFile = PsiManager.getInstance(project).findFile(libInfo.location) as? LatexFile ?: return emptyList()
        psiFile.stubTree?.root?.let {
            return collectDefinitionsInLibStub(it, psiFile, libInfo, project)
        }
        return collectDefinitionsInLibAST(psiFile, libInfo, project)
    }

    private fun collectDefinitionsInLibStub(stub: PsiFileStub<*>, psiFile: LatexFile, libInfo: LatexLibraryInfo, project: Project): List<SourcedDefinition> {
        val pkgName = libInfo.name
        val topLevelStubs = stub.childrenStubs
        val pointerManager = SmartPointerManager.getInstance(project)
        val definitions = mutableListOf<SourcedDefinition>()
        for ((idx, defStub) in topLevelStubs.withIndex()) {
            if (defStub !is LatexCommandsStub) continue
            val defCommandName = defStub.commandName
            if (defCommandName in PredefinedCmdDefinitions.namesOfAllCommandDef) {
                val nameWithSlash = getCommandDefNameStub(defStub, idx, topLevelStubs) ?: continue
                val pointer = pointerManager.createSmartPsiElementPointer(defStub.psi, psiFile)
                definitions.add(
                    SourcedDefinition(
                        LSemanticCommand(
                            nameWithSlash.removePrefix("\\"),
                            pkgName
                        ),
                        pointer, DefinitionSource.LibraryScan
                    )
                )
            }
            else if (defCommandName in PredefinedCmdDefinitions.namesOfAllEnvironmentDef) {
                val envName = getEnvironmentDefNameStub(defStub) ?: continue
                val semanticEnv =
                    LSemanticEnv(
                        envName,
                        pkgName,
                        arguments = emptyList(),
                        contextSignature = LatexContextIntro.inherit()
                    )
                val pointer = pointerManager.createSmartPsiElementPointer(defStub.psi, psiFile)
                definitions.add(
                    SourcedDefinition(semanticEnv, pointer, DefinitionSource.LibraryScan)
                )
            }
        }
        return definitions
    }

    private fun collectDefinitionsInLibAST(psiFile: LatexFile, libInfo: LatexLibraryInfo, project: Project): List<SourcedDefinition> {
        val contentNode = psiFile.findFirstChildTyped<LatexContent>() ?: return emptyList()
        val elements = contentNode.traverse(2) // not so much commands, so we can afford to traverse the whole tree
        val pointerManager = SmartPointerManager.getInstance(project)
        val result = mutableListOf<SourcedDefinition>()
        for (e in elements) {
            if (e !is LatexCommands) continue // only commands
            val name = e.name?.removePrefix("\\") ?: continue
            if (name in PredefinedCmdDefinitions.namesOfAllCommandDef) {
                val cmdName = getCommandDefNameAST(e) ?: continue
                val semanticCmd = LSemanticCommand(cmdName.removePrefix("\\"), libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedDefinition(semanticCmd, pointer, DefinitionSource.LibraryScan)
                )
            }
            else if (name in PredefinedCmdDefinitions.namesOfAllEnvironmentDef) {
                val envName = getEnvironmentDefNameAST(e) ?: continue
                val semanticEnv = LSemanticEnv(envName, libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedDefinition(semanticEnv, pointer, DefinitionSource.LibraryScan)
                )
            }
        }
        return result
    }

    /**
     * Find the first parameter of the command definition stub that defines a command name.
     *
     * @return shift in index, name of the defined command
     */
    private fun getCommandDefNameStub(defStub: LatexCommandsStub, idx: Int, stubs: List<StubElement<*>>): String? {
        defStub.requiredParamAt(0)?.let {
            // \newcommand{\cmd}{...}
            return it.trim()
        }
        // \def\cmd\something
        val nextCommand = stubs.getOrNull(idx + 1) as? LatexCommandsStub ?: return null
        // no need for checking \def\cmd\relax since only top-level stubs are considered
        return nextCommand.commandToken
    }

    private fun getCommandDefNameAST(defCommand: LatexCommands): String? {
        defCommand.parameterList.getOrNull(0)?.let {
            // \newcommand{\cmd}{...}
            return it.contentText().trim()
        }
        return LatexPsiUtil.getDefinedCommandElement(defCommand)?.name
    }

    private fun getEnvironmentDefNameStub(defStub: LatexCommandsStub): String? {
        // must be something like \newenvironment{env}{...}{...}
        return defStub.requiredParamAt(0)?.trim()
    }

    private fun getEnvironmentDefNameAST(defCommand: LatexCommands): String? {
        // must be something like \newenvironment{env}{...}{...}
        return defCommand.parameterList.getOrNull(0)?.contentText()?.trim()
    }

    /**
     * We only process regular command definitions and environment definitions here.
     * `\let` and `\def` are not processed because they are too flexible to parse.
     *
     */
    fun collectCustomDefinitions(virtualFile: VirtualFile, project: Project, bundle: WorkingFilesetDefinitionBundle) {
        val psiManager = PsiManager.getInstance(project)
        val psiFile = psiManager.findFile(virtualFile) as? LatexFile ?: return
        val manager = SmartPointerManager.getInstance(project)

        // let us use the index to find the command definitions
        val defCommands = NewSpecialCommandsIndex.getAllDefinitions(project, virtualFile)
        val source = if (virtualFile.fileType == LatexFileType) DefinitionSource.UserDefined else DefinitionSource.LibraryScan
        for (defCommand in defCommands) {
            val defCmdName = defCommand.nameWithoutSlash ?: continue
            val semantics = when (defCmdName) {
                in PredefinedCmdDefinitions.namesOfAllCommandDef -> parseCommandDef(defCmdName, defCommand, bundle, project)
                in PredefinedCmdDefinitions.namesOfAllEnvironmentDef -> parseEnvironmentDef(defCmdName, defCommand, bundle, project)
                else -> continue
            } ?: continue
            val pointer = manager.createSmartPsiElementPointer(defCommand, psiFile)
            bundle.addCustomDefinition(
                SourcedDefinition(semantics, pointer, source)
            )
        }
    }

    private fun extractParameterTypeAndContent(command: LatexCommands): List<Pair<LArgumentType, String>> {
        val stub = command.stub
        if (stub != null) {
            return stub.parameters.map {
                LatexPsiUtil.stubTypeToLArgumentType(it.type) to it.content
            }
        }
        return command.parameterList.map {
            val type = if (it.optionalParam != null) LArgumentType.OPTIONAL else LArgumentType.REQUIRED
            type to it.contentText()
        }
    }

    private fun List<Pair<LArgumentType, String>>.getNthArgOfType(index: Int, targetType: LArgumentType): String? {
        var idx = 0
        for ((type, text) in this) {
            if (type == targetType) {
                if (idx == index) return text
                idx++
            }
        }
        return null
    }

    private fun List<Pair<LArgumentType, String>>.getNthRequiredArg(index: Int): String? = getNthArgOfType(index, LArgumentType.REQUIRED)

    private fun List<Pair<LArgumentType, String>>.getNthOptionalArg(index: Int): String? = getNthArgOfType(index, LArgumentType.OPTIONAL)

    private val namesOfCmdDefRegular = buildSet {
        PredefinedCmdDefinitions.regularDefinitionOfCommand.mapTo(this) { it.name }
    }
    private val namesOfCmdDefMath = buildSet {
        PredefinedCmdDefinitions.definitionOfMathCommand.mapTo(this) { it.name }
    }

    private val namesOfCmdDefText = buildSet {
        PredefinedCmdDefinitions.definitionOfTextCommand.mapTo(this) { it.name }
    }

    private val namesOfCmdDefArgSpec = buildSet {
        PredefinedCmdDefinitions.argSpecDefinitionOfCommand.mapTo(this) { it.name }
    }

    private fun parseCommandDef(
        defCmdName: String, defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project
    ): LSemanticCommand? = when (defCmdName) {
        in namesOfCmdDefRegular -> parseRegularCommandDef(defCommand, lookup, project)
        in namesOfCmdDefArgSpec -> parseArgSpecCommandDef(defCommand, lookup, project)
        in namesOfCmdDefMath -> parseCommandDefNameOnlyUnderCtx(defCommand, setOf(LatexContexts.Math))
        in namesOfCmdDefText -> parseCommandDefNameOnlyUnderCtx(defCommand, setOf(LatexContexts.Text))
        else -> parseCommandDefNameOnlyUnderCtx(defCommand)
    }

    private fun parseCommandDefNameOnlyUnderCtx(defCommand: LatexCommands, requiredCtx: LContextSet? = null): LSemanticCommand? {
        // note that requiredCtx == null means applicable in all contexts
        val declaredName = defCommand.requiredParameterText(0) ?: return null
        return LSemanticCommand(declaredName.removePrefix("\\"), LatexLib.CUSTOM, requiredCtx)
    }

    private fun buildRegularArgSignature(numArgs: Int, hasOptionalArgs: Boolean): List<LArgumentType> {
        if (numArgs <= 0) return emptyList()
        return List(numArgs) { i ->
            if (i == 0 && hasOptionalArgs) LArgumentType.OPTIONAL else LArgumentType.REQUIRED
        }
    }

    private fun buildArgSpecSignature(argSpec: String?): List<LArgumentType> {
        if (argSpec.isNullOrEmpty()) return emptyList()
        return buildList {
            var bracketCount = 0
            for (c in argSpec) {
                when (c) {
                    'm' -> if (bracketCount == 0) add(LArgumentType.REQUIRED) // 'm' is for mandatory
                    'o', 'O' -> if (bracketCount == 0) add(LArgumentType.OPTIONAL) // 'o' is for optional, 'O' is for optional with default value
                    // ignore other specifiers for now
                    '{' -> bracketCount++
                    '}' -> bracketCount--
                }
            }
        }
    }

    private fun parseRegularCommandDef(defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project): LSemanticCommand? {
        // command definitions like \newcommand{\cmd}[num][default]{code}
        val contents = extractParameterTypeAndContent(defCommand)
        val declaredName = contents.getNthRequiredArg(0) ?: return null
        val codeText = contents.getNthRequiredArg(1)
        val numOfArgs = contents.getNthOptionalArg(0)?.toIntOrNull() ?: 0
        val hasOptionalArgs = contents.getNthOptionalArg(1) != null
        return buildCommandSemantics(project, lookup, declaredName, codeText, buildRegularArgSignature(numOfArgs, hasOptionalArgs))
    }

    private fun parseArgSpecCommandDef(defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project): LSemanticCommand? {
        // command definitions like \NewDocumentCommand{\cmd}{args spec}{code}
        val contents = extractParameterTypeAndContent(defCommand)
        val declaredName = contents.getNthRequiredArg(0) ?: return null
        val argSignature = buildArgSpecSignature(contents.getNthRequiredArg(1))
        val codeText = contents.getNthRequiredArg(2)
        return buildCommandSemantics(project, lookup, declaredName, codeText, argSignature)
    }

    private fun buildCommandSemantics(
        project: Project, lookup: LatexSemanticsLookup,
        rawName: String, codeRawText: String?, argSignature: List<LArgumentType>
    ): LSemanticCommand {
        val name = rawName.removePrefix("\\") // remove the leading backslash
        codeRawText ?: return LSemanticCommand(name, LatexLib.CUSTOM)
        val codeText = codeRawText.trim()
        if (argSignature.isEmpty() && PatternMagic.commandToken.matches(codeText)) {
            // this is an alias definition, e.g., \newcommand{\cmd}{\othercmd}
            val originalSemantic = lookup.lookupCommand(codeText.removePrefix("\\"))
            if (originalSemantic != null) {
                // use the original command semantics
                val description = "Alias for ${originalSemantic.displayName}"
                val semantics = LSemanticCommand(
                    name, LatexLib.CUSTOM, originalSemantic.applicableContext, originalSemantic.arguments, description, originalSemantic.display
                )
                originalSemantic.copyMetaTo(semantics) // also remember to copy meta info
                return semantics
            }
        }

        val codeElement = LatexPsiHelper.createFromText(codeRawText, project)
        val applicableContext = guessApplicableContexts(codeElement, lookup)
        if (argSignature.isEmpty()) {
            val display = deriveDisplayFromDefinition(project, lookup, codeText)
            return LSemanticCommand(name, LatexLib.CUSTOM, applicableContext, description = codeRawText, arguments = emptyList(), display = display)
        }
        val argIntro = guessArgumentContextIntro(codeElement, argSignature.size, lookup)
        val arguments = argIntro.mapIndexed { i, argIntro ->
            LArgument("#${i + 1}", argSignature[i], argIntro)
        }
        return LSemanticCommand(name, LatexLib.CUSTOM, applicableContext, arguments, description = codeText)
    }

    private fun deriveDisplayFromDefinition(project: Project, lookup: LatexSemanticsLookup, codeText: String): String? {
        if (codeText.isBlank()) return null
        val trimmed = codeText.trim()
        val psiFile = LatexPsiHelper.createFromText(trimmed, project)
        val command = psiFile.findFirstChildTyped<LatexCommands>() ?: return null
        if (command.text != trimmed) return null
        val commandName = command.name?.removePrefix("\\") ?: return null
        val semantic = lookup.lookupCommand(commandName) ?: return null
        val style = semantic.getMeta(MathStyle.META_KEY) ?: return null
        val firstReq = command.firstRequiredParameter() ?: return null
        val rawText = firstReq.contentText()
        return style.map(rawText)
    }

    private fun guessApplicableContexts(definitionElement: PsiElement?, lookup: LatexSemanticsLookup): LContextSet? {
        definitionElement ?: return null
        val applicableContexts = mutableSetOf<LatexContext>()
        LatexPsiUtil.traverseRecordingContextIntro(definitionElement, lookup) { e, introList ->
            val requiredContext: LContextSet? = when (e) {
                is LatexCommands -> lookup.lookupCommand(e.name?.removePrefix("\\") ?: "")?.applicableContext
                is LatexEnvironment -> lookup.lookupEnv(e.getEnvironmentName())?.applicableContext
                else -> null
            }
            if (!requiredContext.isNullOrEmpty()) {
                LatexContextIntro.computeMinimalRequiredContext(introList, requiredContext)?.forEach {
                    applicableContexts.add(it)
                } // we will ignore cases where the context cannot be satisfied
            }
        }
        if (applicableContexts.isEmpty()) return null // if we cannot find any context, just guess that it is applicable in all contexts
        // actually it is the union of all sub-applicable contexts
        return applicableContexts
    }

    private val parameterPlaceholderRegex = Regex("#[1-9]")

    /**
     *
     *
     * @param codeElement A definition of a command/environment
     * @param argCount Number of arguments of the command/environment being defined as [codeElement]
     * @param lookup Look up known semantics
     * @param contextIntroArr For each argument, what contexts are introduced
     * @return ?
     */
    private fun guessArgumentContextIntroAndExitState(
        codeElement: PsiElement, argCount: Int, lookup: LatexSemanticsLookup, contextIntroArr: Array<LatexContextIntro?> = arrayOfNulls(argCount)
    ): Pair<Array<LatexContextIntro?>, List<LatexContextIntro>> {
        val exitState = LatexPsiUtil.traverseRecordingContextIntro(codeElement, lookup) traverse@{ e, introList ->
            // In definitions, arguments may appear in raw/verbatim-like areas (RAW_TEXT), so also scan those for placeholders.
            // Missing a placeholder here causes the argument to be treated as `Comment` by default, which can grey-out large code blocks.
            if (e.elementType != LatexTypes.NORMAL_TEXT_WORD && e.elementType != LatexTypes.RAW_TEXT) return@traverse
            if (!e.textContains('#')) return@traverse
            parameterPlaceholderRegex.findAll(e.text).forEach { match ->
                val paramIndex = match.value.removePrefix("#").toIntOrNull() ?: return@forEach
                if (paramIndex !in 1..argCount) return@forEach
                val reducedIntro = LatexContextIntro.composeList(introList)
                val prevIntro = contextIntroArr[paramIndex - 1]
                contextIntroArr[paramIndex - 1] = prevIntro?.let { LatexContextIntro.union(it, reducedIntro) } ?: reducedIntro
            }
        }

        return contextIntroArr to exitState
    }

    internal fun guessArgumentContextIntro(
        codeElement: PsiElement, argCount: Int, lookup: LatexSemanticsLookup,
        contextIntroArr: Array<LatexContextIntro?> = arrayOfNulls(argCount)
    ): List<LatexContextIntro> = guessArgumentContextIntroAndExitState(codeElement, argCount, lookup, contextIntroArr).first.map {
        // If we cannot infer the context for an argument, do not treat it as a comment.
        // In practice, a `Comment` fallback can grey-out large parts of a document when inference fails.
        it ?: LatexContextIntro.inherit()
    }

    private val namesOfEnvDefRegular = buildSet {
        PredefinedCmdDefinitions.regularDefinitionOfEnvironment.mapTo(this) { it.name }
        PredefinedCmdGeneric.listingsDefinitionCommands.mapTo(this) { it.name }
    }
    private val namesOfEnvDefArgSpec = buildSet {
        PredefinedCmdDefinitions.argSpecDefinitionOfEnvironment.mapTo(this) { it.name }
    }

    private fun parseEnvironmentDef(
        defCmdName: String, defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project
    ): LSemanticEnv? {
        val contents = extractParameterTypeAndContent(defCommand)
        return when (defCmdName) {
            in namesOfEnvDefRegular -> parseRegularEnvironmentDef(defCommand, contents, lookup, project)
            in namesOfEnvDefArgSpec -> parseArgSpecEnvironmentDef(defCommand, contents, lookup, project)
            else -> parseEnvDefNameOnlyUnderCtx(defCommand)
        }
    }

    private fun parseEnvDefNameOnlyUnderCtx(defCommand: LatexCommands, requiredCtx: LContextSet? = null): LSemanticEnv? {
        // note that requiredCtx == null means applicable in all contexts
        val declaredName = defCommand.requiredParameterText(0) ?: return null
        return LSemanticEnv(declaredName, LatexLib.CUSTOM, requiredCtx)
    }

    private fun rebuildEnvDefinitionCommand(
        defCommand: LatexCommands, contents: List<Pair<LArgumentType, String>>, project: Project
    ): LatexCommands? {
        val rebuiltCode = buildString {
            val name = defCommand.name
            if (name == null) append("\\newenvironment")
            else {
                if (!name.startsWith('\\')) append('\\')
                append(name)
            }
            for ((type, content) in contents) {
                when (type) {
                    LArgumentType.REQUIRED -> append('{').append(content).append('}')
                    LArgumentType.OPTIONAL -> append('[').append(content).append(']')
                }
            }
        }
        return LatexPsiHelper.createFromText(rebuiltCode, project).findFirstChildTyped<LatexCommands>()
    }

    private fun parseRegularEnvironmentDef(defCommand: LatexCommands, contents: List<Pair<LArgumentType, String>>, lookup: LatexSemanticsLookup, project: Project): LSemanticEnv? {
        // This will be something like \newenvironment{env}[num arg][default value]{begin code}{end code}
        val envName = contents.getNthRequiredArg(0) ?: return null
        val codeElement = rebuildEnvDefinitionCommand(defCommand, contents, project) ?: return LSemanticEnv(envName, LatexLib.CUSTOM)
        val numArg = contents.getNthOptionalArg(0)?.toIntOrNull() ?: 0
        val hasOptionalArg = contents.getNthOptionalArg(1) != null
        val beginElement = codeElement.getNthRequiredParameter(1)
        val endElement = codeElement.getNthRequiredParameter(2)
        return buildEnvironmentSemantics(envName, beginElement, endElement, buildRegularArgSignature(numArg, hasOptionalArg), defCommand, lookup, project)
    }

    private fun parseArgSpecEnvironmentDef(defCommand: LatexCommands, contents: List<Pair<LArgumentType, String>>, lookup: LatexSemanticsLookup, project: Project): LSemanticEnv? {
        // This will be something like \NewDocumentEnvironment{env}{args spec}{begin code}{end code}
        val envName = contents.getNthRequiredArg(0) ?: return null
        val codeElement = rebuildEnvDefinitionCommand(defCommand, contents, project) ?: return LSemanticEnv(envName, LatexLib.CUSTOM)
        val argSignature = buildArgSpecSignature(contents.getNthRequiredArg(1))
        val beginElement = codeElement.getNthRequiredParameter(2)
        val endElement = codeElement.getNthRequiredParameter(3)
        return buildEnvironmentSemantics(envName, beginElement, endElement, argSignature, defCommand, lookup, project)
    }

    private fun buildEnvironmentSemantics(
        envName: String, beginElement: PsiElement?, endElement: PsiElement?,
        argTypeList: List<LArgumentType>,
        defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project
    ): LSemanticEnv {
        if (beginElement == null || endElement == null) return LSemanticEnv(envName, LatexLib.CUSTOM)
        val applicableContexts = guessApplicableContexts(beginElement, lookup)
        val (argIntro1, innerIntroList) = guessArgumentContextIntroAndExitState(beginElement, argTypeList.size, lookup)
        val innerIntro = LatexContextIntro.composeList(innerIntroList)
        val argIntro2 = guessArgumentContextIntro(endElement, argTypeList.size, lookup, argIntro1)
        val arguments = argTypeList.mapIndexed { i, type ->
            LArgument("#${i + 1}", type, argIntro2[i])
        }
        return LSemanticEnv(envName, LatexLib.CUSTOM, applicableContexts, arguments, innerIntro)
    }

    private fun mergeApplicableContexts(
        old: LSemanticEntity, new: LSemanticEntity
    ): LContextSet? {
        val oldCtx = old.applicableContext
        val newCtx = new.applicableContext
        if (oldCtx == null) return newCtx
        if (newCtx == null) return oldCtx
        return oldCtx.union(newCtx)
    }

    private fun replaceIfEmpty(old: String, new: String): String = old.ifEmpty { new }

    private fun chooseArgs(old: List<LArgument>, new: List<LArgument>, isOldPredefined: Boolean): List<LArgument> {
        if (new.isEmpty()) return old
        if (old.isEmpty()) return new
        return if (isOldPredefined) old else if (new.size > old.size) new else old
    }

    private fun mergeCmdDefinition(oldCmd: LSemanticCommand, newCmd: LSemanticCommand, isOldPredefined: Boolean): LSemanticCommand {
        val ctx = mergeApplicableContexts(oldCmd, newCmd)
        val arg = chooseArgs(oldCmd.arguments, newCmd.arguments, isOldPredefined)
        val description = replaceIfEmpty(oldCmd.description, newCmd.description)
        val display = newCmd.display ?: oldCmd.display
        return LSemanticCommand(
            oldCmd.name, oldCmd.dependency,
            ctx, arg, description, display
        ).also {
            mergeMetaTo(it, oldCmd, newCmd, isOldPredefined)
        }
    }

    private fun mergeEnvDefinition(oldEnv: LSemanticEnv, newEnv: LSemanticEnv, isOldPredefined: Boolean): LSemanticEnv {
        val ctx = mergeApplicableContexts(oldEnv, newEnv)
        val innerIntro = LatexContextIntro.union(newEnv.contextSignature, oldEnv.contextSignature)
        val arg = chooseArgs(oldEnv.arguments, newEnv.arguments, isOldPredefined)
        val description = replaceIfEmpty(oldEnv.description, newEnv.description)
        return LSemanticEnv(
            oldEnv.name, oldEnv.dependency,
            ctx, arg, innerIntro, description
        ).also {
            mergeMetaTo(it, oldEnv, newEnv, isOldPredefined)
        }
    }

    private fun mergeMetaTo(created: LSemanticEntity, old: LSemanticEntity, new: LSemanticEntity, isOldPredefined: Boolean) {
        // currently, we just copy all meta info, but in the future we may want to be more careful about merging meta info
        old.copyMetaTo(created)
        new.copyMetaTo(created)
    }

    /**
     * Merge a new definition parsed _from a library_ into an old definition.
     */
    fun mergeDefinition(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        // do not override primitive definitions
        if (old.source == DefinitionSource.Primitive) return old
        if (new.source == DefinitionSource.Primitive) return new

        val oldEntity = old.entity
        val newEntity = new.entity
        if (oldEntity != newEntity) {
            // if the entities are not the same, we cannot merge them
            // so we just override or keep the old one
            if (old.source == DefinitionSource.Predefined && new.source == DefinitionSource.LibraryScan) {
                return old
            }
            return new
        }
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        val isOldPredefined = old.source == DefinitionSource.Predefined
        val entity = when (oldEntity) {
            is LSemanticCommand -> when (newEntity) {
                is LSemanticCommand -> mergeCmdDefinition(oldEntity, newEntity, isOldPredefined)
                else -> newEntity
            }

            is LSemanticEnv -> when (newEntity) {
                is LSemanticEnv -> mergeEnvDefinition(oldEntity, newEntity, isOldPredefined)
                else -> newEntity
            }
            // if we have to merge different types of entities, just take the new one
        }
        return SourcedDefinition(entity, pointer, old.source)
    }
}