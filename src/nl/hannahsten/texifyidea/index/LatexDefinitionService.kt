package nl.hannahsten.texifyidea.index

import arrow.atomic.AtomicLong
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
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.index.file.LatexRegexBasedIndex
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.lang.predefined.PredefinedPrimitives
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.nameWithoutSlash
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.AbstractBackgroundCacheService
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import java.util.concurrent.atomic.AtomicInteger
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
        LibraryScan,
        UserDefined
    }
}

interface DefinitionBundle : LatexSemanticsLookup {
    fun findDefinition(name: String): SourcedDefinition?

    override fun lookup(name: String): LSemanticEntity? {
        return findDefinition(name)?.entity
    }

    fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>)

    fun sourcedDefinitions(): Collection<SourcedDefinition> {
        val nameMap = mutableMapOf<String, SourcedDefinition>()
        val includedPackages = mutableSetOf<LatexLib>()
        appendDefinitions(nameMap, includedPackages)
        return nameMap.values
    }
}

abstract class MergedDefinitionBundle(
    val introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    val directDependencies: List<DefinitionBundle> = emptyList(),
) : DefinitionBundle {
    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        for (dep in directDependencies) {
            dep.appendDefinitions(nameMap, includedPackages)
        }
        for (sourcedDef in introducedDefinitions.values) {
            nameMap.merge(sourcedDef.entity.name, sourcedDef, LatexDefinitionUtil::mergeDefinition)
        }
    }
}

open class CachedMergedDefinitionBundle(
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<DefinitionBundle> = emptyList()
) : MergedDefinitionBundle(introducedDefinitions, directDependencies) {
    private val allNameLookup: Map<String, SourcedDefinition> by lazy {
        buildMap {
            // let us cache the full lookup map since a package can be used frequently
            appendDefinitions(this, mutableSetOf())
        }
    }

    final override fun sourcedDefinitions(): Collection<SourcedDefinition> {
        return allNameLookup.values
    }

    final override fun findDefinition(name: String): SourcedDefinition? {
        // this would load the cached full map, but necessary
        return allNameLookup[name]
    }
}

class LibDefinitionBundle(
    val libName: LatexLib,
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<LibDefinitionBundle> = emptyList(),
    val allLibraries: Set<LatexLib> = setOf(libName)
) : CachedMergedDefinitionBundle(introducedDefinitions, directDependencies) {

    override fun toString(): String {
        return "Lib($libName, #defs=${introducedDefinitions.size})"
    }

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        if (!includedPackages.add(libName)) return // do not process the same package twice
        super.appendDefinitions(nameMap, includedPackages)
    }
}

/**
 * Command definition service for a single LaTeX package (`.cls` or `.sty` file).
 *
 * @author Ezrnest
 */
@Service(Service.Level.PROJECT)
class LatexLibraryDefinitionService(
    val project: Project
) : AbstractBlockingCacheService<LatexLib, LibDefinitionBundle>() {

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

    private fun processExternalDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val file = libInfo.location
        for (name in LatexRegexBasedIndex.getCommandDefinitions(file, project)) {
            if(currentSourcedDefinitions.containsKey(name)) continue // do not overwrite existing definitions, as the regex-based index is fallible
            val sourcedDef = SourcedDefinition(
                LSemanticCommand(name, libInfo.name),
                null,
                DefinitionSource.LibraryScan
            )
            currentSourcedDefinitions.put(name, sourcedDef)
        }
        for (name in LatexRegexBasedIndex.getEnvironmentDefinitions(file, project)) {
            if(currentSourcedDefinitions.containsKey(name)) continue // do not overwrite existing definitions, as the regex-based index is fallible
            val sourcedDef = SourcedDefinition(
                LSemanticEnv(name, libInfo.name),
                null,
                DefinitionSource.LibraryScan
            )
            currentSourcedDefinitions.put(name, sourcedDef)
        }
    }

    private fun computeDefinitionsRecur(
        pkgName: LatexLib, processedPackages: MutableSet<LatexLib> // to prevent loops
    ): LibDefinitionBundle {
        if (pkgName.isDefault) {
            return baseLibBundle
        }
        getTimedValue(pkgName)?.takeIf { it.isNotExpired(libExpiration) }?.let { return it.value }
        if (!processedPackages.add(pkgName)) {
            Log.warn("Recursive package dependency detected for package [$pkgName] !")
            return LibDefinitionBundle(pkgName)
        }
        val libInfo = LatexLibraryStructureService.getInstance(project).getLibraryInfo(pkgName)
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
            for (name in directDependencyNames) {
                val dependency = LatexLib(name)
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
    override fun computeValue(key: LatexLib, oldValue: LibDefinitionBundle?): LibDefinitionBundle {
        val start = System.currentTimeMillis()
        val result = computeDefinitionsRecur(key, mutableSetOf())
        val buildTime = System.currentTimeMillis() - start
        countOfBuilds.incrementAndGet()
        totalTimeCost.addAndGet(buildTime)
        return result
    }

    /**
     * Should be long, since packages do not change much
     */

    fun getLibBundle(libName: LatexLib): LibDefinitionBundle {
        return getOrComputeNow(libName, libExpiration)
    }

    fun getLibBundle(libName: String): LibDefinitionBundle {
        return getLibBundle(LatexLib(libName))
    }

    companion object : SimplePerformanceTracker {

        val libExpiration: Duration = 1.hours

        fun getInstance(project: Project): LatexLibraryDefinitionService {
            return project.service()
        }

        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)

        val baseLibBundle: LibDefinitionBundle by lazy {

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
            AllPredefined.packageToEntities(name).forEach { entity ->
                defMap[entity.name] = SourcedDefinition(entity, null, DefinitionSource.Predefined)
            }
        }
    }
}

class WorkingFilesetDefinitionBundle(
    libraryBundles: List<LibDefinitionBundle> = emptyList(),
) : DefinitionBundle {
    private val allNameLookup: MutableMap<String, SourcedDefinition> = mutableMapOf()

    init {
        val includedPackages = mutableSetOf<LatexLib>()
        for (dep in libraryBundles) {
            dep.appendDefinitions(allNameLookup, includedPackages)
        }
    }

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
        nameMap.putAll(allNameLookup)
    }

    override fun findDefinition(name: String): SourcedDefinition? {
        return allNameLookup[name]
    }

    /**
     * Overwrite the definition for a custom command or environment.
     */
    fun addCustomDefinition(def: SourcedDefinition) {
        // always overwrite for user-defined commands
        allNameLookup[def.entity.name] = def
    }

    override fun sourcedDefinitions(): Collection<SourcedDefinition> {
        return allNameLookup.values
    }
}

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.lang.predefined.AllPredefined].
 *   * those indexed by stub-based index [NewDefinitionIndex]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 *
 *
 * @author Ezrnest
 */
@Service(Service.Level.PROJECT)
class LatexDefinitionService(
    val project: Project, scope: CoroutineScope
) : AbstractBackgroundCacheService<Fileset, DefinitionBundle>(scope) {

    private fun computeValue(key: Fileset, oldValue: DefinitionBundle?): DefinitionBundle {
        val startTime = System.currentTimeMillis()

        // packages first
        val packageService = LatexLibraryDefinitionService.getInstance(project)
        val libraries = ArrayList<LibDefinitionBundle>(key.libraries.size + 1)
        libraries.add(packageService.getLibBundle(LatexLib.BASE)) // add the default commands
        key.libraries.mapTo(libraries) { packageService.getLibBundle(it) }

        val bundle = WorkingFilesetDefinitionBundle(libraries)

        val projectFileIndex = ProjectFileIndex.getInstance(project)
        // a building placeholder for the bundle to make lookups work
        for (file in key.files) {
            if (!projectFileIndex.isInProject(file)) continue
            LatexDefinitionUtil.collectCustomDefinitions(file, project, bundle)
        }
        countOfBuilds.incrementAndGet()
        totalTimeCost.addAndGet(System.currentTimeMillis() - startTime)
        return bundle
    }

    override suspend fun computeValueSuspend(key: Fileset, oldValue: DefinitionBundle?): DefinitionBundle {
        return smartReadAction(project) {
            computeValue(key, oldValue)
        }
    }

    fun getDefBundleForFileset(fileset: Fileset): DefinitionBundle {
        return getAndComputeLater(fileset, expirationTime, LatexLibraryDefinitionService.baseLibBundle)
    }

    /**
     * Get the definition bundle for the given [psiFile],
     */
    fun getDefBundlesMerged(psiFile: PsiFile): DefinitionBundle {
        val filesetData = LatexProjectStructure.getFilesetDataFor(psiFile) ?: return LatexLibraryDefinitionService.baseLibBundle
        if (filesetData.filesets.size == 1) return getDefBundleForFileset(filesetData.filesets.first())
        return union(filesetData.filesets.map { getDefBundleForFileset(it) })
    }

    fun resolveCommandDef(v: VirtualFile, commandName: String): SourcedDefinition? {
        return resolveDef(v, commandName.removePrefix("\\"))
    }

    fun resolveEnvDef(v: VirtualFile, envName: String): SourcedDefinition? {
        return resolveDef(v, envName)
    }

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
        scheduleRefreshAll()
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
    ) : DefinitionBundle {
        override fun findDefinition(name: String): SourcedDefinition? {
            return bundles.firstNotNullOfOrNull { it.findDefinition(name) }
        }

        override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<LatexLib>) {
            for (bundle in bundles) {
                bundle.sourcedDefinitions().forEach {
                    nameMap[it.entity.name] = it
                }
            }
        }
    }

    companion object : SimplePerformanceTracker {

        val expirationTime: Duration
            get() = TexifySettings.getInstance().filesetExpirationTimeMs.milliseconds

        fun getInstance(project: Project): LatexDefinitionService {
            return project.service()
        }

        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)

        fun resolvePredefined(name: String): SourcedDefinition? {
            return AllPredefined.lookup(name)?.let {
                SourcedDefinition(it, null, DefinitionSource.Predefined)
            }
        }

        fun baseBundle(): DefinitionBundle {
            return LatexLibraryDefinitionService.baseLibBundle
        }

        fun union(list: List<DefinitionBundle>): DefinitionBundle {
            if (list.size == 1) return list[0]
            return CompositeOverridingDefinitionBundle(list)
        }

        private fun getBundleFor(element: PsiElement): DefinitionBundle {
            val file = element.containingFile ?: return LatexLibraryDefinitionService.baseLibBundle
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