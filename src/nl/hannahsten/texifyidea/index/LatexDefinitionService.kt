package nl.hannahsten.texifyidea.index

import arrow.atomic.AtomicLong
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.elementType
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.requiredParamAt
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LArgumentType
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexContext
import nl.hannahsten.texifyidea.lang.LatexSemanticLookup
import nl.hannahsten.texifyidea.lang.predefined.PredefinedBasicCommands
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedEnvironments
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.contentText
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverse
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.get

sealed class SourcedDefinition(
    val definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?,
    val source: DefinitionSource = DefinitionSource.UserDefined
) {
    abstract val entity: LSemanticEntity
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourcedDefinition

        return entity.name == other.entity.name
    }

    override fun hashCode(): Int {
        return entity.name.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("Def($entity, ${source.name}")
            if (definitionCommandPointer != null) {
                append(", with PSI")
            }
            append(")")
        }
    }

    enum class DefinitionSource {
        Primitive,
        Predefined,
        Merged,
        Package,
        UserDefined
    }
}

class SourcedCmdDefinition(
    override val entity: LSemanticCommand,
    definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?,
    source: DefinitionSource = DefinitionSource.UserDefined
) : SourcedDefinition(definitionCommandPointer, source)

class SourcedEnvDefinition(
    override val entity: LSemanticEnv,
    definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?,
    source: DefinitionSource = DefinitionSource.UserDefined
) : SourcedDefinition(definitionCommandPointer, source)

object LatexDefinitionUtil {

    private val definitionOfCmdInLib: Set<String> = buildSet {
        add("let")
        add("def")
        PredefinedBasicCommands.definitionOfCommand.forEach { add(it.name) }
    }

    /**
     * Parses command definitions in the library file, only recognizing command names but no semantics (because they can be very complex).
     *
     * The semantics of the command definitions in latex libraries can be manually specified in the [AllPredefinedCommands].
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
            if (defCommandName in definitionOfCmdInLib) {
                val nameWithSlash = getCommandDefNameStub(defStub, idx, topLevelStubs) ?: continue
                val pointer = pointerManager.createSmartPsiElementPointer(defStub.psi, psiFile)
                definitions.add(
                    SourcedCmdDefinition(
                        LSemanticCommand(nameWithSlash.removePrefix("\\"), pkgName), pointer, DefinitionSource.Package
                    )
                )
            }
            else if (defCommandName in AllPredefinedCommands.regularEnvironmentDef) {
                val envName = getEnvironmentDefNameStub(defStub) ?: continue
                val semanticEnv =
                    LSemanticEnv(
                        envName, pkgName, arguments = emptyList(), contextSignature = LatexContextIntro.inherit()
                    )
                val pointer = pointerManager.createSmartPsiElementPointer(defStub.psi, psiFile)
                definitions.add(
                    SourcedEnvDefinition(semanticEnv, pointer, DefinitionSource.Package)
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
            if (name in definitionOfCmdInLib) {
                val cmdName = getCommandDefNameAST(e) ?: continue
                val semanticCmd = LSemanticCommand(cmdName.removePrefix("\\"), libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedCmdDefinition(semanticCmd, pointer, DefinitionSource.Package)
                )
            }
            else if (name in AllPredefinedCommands.regularEnvironmentDef) {
                val envName = getEnvironmentDefNameAST(e) ?: continue
                val semanticEnv = LSemanticEnv(envName, libInfo.name)
                val pointer = pointerManager.createSmartPsiElementPointer(e, psiFile)
                result.add(
                    SourcedEnvDefinition(semanticEnv, pointer, DefinitionSource.Package)
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
     * We only
     */
    fun collectCustomDefinitions(virtualFile: VirtualFile, project: Project, lookup: LatexSemanticLookup): List<SourcedDefinition> {
        val psiManager = PsiManager.getInstance(project)
        val psiFile = psiManager.findFile(virtualFile) as? LatexFile ?: return emptyList()
        if (DumbService.isDumb(project)) return emptyList()
        // let us use the index to find the command definitions
        val definitions = mutableListOf<SourcedCmdDefinition>()
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
        return definitions
    }

//    fun collectCustomDefinitionsAST(psiFile: LatexFile, project: Project, lookup: LatexSemanticLookup): List<SourcedCmdDefinition> {
// //        val
//        // only deal with top-level commands
//        // Latex.bnf: file - content - nomathcontent - command
//        val contentNode = psiFile.findFirstChildTyped<LatexContent>() ?: return emptyList()
//        val elements = contentNode.traverse(2) // not so much commands, so we can afford to traverse the whole tree
//        val pointerManager = SmartPointerManager.getInstance(project)
//        val result = mutableListOf<SourcedCmdDefinition>()
//        for (e in elements) {
//            if (e is LatexNormalText || e is LatexEnvironment) break // possibly not in the preamble
//            if (e !is LatexCommands) continue // only commands
//            val parsed = parseSingleCommandAST(e, lookup, project) ?: continue
//            val ref = pointerManager.createSmartPsiElementPointer(e, psiFile)
//            result.add(
//                SourcedCmdDefinition(parsed, ref, DefinitionSource.UserDefined)
//            )
//        }
//        return result
//    }

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

    private fun parseRegularCommandDef(defCommand: LatexCommands, lookup: LatexSemanticLookup, project: Project): LSemanticCommand? {
        val defCmdName = defCommand.name?.removePrefix("\\") ?: return null
        if (defCmdName !in AllPredefinedCommands.regularCommandDef) return null
        // currently only support command definitions like \newcommand{\cmd}[num][default]{code}

        val parameterTypeAndContent = extractParameterTypeAndContent(defCommand)
        val parameterSize = parameterTypeAndContent.size

        var declaredName: String? = null
        var numOfArgs = 0
        var hasOptionalArgs = false
        var codeText: String? = null
        var idx = 0
        for (pos in 0..3) {
            if (idx >= parameterSize) break
            val (type, text) = parameterTypeAndContent[idx]
            when (pos) {
                0 -> declaredName = text
                1 -> if (type != LArgumentType.OPTIONAL) continue
                else {
                    numOfArgs = text.toIntOrNull() ?: 0
                }

                2 -> if (type != LArgumentType.OPTIONAL) continue
                else {
                    hasOptionalArgs = true
                }

                3 -> codeText = text
            }
            idx++ // matched
        }
        return buildCommandSemantics(project, lookup, declaredName, codeText, numOfArgs, hasOptionalArgs)
    }

    private fun buildCommandSemantics(
        project: Project, lookup: LatexSemanticLookup,
        rawName: String?, codeRawText: String?, argCount: Int = 0, hasOptional: Boolean = false
    ): LSemanticCommand? {
        rawName ?: return null
        codeRawText ?: return null
        val name = rawName.removePrefix("\\") // remove the leading backslash
        val codeText = codeRawText.trim()
        if (argCount == 0 && PatternMagic.commandToken.matches(codeText)) {
            // this is an alias definition, e.g., \newcommand{\cmd}{\othercmd}
            val originalSemantic = lookup.lookupCommand(codeText)
            if (originalSemantic != null) {
                // use the original command semantics
                val description = "Alias for ${originalSemantic.displayName}"
                return LSemanticCommand(name, originalSemantic.dependency, originalSemantic.requiredContext, originalSemantic.arguments, description, originalSemantic.display)
            }
        }

        val codeElement = LatexPsiHelper.createFromText(codeRawText, project)
        val requiredContext = guessRequiredContext(codeElement, lookup)
        if (argCount == 0) {
            return LSemanticCommand(name, "", requiredContext, description = codeRawText, arguments = emptyList())
        }
        val argIntro = guessArgumentContextIntro(codeElement, argCount, lookup)
        val arguments = argIntro.mapIndexed { i, argIntro ->
            val type = if (i == 0 && hasOptional) LArgumentType.OPTIONAL else LArgumentType.REQUIRED
            LArgument("#${i + 1}", type, argIntro)
        }
        return LSemanticCommand(name, "", requiredContext, arguments, description = codeText)
    }

    private fun guessRequiredContext(definitionElement: PsiElement?, lookup: LatexSemanticLookup): LContextSet {
        definitionElement ?: return emptySet()
        val necessaryContexts = mutableSetOf<LatexContext>()
        LatexPsiUtil.traverseRecordingContextIntro(definitionElement, lookup) { e, introList ->
            val requiredContext: LContextSet? = when (e) {
                is LatexCommands -> lookup.lookupCommand(e.name ?: "")?.requiredContext
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

    private fun guessArgumentContextIntro(codeElement: PsiElement, argCount: Int, lookup: LatexSemanticLookup): List<LatexContextIntro> {
        if (argCount <= 0) return emptyList()
        val contextIntroList = Array(argCount) { LatexContextIntro.inherit() }
        LatexPsiUtil.traverseRecordingContextIntro(codeElement, lookup) traverse@{ e, introList ->
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
        return contextIntroList.asList()
    }

    fun mergeCommand(old: LSemanticCommand, new: LSemanticCommand): LSemanticCommand {
        if (old != new) { // whether the commands are the same
            return new
        }
        val arguments = new.arguments.ifEmpty {
            old.arguments // if the new command has no arguments, keep the old ones
        }
        val description = new.description.ifEmpty {
            old.description // if the new command has no description, keep the old one
        }
        val display = new.display ?: old.display
        val requiredContext = new.requiredContext.ifEmpty {
            old.requiredContext // if the new command has no required context, keep the old one
        }
        return LSemanticCommand(
            new.name, new.dependency,
            requiredContext = requiredContext,
            arguments = arguments,
            description = description,
            display = display
        )
    }

    fun mergeCmdDefinition(old: SourcedCmdDefinition, new: SourcedCmdDefinition): SourcedCmdDefinition {
        val cmd = mergeCommand(old.entity, new.entity)
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        return SourcedCmdDefinition(cmd, pointer, DefinitionSource.Merged)
    }

    fun mergeEnvDefinition(old: SourcedEnvDefinition, new: SourcedEnvDefinition): SourcedEnvDefinition {
        val env = new.entity
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        return SourcedEnvDefinition(env, pointer, DefinitionSource.Merged)
    }

    fun mergeDefinition(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        if (old.entity != new.entity) {
            // TODO: change to log after testing
            println("Command override: $old and $new")
        }
        // do not override primitive definitions
        if (old.source == DefinitionSource.Primitive) return old
        if (new.source == DefinitionSource.Primitive) return new
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

interface DefinitionBundle : LatexSemanticLookup {
    fun findCmdDef(name: String): SourcedCmdDefinition? {
        return findDefinition(name) as? SourcedCmdDefinition
    }

    fun findEnvDef(name: String): SourcedEnvDefinition? {
        return findDefinition(name) as? SourcedEnvDefinition
    }

    fun findDefinition(name: String): SourcedDefinition?

    override fun lookupCommand(name: String): LSemanticCommand? {
        return findCmdDef(name.removePrefix("\\"))?.entity
    }

    override fun lookupEnv(name: String): LSemanticEnv? {
        return findEnvDef(name)?.entity
    }

    fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>)

    fun sourcedDefinitions(): Collection<SourcedDefinition> {
        val nameMap = mutableMapOf<String, SourcedDefinition>()
        val includedPackages = mutableSetOf<String>()
        appendDefinitions(nameMap, includedPackages)
        return nameMap.values
    }
}

open class CompositeDefinitionBundle(
    val introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    val directDependencies: List<DefinitionBundle> = emptyList(),
) : DefinitionBundle {

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
        for (dep in directDependencies) {
            dep.appendDefinitions(nameMap, includedPackages)
        }
        for (sourcedDef in introducedDefinitions.values) {
            nameMap.merge(sourcedDef.entity.name, sourcedDef, LatexDefinitionUtil::mergeDefinition)
        }
    }

    override fun findDefinition(name: String): SourcedDefinition? {
        return introducedDefinitions[name] ?: directDependencies.asReversed().firstNotNullOfOrNull {
            it.findDefinition(name)
        }
    }
}

open class CachedCompositeDefinitionBundle(
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<DefinitionBundle> = emptyList()
) : CompositeDefinitionBundle(introducedDefinitions, directDependencies) {
    val allNameLookup: Map<String, SourcedDefinition> by lazy {
        buildMap {
            // let us cache the full lookup map since a package can be used frequently
            appendDefinitions(this, mutableSetOf())
        }
    }

    final override fun sourcedDefinitions(): Collection<SourcedDefinition> {
        return allNameLookup.values
    }
}

class LibDefinitionBundle(
    val libName: String,
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<LibDefinitionBundle> = emptyList(),
    val allLibraries: Set<String> = setOf(libName)
) : CachedCompositeDefinitionBundle(introducedDefinitions, directDependencies) {

    override fun toString(): String {
        return "Lib($libName, #defs=${introducedDefinitions.size})"
    }

    override fun findDefinition(name: String): SourcedDefinition? {
        // load the cached full map
        return allNameLookup[name]
    }

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
        if (!includedPackages.add(libName)) return // do not process the same package twice
        super.appendDefinitions(nameMap, includedPackages)
    }
}

class FilesetDefinitionBundle(
    customDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    libraryBundles: List<LibDefinitionBundle> = emptyList()
) : CachedCompositeDefinitionBundle(customDefinitions, libraryBundles)

/**
 * Command definition service for a single LaTeX package (`.cls` or `.sty` file).
 */
@Service(Service.Level.PROJECT)
class PackageDefinitionService(
    val project: Project
) : AbstractBlockingCacheService<String, LibDefinitionBundle>() {

    fun invalidateCache() {
        clearAllCache()
    }

    private fun processPsiCommandDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val definitions = LatexDefinitionUtil.collectDefinitionsInLib(libInfo, project)
        for (def in definitions) {
            currentSourcedDefinitions.merge(def.entity.name, def, LatexDefinitionUtil::mergeDefinition)
        }
    }

    private fun processPsiEnvironmentDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
    }

    private fun processExternalDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val scope = GlobalSearchScope.fileScope(project, libInfo.location)
        // TODO: we need more efficient external command indexing, the current one is too slow
        // Alternatively,
//        val externalIndexDefinitions = LatexExternalCommandIndex.getAllKeys(scope)
//        externalIndexDefinitions.size
//        for (key in externalIndexDefinitions) {
//            val documentation = LatexExternalCommandIndex.getValuesByKey(key, scope).lastOrNull() ?: continue
//            val name = key.removePrefix("\\") // remove the leading backslash
//            val sourcedDef = SourcedCmdDefinition(
//                LSemanticCommand(name, libInfo.name, description = documentation),
//                null,
//                DefinitionSource.Package
//            )
//            currentSourcedDefinitions.merge(name, sourcedDef, LatexDefinitionUtil::mergeDefinition)
//        }
    }

    private fun computeDefinitionsRecur(
        pkgName: String, processedPackages: MutableSet<String> // to prevent loops
    ): LibDefinitionBundle {
        if (pkgName.isEmpty()) {
            return baseLibBundle
        }
        getTimedValue(pkgName)?.takeIf { it.isNotExpired(expirationInMs) }?.let { return it.value }
        if (!processedPackages.add(pkgName)) {
            Log.warn("Recursive package dependency detected for package [$pkgName] !")
            return LibDefinitionBundle(pkgName)
        }
        val libInfo = LatexLibraryStructure.getLibraryInfo(pkgName, project)
        val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
        processPredefinedCommandsAndEnvironments(pkgName, currentSourcedDefinitions)

        val directDependencies: List<LibDefinitionBundle>
        val includedPackages: Set<String>
        if (libInfo != null) {
            val directDependencyNames = libInfo.directDependencies
            includedPackages = mutableSetOf(pkgName) // do not process the same package twice
            /*
            Let us be careful not to process the same package twice, note that we may have to following:
                    A -> B1 -> C
                      -> B2 -> C,D
             */
            directDependencies = mutableListOf()
            for (dependency in directDependencyNames) {
                if (!includedPackages.add(dependency)) {
                    continue
                }
                // recursively compute the command definitions for the dependency
                val depBundle = getValueOrNull(dependency) ?: computeDefinitionsRecur(dependency, processedPackages)
                directDependencies.add(depBundle)
                includedPackages.addAll(depBundle.allLibraries)
            }

            processPsiCommandDefinitions(libInfo, currentSourcedDefinitions)
            processExternalDefinitions(libInfo, currentSourcedDefinitions)
        }
        else {
            directDependencies = emptyList()
            includedPackages = setOf(pkgName)
        }

        val result = LibDefinitionBundle(pkgName, currentSourcedDefinitions, directDependencies, includedPackages)
        putValue(pkgName, result)
        return result
    }

    /**
     * Computes the command definitions for a given package (`.cls` or `.sty` file).
     *
     * @param key the name of the package (with the file extension)
     */
    override fun computeValue(key: String, oldValue: LibDefinitionBundle?): LibDefinitionBundle {
        val start = System.currentTimeMillis()
        val result = computeDefinitionsRecur(key, mutableSetOf())
        val buildTime = System.currentTimeMillis() - start
        countOfBuilds.incrementAndGet()
        totalBuildTime.addAndGet(buildTime)
        return result
    }

    /**
     * Should be long, since packages do not change.
     */
    val expirationInMs: Long = 100000L

    fun getLibBundle(libName: String): LibDefinitionBundle {
        return getOrComputeNow(libName, expirationInMs)
    }

    companion object {
        fun getInstance(project: Project): PackageDefinitionService {
            return project.service()
        }

        val countOfBuilds = AtomicInteger(0)
        val totalBuildTime = AtomicLong(0)

        val baseLibBundle: LibDefinitionBundle by lazy {
            // return the hard-coded basic commands
            val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
            processPredefinedCommandsAndEnvironments("", currentSourcedDefinitions)
            // overwrite the definitions with the primitive commands
            PredefinedBasicCommands.primitives.forEach {
                currentSourcedDefinitions[it.name] = SourcedCmdDefinition(it, null, DefinitionSource.Primitive)
            }
            LibDefinitionBundle("", currentSourcedDefinitions)
        }

        private fun processPredefinedCommandsAndEnvironments(name: String, defMap: MutableMap<String, SourcedDefinition>) {
            AllPredefinedCommands.packageToCommands[name]?.forEach { command ->
                defMap[command.name] = SourcedCmdDefinition(command, null, DefinitionSource.Predefined)
            }
            AllPredefinedEnvironments.packageToEnvironments[name]?.forEach { env ->
                defMap[env.name] = SourcedEnvDefinition(env, null, DefinitionSource.Predefined)
            }
        }
    }
}

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.util.magic.CommandMagic], [nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 *   * those indexed by stub-based index [NewDefinitionIndex]
 */
@Service(Service.Level.PROJECT)
class LatexDefinitionService(
    val project: Project,
) : AbstractBlockingCacheService<Fileset, FilesetDefinitionBundle>() {

    var expirationInMs: Long = 100L

    override fun computeValue(key: Fileset, oldValue: FilesetDefinitionBundle?): FilesetDefinitionBundle {
        val startTime = System.currentTimeMillis()

        // packages do not need indexing
        val packageService = PackageDefinitionService.getInstance(project)
        val libraries = ArrayList<LibDefinitionBundle>(key.libraries.size + 1)
        libraries.add(packageService.getLibBundle("")) // add the default commands
        key.libraries.mapTo(libraries) { packageService.getLibBundle(it) }

        if (DumbService.isDumb(project)) return FilesetDefinitionBundle(emptyMap(), libraries)

        val customCommands = mutableMapOf<String, SourcedDefinition>()
        val bundle = FilesetDefinitionBundle(customCommands, libraryBundles = libraries)

        val projectFileIndex = ProjectFileIndex.getInstance(project)
        // a building placeholder for the bundle to make lookups work
        for (file in key.files) {
            if (!projectFileIndex.isInProject(file)) continue
            val commandDefinitions = LatexDefinitionUtil.collectCustomDefinitions(file, project, bundle)
            for (sourcedDef in commandDefinitions) {
                // always overwrite for user-defined commands
                customCommands.put(sourcedDef.entity.name, sourcedDef)
            }
        }

        countOfBuilds.incrementAndGet()
        totalBuildTime.addAndGet(System.currentTimeMillis() - startTime)
        return bundle
    }

    fun getFilesetBundle(fileset: Fileset): DefinitionBundle {
        return getOrComputeNow(fileset, expirationInMs)
    }

    fun getFilesetBundlesMerged(v: VirtualFile): DefinitionBundle {
        val filesetData = LatexProjectStructure.getFilesetDataFor(v, project) ?: return PackageDefinitionService.baseLibBundle
        if (filesetData.filesets.size == 1) return getFilesetBundle(filesetData.filesets.first())
        return union(filesetData.filesets.map { getFilesetBundle(it) })
    }

    fun resolveCommandDef(v: VirtualFile, commandName: String): SourcedCmdDefinition? {
        return resolveDef(v, commandName.removePrefix("\\")) as? SourcedCmdDefinition
    }

    fun resolveEnvDef(v: VirtualFile, envName: String): SourcedEnvDefinition? {
        return resolveDef(v, envName) as? SourcedEnvDefinition
    }

    fun resolveDef(v: VirtualFile, name: String): SourcedDefinition? {
        val filesetData = LatexProjectStructure.getFilesetDataFor(v, project) ?: return resolvePredefinedDef(name)
        return filesetData.filesets.firstNotNullOfOrNull {
            getFilesetBundle(it).findDefinition(name)
        }
    }

    fun resolveDefInProject(name: String): SourcedDefinition? {
        val pf = LatexProjectStructure.getFilesets(project) ?: return resolvePredefinedDef(name)
        return pf.filesets.firstNotNullOfOrNull {
            getFilesetBundle(it).findDefinition(name)
        }
    }

    companion object {
        fun getInstance(project: Project): LatexDefinitionService {
            return project.service()
        }

        val countOfBuilds = AtomicInteger(0)
        val totalBuildTime = AtomicLong(0)

        fun resolvePredefinedDef(name: String): SourcedDefinition? {
            return resolvePredefinedCommandDef(name) ?: resolvePredefinedEnvDef(name)
        }

        fun resolvePredefinedCommandDef(name: String): SourcedCmdDefinition? {
            return AllPredefinedCommands.simpleNameLookup[name]?.let { cmd ->
                return SourcedCmdDefinition(cmd, null, DefinitionSource.Predefined)
            }
        }

        fun resolvePredefinedEnvDef(envName: String): SourcedEnvDefinition? {
            return AllPredefinedEnvironments.simpleNameLookup[envName]?.let { env ->
                SourcedEnvDefinition(env, null, DefinitionSource.Predefined)
            }
        }

        fun union(list: List<DefinitionBundle>): DefinitionBundle {
            if (list.size == 1) return list[0]
            return CompositeDefinitionBundle(directDependencies = list)
        }
    }
}