package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.requiredParamAt
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.PredefinedDefinitionCommands
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.getNthRequiredParameter
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverse

object LatexDefinitionUtil {

    /**
     * Parses command definitions in the library file, only recognizing command names but no semantics (because they can be very complex).
     *
     * The semantics of the command definitions in latex libraries can be manually specified in the [nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands].
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
            if (defCommandName in PredefinedDefinitionCommands.namesOfAllCommandDef) {
                val nameWithSlash = getCommandDefNameStub(defStub, idx, topLevelStubs) ?: continue
                val pointer = pointerManager.createSmartPsiElementPointer(defStub.psi, psiFile)
                definitions.add(
                    SourcedCmdDefinition(
                        LSemanticCommand(
                            nameWithSlash.removePrefix("\\"),
                            pkgName
                        ),
                        pointer, DefinitionSource.LibraryScan
                    )
                )
            }
            else if (defCommandName in PredefinedDefinitionCommands.namesOfAllEnvironmentDef) {
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
                    SourcedEnvDefinition(semanticEnv, pointer, DefinitionSource.LibraryScan)
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
            if (name in PredefinedDefinitionCommands.namesOfAllCommandDef) {
                val cmdName = getCommandDefNameAST(e) ?: continue
                val semanticCmd = LSemanticCommand(cmdName.removePrefix("\\"), libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedCmdDefinition(semanticCmd, pointer, DefinitionSource.LibraryScan)
                )
            }
            else if (name in PredefinedDefinitionCommands.namesOfAllEnvironmentDef) {
                val envName = getEnvironmentDefNameAST(e) ?: continue
                val semanticEnv = LSemanticEnv(envName, libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedEnvDefinition(semanticEnv, pointer, DefinitionSource.LibraryScan)
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
        defStub.requiredParamAt(0)?.let { // \newcommand{\cmd}{...}
            return it.trim()
        }
        // \def\cmd\something
        val nextCommand = stubs.getOrNull(idx + 1) as? LatexCommandsStub ?: return null
        // no need for checking \def\cmd\relax since only top-level stubs are considered
        return nextCommand.commandToken
    }

    private fun getCommandDefNameAST(defCommand: LatexCommands): String? {
        defCommand.parameterList.getOrNull(0)?.let { // \newcommand{\cmd}{...}
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
     *
     *
     * We only process regular command definitions
     */
    fun collectCustomDefinitions(virtualFile: VirtualFile, project: Project, lookup: LatexSemanticsLookup): List<SourcedDefinition> {
        val psiManager = PsiManager.getInstance(project)
        val psiFile = psiManager.findFile(virtualFile) as? LatexFile ?: return emptyList()
        if (DumbService.isDumb(project)) return emptyList()
        // let us use the index to find the command definitions
        val definitions = mutableListOf<SourcedDefinition>()
        val manager = SmartPointerManager.getInstance(project)

        val defCommands = NewSpecialCommandsIndex.getRegularCommandDef(project, virtualFile)
        for (defCommand in defCommands) {
            val semantics = parseRegularCommandDef(defCommand, lookup, project) ?: continue
            val pointer = manager.createSmartPsiElementPointer(defCommand, psiFile)
            definitions.add(
                SourcedCmdDefinition(semantics, pointer, DefinitionSource.UserDefined)
            )
        }

        val defEnvironments = NewSpecialCommandsIndex.getRegularEnvDef(project, virtualFile)
        for (defCommand in defEnvironments) {
            val semantics = parseEnvironmentDef(defCommand, lookup, project) ?: continue
            val pointer = manager.createSmartPsiElementPointer(defCommand, psiFile)
            definitions.add(
                SourcedEnvDefinition(semantics, pointer, DefinitionSource.UserDefined)
            )
        }
        return definitions
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

    private fun List<Pair<LArgumentType, String>>.getNthRequiredArg(index: Int): String? {
        return getNthArgOfType(index, LArgumentType.REQUIRED)
    }

    private fun List<Pair<LArgumentType, String>>.getNthOptionalArg(index: Int): String? {
        return getNthArgOfType(index, LArgumentType.OPTIONAL)
    }

    private val namesOfCmdDefRegular = buildSet {
        PredefinedDefinitionCommands.regularDefinitionOfCommand.mapTo(this) { it.name }
    }
    private val namesOfCmdDefMath = buildSet {
        PredefinedDefinitionCommands.definitionOfMathCommand.mapTo(this) { it.name }
    }

    private val namesOfCmdDefArgSpec = buildSet {
        PredefinedDefinitionCommands.argSpecDefinitionOfCommand.mapTo(this) { it.name }
    }

    private fun parseCommandDef(defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project): LSemanticCommand? {
        val defCmdName = defCommand.name?.removePrefix("\\") ?: return null
        return when (defCmdName) {
            in namesOfCmdDefRegular -> parseRegularCommandDef(defCommand, lookup, project)
            in namesOfCmdDefMath -> parseCommandDefNameOnlyUnderCtx(defCommand, setOf(LatexContexts.Math))
            in namesOfCmdDefArgSpec -> parseArgSpecCommandDef(defCommand, lookup, project)
            // arg spec type like \NewDocumentCommand{\cmd}{mO{default}m}{code} are too complex to handle for now
            else -> parseCommandDefNameOnlyUnderCtx(defCommand)
        }
    }

    private fun parseCommandDefNameOnlyUnderCtx(defCommand: LatexCommands, requiredCtx: LContextSet = emptySet()): LSemanticCommand? {
        val declaredName = defCommand.requiredParameterText(0) ?: return null
        return LSemanticCommand(declaredName.removePrefix("\\"), "", requiredCtx)
    }

    private fun buildRegularArgSignature(numArgs: Int, hasOptionalArgs: Boolean): List<LArgumentType> {
        if (numArgs <= 0) return emptyList()
        return List(numArgs) { i ->
            if (i == 0 && hasOptionalArgs) LArgumentType.OPTIONAL else LArgumentType.REQUIRED
        }
    }

    private fun buildArgSpecSignature(argSpec: String?): List<LArgumentType> {
        if (argSpec == null || argSpec.isEmpty()) return emptyList()
        return buildList {
            for (c in argSpec) {
                when (c) {
                    'm' -> add(LArgumentType.REQUIRED)
                    'o', 'O' -> add(LArgumentType.OPTIONAL)
                    // ignore other specifiers for now
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
    ): LSemanticCommand? {
        val name = rawName.removePrefix("\\") // remove the leading backslash
        codeRawText ?: return LSemanticCommand(name, "")
        val codeText = codeRawText.trim()
        if (argSignature.isEmpty() && PatternMagic.commandToken.matches(codeText)) {
            // this is an alias definition, e.g., \newcommand{\cmd}{\othercmd}
            val originalSemantic = lookup.lookupCommand(codeText.removePrefix("\\"))
            if (originalSemantic != null) {
                // use the original command semantics
                val description = "Alias for ${originalSemantic.displayName}"
                return LSemanticCommand(
                    name, "", originalSemantic.requiredContext, originalSemantic.arguments, description, originalSemantic.display
                )
            }
        }

        val codeElement = LatexPsiHelper.createFromText(codeRawText, project)
        val requiredContext = guessRequiredContext(codeElement, lookup)
        if (argSignature.isEmpty()) {
            return LSemanticCommand(name, "", requiredContext, description = codeRawText, arguments = emptyList())
        }
        val argIntro = guessArgumentContextIntroAndExitState(codeElement, argSignature.size, lookup).first
        val arguments = argIntro.mapIndexed { i, argIntro ->
            LArgument("#${i + 1}", argSignature[i], argIntro)
        }
        return LSemanticCommand(name, "", requiredContext, arguments, description = codeText)
    }

    private fun guessRequiredContext(definitionElement: PsiElement?, lookup: LatexSemanticsLookup): LContextSet {
        definitionElement ?: return emptySet()
        val necessaryContexts = mutableSetOf<LatexContext>()
        LatexPsiUtil.traverseRecordingContextIntro(definitionElement, lookup) { e, introList ->
            val requiredContext: LContextSet? = when (e) {
                is LatexCommands -> lookup.lookupCommand(e.name?.removePrefix("\\") ?: "")?.requiredContext
                is LatexEnvironment -> lookup.lookupEnv(e.getEnvironmentName())?.requiredContext
                else -> null
            }
            if (requiredContext != null && requiredContext.isNotEmpty()) {
                LatexContextIntro.computeMinimalRequiredContext(introList, requiredContext)?.forEach {
                    necessaryContexts.add(it)
                } // we will ignore cases where the context cannot be satisfied
            }
        }
        return necessaryContexts
    }

    private val parameterPlaceholderRegex = Regex("#[1-9]")

    private fun guessArgumentContextIntroAndExitState(codeElement: PsiElement, argCount: Int, lookup: LatexSemanticsLookup): Pair<List<LatexContextIntro>, List<LatexContextIntro>> {
        val contextIntroList = Array(argCount) { LatexContextIntro.inherit() }
        val exitState = LatexPsiUtil.traverseRecordingContextIntro(codeElement, lookup) traverse@{ e, introList ->
            if (e.elementType != LatexTypes.NORMAL_TEXT_WORD) return@traverse
            if (!e.textContains('#')) return@traverse
            parameterPlaceholderRegex.findAll(e.text).forEach { match ->
                val paramIndex = match.value.removePrefix("#").toIntOrNull() ?: return@forEach
                if (paramIndex < 1 || paramIndex > argCount) return@forEach
                val reducedIntro = LatexContextIntro.composeList(introList)
                val prevIntro = contextIntroList[paramIndex - 1]
                contextIntroList[paramIndex - 1] = LatexContextIntro.union(prevIntro, reducedIntro)
            }
        }
        return contextIntroList.asList() to exitState
    }

    private val namesOfEnvDefRegular = buildSet {
        PredefinedDefinitionCommands.regularDefinitionOfEnvironment.mapTo(this) { it.name }
    }
    private val namesOfEnvDefArgSpec = buildSet {
        PredefinedDefinitionCommands.argSpecDefinitionOfEnvironment.mapTo(this) { it.name }
    }

    private fun parseEnvironmentDef(defCommand: LatexCommands, lookup: LatexSemanticsLookup, project: Project): LSemanticEnv? {
        val defCmdName = defCommand.name?.removePrefix("\\") ?: return null
        val contents = extractParameterTypeAndContent(defCommand)
        return when (defCmdName) {
            in namesOfEnvDefRegular -> parseRegularEnvironmentDef(defCommand, contents, lookup, project)
            in namesOfEnvDefArgSpec -> parseArgSpecEnvironmentDef(defCommand, contents, lookup, project)
            else -> parseEnvDefNameOnlyUnderCtx(defCommand)
        }
    }

    private fun parseEnvDefNameOnlyUnderCtx(defCommand: LatexCommands, requiredCtx: LContextSet = emptySet()): LSemanticEnv? {
        val declaredName = defCommand.requiredParameterText(0) ?: return null
        return LSemanticEnv(declaredName, "", requiredCtx)
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
        val codeElement = rebuildEnvDefinitionCommand(defCommand, contents, project) ?: return LSemanticEnv(envName, "")
        val numArg = contents.getNthOptionalArg(0)?.toIntOrNull() ?: 0
        val hasOptionalArg = contents.getNthOptionalArg(1) != null
        val beginElement = codeElement.getNthRequiredParameter(1)
        val endElement = codeElement.getNthRequiredParameter(2)
        return buildEnvironmentSemantics(envName, beginElement, endElement, buildRegularArgSignature(numArg, hasOptionalArg), defCommand, lookup, project)
    }

    private fun parseArgSpecEnvironmentDef(defCommand: LatexCommands, contents: List<Pair<LArgumentType, String>>, lookup: LatexSemanticsLookup, project: Project): LSemanticEnv? {
        // This will be something like \NewDocumentEnvironment{env}{args spec}{begin code}{end code}
        val envName = contents.getNthRequiredArg(0) ?: return null
        val codeElement = rebuildEnvDefinitionCommand(defCommand, contents, project) ?: return LSemanticEnv(envName, "")
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
        if (beginElement == null || endElement == null) return LSemanticEnv(envName, "")
        val requiredContext = guessRequiredContext(beginElement, lookup)
        val (argIntro1, innerIntroList) = guessArgumentContextIntroAndExitState(beginElement, argTypeList.size, lookup)
        val innerIntro = LatexContextIntro.composeList(innerIntroList)
        val (argIntro2, _) = guessArgumentContextIntroAndExitState(endElement, argTypeList.size, lookup)
        val arguments = argTypeList.mapIndexed { i, type ->
            LArgument("#${i + 1}", type, LatexContextIntro.union(argIntro1[i], argIntro2[i]))
        }
        return LSemanticEnv(envName, "", requiredContext, arguments, innerIntro)
    }


    private fun mergeCmdDefinition(old: SourcedCmdDefinition, new: SourcedCmdDefinition): SourcedCmdDefinition {
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        return SourcedCmdDefinition(old.entity, pointer, old.source)
    }

    private fun mergeEnvDefinition(old: SourcedEnvDefinition, new: SourcedEnvDefinition): SourcedEnvDefinition {
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        return SourcedEnvDefinition(old.entity, pointer, old.source)
    }

    private fun overrideOrKeep(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        if (old.source == DefinitionSource.Predefined && new.source == DefinitionSource.LibraryScan) {
            // do not override predefined environments with package definitions (they are messy)
            return old
        }
        println("Def overridden: $old by $new")
        return new
    }

    fun mergeDefinition(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        if (old.entity != new.entity) {
            // TODO: change to log after testing
            println("Merging commands: $old and $new")
        }
        // do not override primitive definitions
        if (old.source == DefinitionSource.Primitive) return old
        if (new.source == DefinitionSource.Primitive) return new
        if (old.entity != new.entity) {
            return overrideOrKeep(old, new)
        }

        return when (old) {
            is SourcedCmdDefinition -> when (new) {
                is SourcedCmdDefinition -> mergeCmdDefinition(old, new)
                is SourcedEnvDefinition -> new
            }

            is SourcedEnvDefinition -> when (new) {
                is SourcedEnvDefinition -> mergeEnvDefinition(old, new)
                is SourcedCmdDefinition -> new
            }
        }
    }
}