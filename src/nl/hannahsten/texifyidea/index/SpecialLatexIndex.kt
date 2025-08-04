package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.contentSearchScope
import nl.hannahsten.texifyidea.util.magic.CommandMagic

object SpecialKeys {
    const val FILE_INPUTS = "file_inputs"
    const val COMMAND_DEFINITIONS = "command_def"
    const val ENV_DEFINITIONS = "env_def"
    const val ALL_DEFINITIONS = "all_def"
    const val PACKAGE_INCLUDES = "package_includes"
    const val REGULAR_COMMAND_DEFINITION = "non_math_cmd_def"
    const val GLOSSARY_ENTRY = "glossary_entry"
}

/**
 * Records special commands in the project, such as file inputs, command definitions, environment definitions, and package includes.
 */
class NewSpecialCommandsIndexEx : SpecialKeyStubIndexWrapper<LatexCommands>(LatexCommands::class.java) {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS_SPECIAL
    }

    override fun getVersion(): Int {
        return 103
    }

    override fun buildFileset(baseFile: PsiFile): GlobalSearchScope {
        return LatexProjectStructure.getFilesetScopeFor(baseFile)
    }

    val mappingPairs = listOf(
        CommandMagic.allFileIncludeCommands to SpecialKeys.FILE_INPUTS,
        CommandMagic.commandDefinitionsAndRedefinitions to SpecialKeys.COMMAND_DEFINITIONS,
        CommandMagic.environmentDefinitions to SpecialKeys.ENV_DEFINITIONS,
        CommandMagic.definitions to SpecialKeys.ALL_DEFINITIONS,
        CommandMagic.packageInclusionCommands to SpecialKeys.PACKAGE_INCLUDES,
        CommandMagic.regularCommandDefinitionsAndRedefinitions to SpecialKeys.REGULAR_COMMAND_DEFINITION,
        CommandMagic.glossaryEntry to SpecialKeys.GLOSSARY_ENTRY
    )

    override val specialKeys: Set<String> = mappingPairs.map { it.second }.toSet()

    override val specialKeyMap: Map<String, List<String>> = buildMap {
        for ((commands, sKey) in mappingPairs) {
            commands.forEach { cmd ->
                merge(cmd, listOf(sKey), List<String>::plus)
            }
        }
    }

    fun getAllFileInputs(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.FILE_INPUTS, project)
    }

    fun getAllFileInputsInFileset(file: PsiFile): Collection<LatexCommands> {
        return getByNameInFileSet(SpecialKeys.FILE_INPUTS, file)
    }

    fun getAllFileInputs(project: Project, file: VirtualFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.FILE_INPUTS, project, GlobalSearchScope.fileScope(project, file))
    }

    fun getAllPackageIncludes(project: Project, scope: GlobalSearchScope): Collection<LatexCommands> {
        return getByName(SpecialKeys.PACKAGE_INCLUDES, project, scope)
    }

    fun getPackageIncludes(project: Project, file: VirtualFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.PACKAGE_INCLUDES, project, GlobalSearchScope.fileScope(project, file))
    }

    fun getAllCommandDef(project: Project, scope: GlobalSearchScope): Collection<LatexCommands> {
        return getByName(SpecialKeys.COMMAND_DEFINITIONS, project, scope)
    }

    fun getAllCommandDef(project: Project, file : VirtualFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.COMMAND_DEFINITIONS, project, file)
    }

    fun getAllCommandDefInFileset(file: PsiFile): Collection<LatexCommands> {
        return getByNameInFileSet(SpecialKeys.COMMAND_DEFINITIONS, file)
    }

    fun getAllRegularCommandDef(project: Project, scope: GlobalSearchScope = project.contentSearchScope): Collection<LatexCommands> {
        return getByName(SpecialKeys.REGULAR_COMMAND_DEFINITION, project, scope)
    }

    fun getAllEnvDef(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.ENV_DEFINITIONS, project)
    }

    fun getAllEnvDef(scope: GlobalSearchScope): Collection<LatexCommands> {
        return getByName(SpecialKeys.ENV_DEFINITIONS, scope)
    }

    fun processCommandDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.COMMAND_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    fun processEnvDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.ENV_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    fun getAllEnvDefRelated(originalFile: PsiFile): Collection<LatexCommands> {
        return getAllEnvDef(originalFile.project)
    }

    /**
     * Get all glossary entries in the project fileset of the given file, considering only `.tex` files.
     */
    fun getAllGlossaryEntries(originalFile: PsiFile): Collection<LatexCommands> {
        val scope = LatexProjectStructure.getFilesetScopeFor(originalFile, onlyTexFiles = true)
        return getByName(SpecialKeys.GLOSSARY_ENTRY, scope)
    }
}

val NewSpecialCommandsIndex = NewSpecialCommandsIndexEx()
