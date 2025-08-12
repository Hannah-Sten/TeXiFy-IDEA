package nl.hannahsten.texifyidea.index

import arrow.atomic.AtomicLong
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.coroutines.CoroutineScope
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LSemanticEnv
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands
import nl.hannahsten.texifyidea.lang.predefined.AllPredefinedEnvironments
import nl.hannahsten.texifyidea.lang.predefined.PredefinedPrimitives
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.AbstractBackgroundCacheService
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import java.util.concurrent.atomic.AtomicInteger

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
        LibraryScan,
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

interface DefinitionBundle : LatexSemanticsLookup {
    fun findCmdDef(name: String): SourcedCmdDefinition? {
        return findDefinition(name) as? SourcedCmdDefinition
    }

    fun findEnvDef(name: String): SourcedEnvDefinition? {
        return findDefinition(name) as? SourcedEnvDefinition
    }

    fun findDefinition(name: String): SourcedDefinition?

    override fun lookup(name: String): LSemanticEntity? {
        return findDefinition(name)?.entity
    }

    fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>)

    fun sourcedDefinitions(): Collection<SourcedDefinition> {
        val nameMap = mutableMapOf<String, SourcedDefinition>()
        val includedPackages = mutableSetOf<String>()
        appendDefinitions(nameMap, includedPackages)
        return nameMap.values
    }
}

abstract class MergedDefinitionBundle(
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
    val libName: String,
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<LibDefinitionBundle> = emptyList(),
    val allLibraries: Set<String> = setOf(libName)
) : CachedMergedDefinitionBundle(introducedDefinitions, directDependencies) {

    override fun toString(): String {
        return "Lib($libName, #defs=${introducedDefinitions.size})"
    }

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
        if (!includedPackages.add(libName)) return // do not process the same package twice
        super.appendDefinitions(nameMap, includedPackages)
    }
}

/**
 * Command definition service for a single LaTeX package (`.cls` or `.sty` file).
 */
@Service(Service.Level.PROJECT)
class LatexLibraryDefinitionService(
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
        val libInfo = LatexLibraryStructureService.getInstance(project).getLibraryInfo(pkgName)
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
        totalTimeCost.addAndGet(buildTime)
        return result
    }

    /**
     * Should be long, since packages do not change.
     */
    val expirationInMs: Long = 100000L

    fun getLibBundle(libName: String): LibDefinitionBundle {
        return getOrComputeNow(libName, expirationInMs)
    }

    companion object : SimplePerformanceTracker {
        fun getInstance(project: Project): LatexLibraryDefinitionService {
            return project.service()
        }

        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)

        val baseLibBundle: LibDefinitionBundle by lazy {

            // return the hard-coded basic commands
            val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
            if (ApplicationManager.getApplication().isUnitTestMode) {
                // add all the predefined commands and environments in unit test mode
                processAllPredefinedCommands(currentSourcedDefinitions)
                processAllPredefinedEnvironments(currentSourcedDefinitions)
            }
            else {
                processPredefinedCommandsAndEnvironments("", currentSourcedDefinitions)
            }

            // overwrite the definitions with the primitive commands
            PredefinedPrimitives.allCommands.forEach {
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

        private fun processAllPredefinedCommands(defMap: MutableMap<String, SourcedDefinition>) {
            AllPredefinedCommands.allCommands.forEach { command ->
                defMap[command.name] = SourcedCmdDefinition(command, null, DefinitionSource.Predefined)
            }
        }

        private fun processAllPredefinedEnvironments(defMap: MutableMap<String, SourcedDefinition>) {
            AllPredefinedEnvironments.allEnvironments.forEach { env ->
                defMap[env.name] = SourcedEnvDefinition(env, null, DefinitionSource.Predefined)
            }
        }
    }
}

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.lang.predefined.AllPredefinedCommands], [nl.hannahsten.texifyidea.lang.predefined.AllPredefinedEnvironments].
 *   * those indexed by stub-based index [NewDefinitionIndex]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 */
@Service(Service.Level.PROJECT)
class LatexDefinitionService(
    val project: Project, scope: CoroutineScope
) : AbstractBackgroundCacheService<Fileset, DefinitionBundle>(scope) {

    val expirationInMs: Long
        get() = TexifySettings.getInstance().filesetExpirationTimeMs.toLong()

    private class WorkingFilesetDefinitionBundle(
        libraryBundles: List<LibDefinitionBundle> = emptyList(),
    ) : DefinitionBundle {
        private val allNameLookup: MutableMap<String, SourcedDefinition> = mutableMapOf()

        init {
            val includedPackages = mutableSetOf<String>()
            for (dep in libraryBundles) {
                dep.appendDefinitions(allNameLookup, includedPackages)
            }
        }

        override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
            nameMap.putAll(allNameLookup)
        }

        override fun findDefinition(name: String): SourcedDefinition? {
            return allNameLookup[name]
        }

        /**
         * Overwrite the definition for a custom command or environment.
         */
        fun addCustomDefinition(def: SourcedDefinition) {
            allNameLookup[def.entity.name] = def
        }

        override fun sourcedDefinitions(): Collection<SourcedDefinition> {
            return allNameLookup.values
        }
    }

    private fun computeValue(key: Fileset, oldValue: DefinitionBundle?): DefinitionBundle {
        val startTime = System.currentTimeMillis()

        // packages first
        val packageService = LatexLibraryDefinitionService.getInstance(project)
        val libraries = ArrayList<LibDefinitionBundle>(key.libraries.size + 1)
        libraries.add(packageService.getLibBundle("")) // add the default commands
        key.libraries.mapTo(libraries) { packageService.getLibBundle(it) }

        val bundle = WorkingFilesetDefinitionBundle(libraries)

        val projectFileIndex = ProjectFileIndex.getInstance(project)
        // a building placeholder for the bundle to make lookups work
        for (file in key.files) {
            if (!projectFileIndex.isInProject(file)) continue
            val commandDefinitions = LatexDefinitionUtil.collectCustomDefinitions(file, project, bundle)
            for (sourcedDef in commandDefinitions) {
                // always overwrite for user-defined commands
                bundle.addCustomDefinition(sourcedDef)
            }
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
        return getAndComputeLater(fileset, expirationInMs, LatexLibraryDefinitionService.baseLibBundle)
    }

    fun getDefBundlesMerged(psiFile: PsiFile): DefinitionBundle {
        val filesetData = LatexProjectStructure.getFilesetDataFor(psiFile) ?: return LatexLibraryDefinitionService.baseLibBundle
        if (filesetData.filesets.size == 1) return getDefBundleForFileset(filesetData.filesets.first())
        return union(filesetData.filesets.map { getDefBundleForFileset(it) })
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
            getDefBundleForFileset(it).findDefinition(name)
        }
    }

    fun resolveDefInProject(name: String): SourcedDefinition? {
        val pf = LatexProjectStructure.getFilesets(project) ?: return resolvePredefinedDef(name)
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

    companion object : SimplePerformanceTracker {
        fun getInstance(project: Project): LatexDefinitionService {
            return project.service()
        }

        override val countOfBuilds = AtomicInteger(0)
        override val totalTimeCost = AtomicLong(0)

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
            return CompositeOverridingDefinitionBundle(list)
        }
    }

    class CompositeOverridingDefinitionBundle(
        val bundles: List<DefinitionBundle>
    ) : DefinitionBundle {
        override fun findDefinition(name: String): SourcedDefinition? {
            return bundles.firstNotNullOfOrNull { it.findDefinition(name) }
        }

        override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
            for (bundle in bundles) {
                bundle.sourcedDefinitions().forEach {
                    nameMap[it.entity.name] = it
                }
            }
        }
    }
}