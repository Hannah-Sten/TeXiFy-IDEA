package nl.hannahsten.texifyidea.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.SourcedDefinition.DefinitionSource
import nl.hannahsten.texifyidea.lang.NewLatexCommand
import nl.hannahsten.texifyidea.lang.commands.PredefinedCommands
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.AbstractBlockingCacheService
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class SourcedDefinition(
    val command: NewLatexCommand,
    val definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?,
    val source: DefinitionSource = DefinitionSource.UserDefined
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourcedDefinition

        return command.name == other.command.name
    }

    override fun hashCode(): Int {
        return command.name.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("Def(${command.name}, ${source.name}")
            if (definitionCommandPointer != null) {
                append(", with PSI")
            }
            append(" in ${command.dependency})")
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


object CommandDefUtil {


    fun parseCommandDef(defCmd: LatexCommands, pkgName: String, project: Project): SourcedDefinition? {
        val ref = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(defCmd)
        var name = LatexPsiUtil.getDefinedCommandName(defCmd) ?: return null
        name = name.removePrefix("\\") // remove the leading backslash
        // TODO: delicate
        return SourcedDefinition(NewLatexCommand(name, pkgName), ref, DefinitionSource.UserDefined)
    }

    fun mergeCommand(old: NewLatexCommand, new: NewLatexCommand): NewLatexCommand {
        if (old.fqName != new.fqName) {
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
        return NewLatexCommand(
            new.name, new.dependency,
            requiredContext = requiredContext,
            arguments = arguments,
            description = description,
            display = display
        )
    }

    fun mergeDefinition(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        if (old.command.fqName != new.command.fqName) {
            // TODO: change to log after testing
            println("Merging command def: $old and $new")
        }
        // do not override primitive definitions
        if (old.source == DefinitionSource.Primitive) return old
        if (new.source == DefinitionSource.Primitive) return new
        val cmd = mergeCommand(old.command, new.command)
        val pointer = new.definitionCommandPointer ?: old.definitionCommandPointer
        return SourcedDefinition(cmd, pointer, DefinitionSource.Merged)
    }
}


interface CommandBundle {
    fun findDef(name: String): SourcedDefinition?

    fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>)

    fun sourcedDefinitions(): Collection<SourcedDefinition> {
        val nameMap = mutableMapOf<String, SourcedDefinition>()
        val includedPackages = mutableSetOf<String>()
        appendDefinitions(nameMap, includedPackages)
        return nameMap.values
    }
}

abstract class CompositeCommandBundle(
    val introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    val directDependencies: List<CommandBundle> = emptyList(),
) : CommandBundle {

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
        for (dep in directDependencies) {
            dep.appendDefinitions(nameMap, includedPackages)
        }
        for (sourcedDef in introducedDefinitions.values) {
            nameMap.merge(sourcedDef.command.name, sourcedDef, CommandDefUtil::mergeDefinition)
        }
    }

    override fun findDef(name: String): SourcedDefinition? {
        return introducedDefinitions[name] ?: directDependencies.asReversed().firstNotNullOfOrNull {
            it.findDef(name)
        }
    }
}

class LibCommandBundle(
    val libName: String,
    introducedDefinitions: Map<String, SourcedDefinition> = emptyMap(),
    directDependencies: List<LibCommandBundle> = emptyList(),
    val allLibraries: Set<String> = setOf(libName)
) : CompositeCommandBundle(introducedDefinitions, directDependencies) {

    override fun toString(): String {
        return "Lib($libName, #defs=${introducedDefinitions.size})"
    }

    val allNameLookup: Map<String, SourcedDefinition> by lazy {
        buildMap {
            // let us cache the full lookup map since a package can be used frequently
            appendDefinitions(this, mutableSetOf())
        }
    }

    override fun findDef(name: String): SourcedDefinition? {
        return allNameLookup[name]
    }

    override fun sourcedDefinitions(): Collection<SourcedDefinition> {
        return allNameLookup.values
    }

    override fun appendDefinitions(nameMap: MutableMap<String, SourcedDefinition>, includedPackages: MutableSet<String>) {
        if (!includedPackages.add(libName)) return // do not process the same package twice
        super.appendDefinitions(nameMap, includedPackages)
    }
}

class FilesetCommandBundle(
    customCommands: Map<String, SourcedDefinition> = emptyMap(),
    libraryBundles: List<LibCommandBundle> = emptyList()
) : CompositeCommandBundle(customCommands, libraryBundles)


/**
 * Collects command definitions
 */
@Service(Service.Level.PROJECT)
class PackageCommandDefService(
    val project: Project
) : AbstractBlockingCacheService<String, LibCommandBundle>() {

    private fun processPredefinedCommands(name: String, defMap: MutableMap<String, SourcedDefinition>) {
        PredefinedCommands.packageToCommands[name]?.forEach { command ->
            defMap[command.name] =
                SourcedDefinition(
                    command, null, // predefined commands do not have a definition command
                    DefinitionSource.Predefined
                )
        }
    }


    private fun processStubBasedDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val stubBasedDefinitions = NewSpecialCommandsIndex.getAllCommandDef(project, libInfo.location)
        for (defCmd in stubBasedDefinitions) {
            val sourcedDef = CommandDefUtil.parseCommandDef(defCmd, libInfo.name, project) ?: continue
            val name = sourcedDef.command.name
            currentSourcedDefinitions.merge(name, sourcedDef, CommandDefUtil::mergeDefinition)
        }
    }

    private fun processExternalDefinitions(
        libInfo: LatexLibraryInfo,
        currentSourcedDefinitions: MutableMap<String, SourcedDefinition>
    ) {
        val scope = GlobalSearchScope.fileScope(project, libInfo.location)
//        val externalIndexDefinitions = LatexExternalCommandIndex.getAllKeys(scope)
//        externalIndexDefinitions.size
//        for (key in externalIndexDefinitions) {
//            val documentation = LatexExternalCommandIndex.getValuesByKey(key, scope).lastOrNull() ?: continue
//            val name = key.removePrefix("\\") // remove the leading backslash
//            val sourcedDef = SourcedDefinition(
//                NewLatexCommand(name, libInfo.name, description = documentation),
//                null,
//                DefinitionSource.Package
//            )
//            currentSourcedDefinitions.merge(name, sourcedDef, CommandDefUtil::mergeDefinition)
//        }
    }

    private fun getDefaultLibBundle(): LibCommandBundle {
        // return the default commands, i.e., those hard-coded in the plugin
        val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
        processPredefinedCommands("", currentSourcedDefinitions)
        return LibCommandBundle("", currentSourcedDefinitions)
    }

    private fun computeCommandDefinitionsRecur(
        pkgName: String, processedPackages: MutableSet<String> // to prevent loops
    ): LibCommandBundle {
        if (pkgName.isEmpty()) {
            return getDefaultLibBundle()
        }
        getTimedValue(pkgName)?.takeIf { it.isNotExpired(expirationInMs) }?.let { return it.value }
        if (!processedPackages.add(pkgName)) {
            Log.warn("Recursive package dependency detected for package [$pkgName] !")
            return LibCommandBundle(pkgName)
        }
        val libInfo = LatexLibraryStructure.getLibraryInfo(pkgName, project) ?: return LibCommandBundle(pkgName)
        val directDependencyNames = libInfo.directDependencies
        val includedPackages = mutableSetOf(pkgName) // do not process the same package twice
        /*
        Let us be careful not to process the same package twice, note that we may have to following:
                A -> B1 -> C
                  -> B2 -> C,D
         */
        val directDependencies = mutableListOf<LibCommandBundle>()
        for (dependency in directDependencyNames) {
            if (!includedPackages.add(dependency)) {
                continue
            }
            // recursively compute the command definitions for the dependency
            val depBundle = getValueOrNull(dependency) ?: computeCommandDefinitionsRecur(dependency, processedPackages)
            directDependencies.add(depBundle)
            includedPackages.addAll(depBundle.allLibraries)
        }
        val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
        processPredefinedCommands(pkgName, currentSourcedDefinitions)
        processStubBasedDefinitions(libInfo, currentSourcedDefinitions)
        processExternalDefinitions(libInfo, currentSourcedDefinitions)
        val result = LibCommandBundle(pkgName, currentSourcedDefinitions, directDependencies, includedPackages)
        putValue(pkgName, result)
        return result
    }

    /**
     * Computes the command definitions for a given package (`.cls` or `.sty` file).
     *
     * @param key the name of the package (with the file extension)
     */
    override fun computeValue(key: String, oldValue: LibCommandBundle?): LibCommandBundle {
        return computeCommandDefinitionsRecur(key, mutableSetOf())
    }


    val expirationInMs: Long = 100000L

    fun getLibBundle(libName: String): LibCommandBundle {
        return getOrComputeNow(libName, expirationInMs)
    }

    companion object {
        fun getInstance(project: Project): PackageCommandDefService {
            return project.service()
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
class LatexCommandDefService(
    val project: Project,
) : AbstractBlockingCacheService<Fileset, FilesetCommandBundle>() {

    var expirationInMs: Long = 1L

    override fun computeValue(key: Fileset, oldValue: FilesetCommandBundle?): FilesetCommandBundle {
        val packageService = PackageCommandDefService.getInstance(project)
        val libraries = ArrayList<LibCommandBundle>(key.libraries.size + 1)
        libraries.add(packageService.getLibBundle("")) // add the default commands
        key.libraries.mapTo(libraries) { packageService.getLibBundle(it) }
        val commandDefinitions = NewSpecialCommandsIndex.getAllCommandDef(project, key.projectFileScope(project))
        return FilesetCommandBundle(
            customCommands = buildMap {
                for (defCmd in commandDefinitions) {
                    val sourcedDef = CommandDefUtil.parseCommandDef(defCmd, "", project) ?: continue
                    val name = sourcedDef.command.name
                    put(name, sourcedDef)
                    // never merge user-defined commands
                }
            },
            libraryBundles = libraries
        )
    }


    fun getFilesetBundle(fileset: Fileset): FilesetCommandBundle {
        return getOrComputeNow(fileset, expirationInMs)
    }

    fun getFilesetBundles(v: VirtualFile): List<FilesetCommandBundle> {
        val filesetData = LatexProjectStructure.getFilesetDataFor(v, project) ?: return emptyList()
        return filesetData.filesets.map { getFilesetBundle(it) }
    }

    fun resolveCommandDef(v: VirtualFile, commandName: String): SourcedDefinition? {
        val nameWithoutSlash = commandName.removePrefix("\\")
        val filesetData = LatexProjectStructure.getFilesetDataFor(v, project) ?: return null
        return filesetData.filesets.firstNotNullOfOrNull {
            getFilesetBundle(it).findDef(nameWithoutSlash)
        }
    }

    fun resolveCommandDef(commandName: String): SourcedDefinition? {
        val nameWithoutSlash = commandName.removePrefix("\\")
        val pf = LatexProjectStructure.getFilesets(project) ?: return null
        return pf.filesets.firstNotNullOfOrNull {
            getFilesetBundle(it).findDef(nameWithoutSlash)
        }
    }

    companion object {
        fun getInstance(project: Project): LatexCommandDefService {
            return project.service()
        }
    }

}