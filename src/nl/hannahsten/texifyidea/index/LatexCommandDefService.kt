package nl.hannahsten.texifyidea.index

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import kotlinx.coroutines.CoroutineScope
import nl.hannahsten.texifyidea.lang.NewLatexCommand
import nl.hannahsten.texifyidea.lang.commands.PredefinedCommands
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.AbstractBackgroundCacheService
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil

class SourcedDefinition(
    val command: NewLatexCommand,
    val definitionCommandPointer: SmartPsiElementPointer<LatexCommands>?
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
}


interface CommandBundle {
    val sourcedDefinitions: Sequence<SourcedDefinition>

    fun findDef(name: String): SourcedDefinition?
}


class PackageCommandBundle(
    val packageName: String,
    val directSourcedDefinitions: Map<String, SourcedDefinition>,
    /**
     * The names of all dependencies of this package, i.e., the packages that this package depends on.
     */
    val directDependencies: List<PackageCommandBundle>,
    val allPackages: Set<String>
) : CommandBundle {
    private val lazyAllNameLookup: Lazy<Map<String, SourcedDefinition>> = lazy {
        buildMap {
            for (dep in directDependencies) {
                putAll(dep.allNameLookup)
            }
            putAll(directSourcedDefinitions)
        }
    }

    private val allNameLookup: Map<String, SourcedDefinition>
        get() = lazyAllNameLookup.value

    override fun findDef(name: String): SourcedDefinition? {
        if (lazyAllNameLookup.isInitialized()) {
            return allNameLookup[name]
        }
        return directSourcedDefinitions[name] ?: directDependencies.asReversed().firstNotNullOfOrNull {
            it.findDef(name)
        }
    }

    override val sourcedDefinitions: Sequence<SourcedDefinition>
        get() = allNameLookup.values.asSequence()


}

object CommandDefUtil {


    fun parseCommandDef(defCmd: LatexCommands, pkgName: String, project: Project): SourcedDefinition? {
        val ref = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(defCmd)
        var name = LatexPsiUtil.getDefinedCommandName(defCmd) ?: return null
        name = name.removePrefix("\\") // remove the leading backslash
        // TODO: delicate
        return SourcedDefinition(NewLatexCommand(name, pkgName), ref)
    }

    fun mergeDefinition(old: SourcedDefinition, new: SourcedDefinition): SourcedDefinition {
        if (old.definitionCommandPointer == null) {
            return SourcedDefinition(old.command, new.definitionCommandPointer)
        }
        // TODO
        return new
    }
}


/**
 * Collects command definitions
 */
@Service(Service.Level.PROJECT)
class PackageCommandDefService(
    val project: Project,
    coroutineScope: CoroutineScope
) : AbstractBackgroundCacheService<String, PackageCommandBundle>(coroutineScope) {


    private fun computeCommandDefinitions(
        pkgName: String
    ): PackageCommandBundle? {
        getValueOrNull(pkgName)?.let { return it }
        val libInfo = LatexLibraryStructure.getLibraryInfo(pkgName, project) ?: return null
        val directDependencyNames = libInfo.directDependencies
        val processedPackages = mutableSetOf(pkgName) // do not process the same package twice
        /*
        Let us be careful not to process the same package twice, note that we may have to following:
                A -> B1 -> C
                  -> B2 -> C,D
         */
        val directDependencies = mutableListOf<PackageCommandBundle>()
        for (dependency in directDependencyNames) {
            if (!processedPackages.add(dependency)) {
                continue
            }
            // recursively compute the command definitions for the dependency
            val depBundle = getValueOrNull(dependency) ?: computeCommandDefinitions(dependency)
            if (depBundle == null) continue
            directDependencies.add(depBundle)
            processedPackages.addAll(depBundle.allPackages)
        }
        val currentSourcedDefinitions = mutableMapOf<String, SourcedDefinition>()
        // process the package itself, first the predefined commands
        PredefinedCommands.packageToCommands[pkgName]?.forEach { command ->
            currentSourcedDefinitions[command.name] = SourcedDefinition(
                command,
                null // predefined commands do not have a definition command
            )
        }
        val commandDefinitions = NewSpecialCommandsIndex.getAllCommandDef(project, libInfo.location)
        for (defCmd in commandDefinitions) {
            val sourcedDef = CommandDefUtil.parseCommandDef(defCmd, pkgName, project) ?: continue
            val name = sourcedDef.command.name
            currentSourcedDefinitions.merge(name, sourcedDef, CommandDefUtil::mergeDefinition)
        }
        val result = PackageCommandBundle(pkgName, currentSourcedDefinitions, directDependencies, processedPackages)
        putValue(pkgName, result)
        return result
    }

    /**
     * Computes the command definitions for a given package (`.cls` or `.sty` file).
     *
     * @param key the name of the package (with the file extension)
     */
    override suspend fun computeValueSuspend(key: String, oldValue: PackageCommandBundle?): PackageCommandBundle? {
        return smartReadAction(project) {
            computeCommandDefinitions(key)
        }

    }
}

/**
 * Provide a unified definition service for LaTeX commands, including
 *   * those hard-coded in the plugin, see [nl.hannahsten.texifyidea.util.magic.CommandMagic], [nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand]
 *   * those indexed by file-based index [nl.hannahsten.texifyidea.index.file.LatexExternalCommandIndex]
 *   * those indexed by stub-based index [nl.hannahsten.texifyidea.index.NewDefinitionIndex]
 */
@Service(Service.Level.PROJECT)
class LatexCommandDefService(
    val project: Project,
    coroutineScope: CoroutineScope
) : AbstractBackgroundCacheService<Fileset, PackageCommandBundle>(coroutineScope) {

    override suspend fun computeValueSuspend(key: Fileset, oldValue: PackageCommandBundle?): PackageCommandBundle? {
        TODO("Not yet implemented")
    }

    fun foo() {
//        CachedValuesManager.getProjectPsiDependentCache<>()
    }
}