package nl.hannahsten.texifyidea.index

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.lang.predefined.PredefinedPrimitives
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.settings.sdk.SdkPath
import nl.hannahsten.texifyidea.util.AbstractBackgroundCacheService
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.isTestProject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class SourcedDefinition(
    val entity: LSemanticEntity,
    val definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?,
    val source: DefinitionSource = DefinitionSource.UserDefined
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourcedDefinition

        return entity.name == other.entity.name
    }

    override fun hashCode(): Int = entity.name.hashCode()

    override fun toString(): String = buildString {
        append("Def($entity, ${source.name}")
        if (definitionCommandPointer != null) {
            append(", with PSI")
        }
        append(")")
    }

    enum class DefinitionSource {
        Primitive,
        Predefined,
        LibraryScan,
        UserDefined
    }
}

interface DefinitionBundle : LatexSemanticsLookup {
    fun findDefinition(name: String): SourcedDefinition?

    override fun lookup(name: String): LSemanticEntity? = findDefinition(name)?.entity

    fun appendDefinitionsTo(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>)

    fun sourcedDefinitions(): Collection<SourcedDefinition> {
        val nameMap = mutableMapOf<String, SourcedDefinition>()
        val includedPackages = mutableSetOf<LatexLib>()
        appendDefinitionsTo(nameMap, includedPackages)
        return nameMap.values
    }

    fun containsLibrary(lib: LatexLib): Boolean
}

abstract class MergedDefinitionBundle(
    val introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    val directDependencies: List<DefinitionBundle> = emptyList(),
) : CachedLatexSemanticsLookup(), DefinitionBundle {
    override fun appendDefinitionsTo(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        for (dep in directDependencies) {
            dep.appendDefinitionsTo(nameMap, includedPackages)
        }
        for (sourcedDef in introducedDefinitions.values) {
            nameMap.merge(sourcedDef.entity.name, sourcedDef, LatexDefinitionUtil::mergeDefinition)
        }
    }

    override fun containsLibrary(lib: LatexLib): Boolean = directDependencies.any { it.containsLibrary(lib) }
}

open class CachedMergedDefinitionBundle(
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<DefinitionBundle> = emptyList()
) : MergedDefinitionBundle(introducedDefinitions, directDependencies) {
    private val simpleNameLookup: Map<String, SourcedDefinition> by lazy {
        buildMap {
            // let us cache the full lookup map since a package can be used frequently
            appendDefinitionsTo(this, mutableSetOf())
        }
    }
    private val allEntities: Set<LSemanticEntity> by lazy {
        buildSet {
            simpleNameLookup.values.mapTo(this) { it.entity }
        }
    }

    override fun allEntitiesSeq(): Sequence<LSemanticEntity> = allEntities.asSequence()

    final override fun sourcedDefinitions(): Collection<SourcedDefinition> = simpleNameLookup.values

    final override fun findDefinition(name: String): SourcedDefinition? {
        // this would load the cached full map, but necessary
        return simpleNameLookup[name]
    }
}

class LibDefinitionBundle(
    val libName: LatexLib,
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<LibDefinitionBundle> = emptyList(),
    val allLibraries: Set<LatexLib> = setOf(libName)
) : CachedMergedDefinitionBundle(introducedDefinitions, directDependencies) {

    override fun containsLibrary(lib: LatexLib): Boolean = allLibraries.contains(lib)

    override fun toString(): String = "Lib($libName, #defs=${introducedDefinitions.size})"

    override fun appendDefinitionsTo(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        if (!includedPackages.add(libName)) return // do not process the same package twice
        super.appendDefinitionsTo(nameMap, includedPackages)
    }
}

/**
 * Cache key for library definitions, combining SDK path and library name.
 * This ensures different LaTeX distributions in different modules get separate caches.
 *
 * @property sdkPath The SDK home path or resolved kpsewhich path (see [SdkPath])
 * @property libName The library name (e.g., amsmath, hyperref)
 */
data class LibDefinitionCacheKey(val sdkPath: SdkPath, val libName: LatexLib)

/**
 * Command definition service for a single LaTeX package (`.cls` or `.sty` file).
 *
 * The cache is keyed by [LibDefinitionCacheKey] to support different LaTeX distributions
 * in different modules.
 *
 * @author Ezrnest
 */
@Suppress("unused")
@Service(Service.Level.PROJECT)
class LatexLibraryDefinitionService(
    val project: Project
) : AbstractBlockingCacheService<LibDefinitionCacheKey, LibDefinitionBundle>() {

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

    private fun processDtxDefinitions(
        lib: LatexLib, currentSourcedDefinitions: MutableMap<String, SourcedDefinition>, assignedLib: LatexLib = lib
    ) {
        LatexRegexBasedIndex.processDtxDefinitions(lib, project) { sd ->
            val entity =
                if (sd.isEnv) LSemanticEnv(sd.name, assignedLib, arguments = sd.arguments, description = sd.description)
                else LSemanticCommand(sd.name, assignedLib, arguments = sd.arguments, description = sd.description)
            val sourced = SourcedDefinition(entity, null, DefinitionSource.LibraryScan)
            currentSourcedDefinitions.merge(sd.name, sourced, LatexDefinitionUtil::mergeDefinition)
        }
    }

    private fun processExternalDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val lib = libInfo.name
        processDtxDefinitions(lib, currentSourcedDefinitions)

        val file = libInfo.location
        for (name in LatexRegexBasedIndex.getCommandDefinitions(file, project)) {
            if (currentSourcedDefinitions.containsKey(name)) continue // do not overwrite existing definitions, as the regex-based index is fallible
            val sourcedDef = SourcedDefinition(
                LSemanticCommand(name, lib),
                null,
                DefinitionSource.LibraryScan
            )
            currentSourcedDefinitions[name] = sourcedDef
        }
        for (name in LatexRegexBasedIndex.getEnvironmentDefinitions(file, project)) {
            if (currentSourcedDefinitions.containsKey(name)) continue // do not overwrite existing definitions, as the regex-based index is fallible
            val sourcedDef = SourcedDefinition(
                LSemanticEnv(name, lib),
                null,
                DefinitionSource.LibraryScan
            )
            currentSourcedDefinitions[name] = sourcedDef
        }
    }

    private fun computeDefinitionsRecur(
        sdkPath: String, pkgName: LatexLib, processedPackages: MutableSet<LatexLib>, contextFile: VirtualFile?
    ): LibDefinitionBundle {
        val cacheKey = LibDefinitionCacheKey(sdkPath, pkgName)
        getTimedValue(cacheKey)?.takeIf { it.isNotExpired(libExpiration) }?.let { return it.value }
        if (!processedPackages.add(pkgName)) {
            Log.debug("Recursive package dependency detected for package [$pkgName] !")
            return LibDefinitionBundle(pkgName)
        }
        val libInfo = LatexLibraryStructureService.getInstance(project).getLibraryInfo(pkgName, contextFile)
        val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
        processPredefinedCommandsAndEnvironments(pkgName, currentSourcedDefinitions)

        val directDependencies: List<LibDefinitionBundle>
        val includedPackages: Set<LatexLib>
        if (libInfo != null) {
            val directDependencyNames = libInfo.directDependencies
            includedPackages = mutableSetOf(pkgName) // do not process the same package twice
            /*
            Let us be careful not to process the same package twice, note that we may have to following:
                    A -> B1 -> C
                      -> B2 -> C,D
             */
            directDependencies = mutableListOf()
            for (fileName in directDependencyNames) {
                val dependency = LatexLib.fromFileName(fileName)
                if (!includedPackages.add(dependency)) {
                    continue
                }
                // recursively compute the command definitions for the dependency
                val depCacheKey = LibDefinitionCacheKey(sdkPath, dependency)
                val depBundle = getValueOrNull(depCacheKey) ?: computeDefinitionsRecur(sdkPath, dependency, processedPackages, contextFile)
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
        putValue(cacheKey, result)
        return result
    }

    /**
     * Computes the command definitions for a given package (`.cls` or `.sty` file).
     *
     * @param key The cache key containing the SDK path and library name (with file extension).
     */
    override fun computeValue(key: LibDefinitionCacheKey, oldValue: LibDefinitionBundle?): LibDefinitionBundle {
        val (sdkPath, libName) = key
        return performanceTracker.track {
            if (libName == LatexLib.BASE) {
                computeBaseBundle()
            }
            else {
                computeDefinitionsRecur(sdkPath, libName, mutableSetOf(), null)
            }
        }
    }

    private fun computeBaseBundle(): LibDefinitionBundle {
        val baseDtxFiles = listOf(
            // all .dtx files from LaTeX2e sources from latexrelease.ins, see https://ctan.org/tex-archive/macros/latex/base
            "ltclass", "ltvers", "latexrelease", "ltdirchk", "ltplain", "ltluatex", "ltexpl", "ltdefns",
            "ltcmd", "lthooks", "ltcmdhooks", "ltsockets", "lttemplates", "ltalloc", "ltcntrl", "lterror",
            "ltpar", "ltpara", "ltmeta", "ltspace", "ltlogos", "ltfiles", "ltoutenc", "ltcounts", "ltlength",
            "ltfssbas", "ltfssaxes", "ltfsstrc", "ltfssdcl", "ltfssini", "fontdef", "ltfntcmd", "lttextcomp",
            "ltpageno", "ltxref", "ltproperties", "ltmiscen", "ltmath", "ltlists", "ltboxes", "lttab",
            "ltpictur", "ltthm", "ltsect", "ltfloat", "ltidxglo", "ltbibl", "ltmarks", "ltpage",
            "ltfilehook", "ltshipout", "ltoutput", "ltfsscmp", "ltfinal",
            "latexrelease",
            "exscale", "newlfont", "inputenc", "alltt" // some additional  files
        )
        val defs = predefinedBaseLibBundle.introducedDefinitions.toMutableMap()
        for (dtx in baseDtxFiles) {
            // those dtx files are scanned and stored as packages, see LatexDtxDefinitionDataIndexer
            processDtxDefinitions(LatexLib.Package(dtx), defs, LatexLib.BASE) // assign all definitions to the base package, rather than the dtx file
        }
        return LibDefinitionBundle(LatexLib.BASE, defs)
    }

    /**
     * Get the definition bundle for a library, using the SDK resolved from the given file context.
     *
     * @param libName The library name
     * @param sdkPath Identifies to which LaTeX installation the library belongs
     */
    fun getLibBundle(libName: LatexLib, sdkPath: SdkPath): LibDefinitionBundle = getOrComputeNow(LibDefinitionCacheKey(sdkPath, libName), libExpiration)

    /**
     * Get the definition bundle for a library, using the SDK resolved from the given file context.
     */
    fun getLibBundle(libName: LatexLib, contextFile: PsiFile): LibDefinitionBundle = getLibBundle(libName, LatexSdkUtil.resolveSdkPath(contextFile.virtualFile, project) ?: "")

    /**
     * Get the definition bundle for a library, using project SDK only (no file context).
     */
    fun getLibBundle(libName: LatexLib): LibDefinitionBundle = getLibBundle(libName, "")

    fun getLibBundle(fileName: String, sdkPath: SdkPath): LibDefinitionBundle = getLibBundle(LatexLib.fromFileName(fileName), sdkPath)

    fun getBaseBundle(): LibDefinitionBundle = getLibBundle(LatexLib.BASE)

    companion object {

        val libExpiration: Duration = 1.hours

        fun getInstance(project: Project): LatexLibraryDefinitionService = project.service()

        val performanceTracker = SimplePerformanceTracker()

        val predefinedBaseLibBundle: LibDefinitionBundle by lazy {
            // return the hard-coded basic commands
            val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
            val lib = LatexLib.BASE
            processPredefinedCommandsAndEnvironments(lib, currentSourcedDefinitions)

            // overwrite the definitions with the primitive commands
            PredefinedPrimitives.allCommands.forEach {
                currentSourcedDefinitions[it.name] = SourcedDefinition(it, null, DefinitionSource.Primitive)
            }

            LibDefinitionBundle(lib, currentSourcedDefinitions)
        }

        private fun processPredefinedCommandsAndEnvironments(name: LatexLib, defMap: MutableMap<String, SourcedDefinition>) {
            AllPredefined.findByLib(name).forEach { entity ->
                defMap[entity.name] = SourcedDefinition(entity, null, DefinitionSource.Predefined)
            }
        }
    }
}

class WorkingFilesetDefinitionBundle(
    private val libraryBundles: List<LibDefinitionBundle> = emptyList(),
) : CachedLatexSemanticsLookup(), DefinitionBundle {
    private val allNameLookup: MutableMap<String, SourcedDefinition> = mutableMapOf()

    init {
        val includedPackages = mutableSetOf<LatexLib>()
        for (dep in libraryBundles) {
            dep.appendDefinitionsTo(allNameLookup, includedPackages)
        }
    }

    override fun appendDefinitionsTo(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        nameMap.putAll(allNameLookup)
    }

    override fun findDefinition(name: String): SourcedDefinition? = allNameLookup[name]

    override fun allEntitiesSeq(): Sequence<LSemanticEntity> = allNameLookup.values.asSequence().map { it.entity }

    /**
     * Overwrite the definition for a custom command or environment.
     */
    fun addCustomDefinition(def: SourcedDefinition) {
        // override if user-defined
        if (def.source == DefinitionSource.UserDefined) {
            allNameLookup[def.entity.name] = def
        }
        else {
            allNameLookup.merge(def.entity.name, def, LatexDefinitionUtil::mergeDefinition)
        }
    }

    override fun sourcedDefinitions(): Collection<SourcedDefinition> = allNameLookup.values

    override fun containsLibrary(lib: LatexLib): Boolean = libraryBundles.any { it.containsLibrary(lib) }
}

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.lang.predefined.AllPredefined].
 *   * those indexed by stub-based index [NewDefinitionIndex]
 *   * those indexed by file-based index in both `.sty` and `.dtx` files:
 *   [LatexRegexBasedIndex]
 *
 *
 *
 * @author Ezrnest
 */
@Suppress("unused")
@Service(Service.Level.PROJECT)
class LatexDefinitionService(
    val project: Project, scope: CoroutineScope
) : AbstractBackgroundCacheService<Fileset, DefinitionBundle>(scope) {

    private fun computeValue(key: Fileset): DefinitionBundle = performanceTracker.track {
        // packages first
        val packageService = LatexLibraryDefinitionService.getInstance(project)
        val contextFile = key.root
        val libraries = ArrayList<LibDefinitionBundle>(key.libraries.size + 1)
        libraries.add(packageService.getBaseBundle()) // add the default commands
        // Depending on number of imports, getLibBundle could be called thousands of times per letter typed, so careful about performance here
        val sdkPath = LatexSdkUtil.resolveSdkPath(contextFile, project) ?: ""
        key.libraries.mapTo(libraries) { packageService.getLibBundle(it, sdkPath) }

        val bundle = WorkingFilesetDefinitionBundle(libraries)

        val projectFileIndex = ProjectFileIndex.getInstance(project)
        // a building placeholder for the bundle to make lookups work
        for (file in key.files) {
            if (!projectFileIndex.isInProject(file)) continue
            LatexDefinitionUtil.collectCustomDefinitions(file, project, bundle)
        }
        bundle
    }

    override suspend fun computeValueSuspend(key: Fileset, oldValue: DefinitionBundle?): DefinitionBundle = smartReadAction(project) {
        computeValue(key)
    }

    fun getDefBundleForFileset(fileset: Fileset): DefinitionBundle = getAndComputeLater(fileset, expirationTime, LatexLibraryDefinitionService.predefinedBaseLibBundle)

    fun getDefBundleForFilesetOrNull(fileset: Fileset): DefinitionBundle? = getAndComputeLater(fileset, expirationTime)

    /**
     * Get the merged definition bundle for the given [psiFile], which may belong to multiple filesets.
     *
     */
    fun getDefBundlesMerged(psiFile: PsiFile, default: DefinitionBundle = LatexLibraryDefinitionService.predefinedBaseLibBundle): DefinitionBundle {
        val filesetData = LatexProjectStructure.getFilesetDataFor(psiFile) ?: return LatexLibraryDefinitionService.predefinedBaseLibBundle
        if (filesetData.filesets.size == 1) return getDefBundleForFileset(filesetData.filesets.first())
        return union(filesetData.filesets.map { getDefBundleForFileset(it) })
    }

    fun getDefBundlesMergedOrNull(psiFile: PsiFile): DefinitionBundle? {
        val filesetData = LatexProjectStructure.getFilesetDataFor(psiFile) ?: return null
        if (filesetData.filesets.size == 1) return getDefBundleForFilesetOrNull(filesetData.filesets.first())
        val bundles = filesetData.filesets.mapNotNull { getDefBundleForFilesetOrNull(it) }
        if (bundles.isEmpty()) return null
        return union(bundles)
    }

    fun resolveCommandDef(v: VirtualFile, commandName: String): SourcedDefinition? = resolveDef(v, commandName.removePrefix("\\"))

    fun resolveEnvDef(v: VirtualFile, envName: String): SourcedDefinition? = resolveDef(v, envName)

    fun resolveDef(v: VirtualFile, name: String): SourcedDefinition? {
        val filesetData = LatexProjectStructure.getFilesetDataFor(v, project) ?: return resolvePredefined(name)
        return filesetData.filesets.firstNotNullOfOrNull {
            getDefBundleForFileset(it).findDefinition(name)
        }
    }

    fun resolveDefInProject(name: String): SourcedDefinition? {
        val pf = LatexProjectStructure.getFilesets(project) ?: return resolvePredefined(name)
        return pf.filesets.firstNotNullOfOrNull {
            getDefBundleForFileset(it).findDefinition(name)
        }
    }

    fun requestRefresh() {
        if (isTestProject()) return // refresh in unit tests is done manually
        coroutineScope.launch {
            ensureRefreshAll()
        }
    }

    /**
     * Update the filesets and refresh all definitions.
     */
    suspend fun ensureRefreshAll() {
        val filesets = LatexProjectStructure.updateFilesetsSuspend(project)
        refreshAll(filesets.filesets)
    }

    suspend fun ensureRefreshFileset(projectFilesets: LatexProjectFilesets) {
        refreshAll(projectFilesets.filesets)
    }

    class CompositeOverridingDefinitionBundle(
        val bundles: List<DefinitionBundle>
    ) : CachedLatexSemanticsLookup(), DefinitionBundle {
        override fun findDefinition(name: String): SourcedDefinition? = bundles.firstNotNullOfOrNull { it.findDefinition(name) }

        override fun appendDefinitionsTo(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
            for (bundle in bundles) {
                bundle.sourcedDefinitions().forEach {
                    nameMap[it.entity.name] = it
                }
            }
        }

        override fun containsLibrary(lib: LatexLib): Boolean = bundles.any { it.containsLibrary(lib) }

        override fun allEntitiesSeq(): Sequence<LSemanticEntity> = bundles.asSequence().flatMap { it.allEntitiesSeq() }
    }

    companion object {

        val expirationTime: Duration
            get() = TexifySettings.getState().filesetExpirationTimeMs.milliseconds

        fun getInstance(project: Project): LatexDefinitionService = project.service()

        val performanceTracker = SimplePerformanceTracker()

        fun resolvePredefined(name: String): SourcedDefinition? = AllPredefined.lookup(name)?.let {
            SourcedDefinition(it, null, DefinitionSource.Predefined)
        }

        fun baseBundle(): DefinitionBundle = LatexLibraryDefinitionService.predefinedBaseLibBundle

        fun union(list: List<DefinitionBundle>): DefinitionBundle {
            if (list.size == 1) return list[0]
            return CompositeOverridingDefinitionBundle(list)
        }

        /**
         * Gets the definition bundle for the given element, a shortcut for first getting the service and then `getDefBundlesMerged`.
         *
         * @see getDefBundlesMerged
         */
        fun getBundleFor(element: PsiElement): DefinitionBundle {
            val file = element.containingFile ?: return LatexLibraryDefinitionService.predefinedBaseLibBundle
            return getInstance(file.project).getDefBundlesMerged(file)
        }

        fun resolveEnv(env: LatexEnvironment): LSemanticEnv? {
            val bundle = getBundleFor(env)
            val name = env.getEnvironmentName()
            return bundle.findDefinition(name)?.entity as? LSemanticEnv
        }

        fun resolveCommand(command: LatexCommands): LSemanticCommand? {
            val bundle = getBundleFor(command)
            val name = command.nameWithoutSlash ?: return null
            return bundle.findDefinition(name)?.entity as? LSemanticCommand
        }
    }
}