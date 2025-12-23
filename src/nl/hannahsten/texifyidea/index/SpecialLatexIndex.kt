package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdDefinitions
import nl.hannahsten.texifyidea.lang.predefined.PredefinedCmdFiles
import nl.hannahsten.texifyidea.psi.LatexCommands
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
    override fun getKey(): StubIndexKey<String, LatexCommands> = LatexStubIndexKeys.COMMANDS_SPECIAL

    override fun getVersion(): Int = 105

    val mappingPairs = listOf(
        PredefinedCmdFiles.namesOfAllFileIncludeCommands to SpecialKeys.FILE_INPUTS,
        PredefinedCmdDefinitions.namesOfAllCommandDef to SpecialKeys.COMMAND_DEFINITIONS,
        PredefinedCmdDefinitions.namesOfAllEnvironmentDef to SpecialKeys.ENV_DEFINITIONS,
        PredefinedCmdDefinitions.namesOfAllDef to SpecialKeys.ALL_DEFINITIONS,
        CommandMagic.packageInclusionCommands to SpecialKeys.PACKAGE_INCLUDES,
        CommandMagic.glossaryEntry.keys to SpecialKeys.GLOSSARY_ENTRY
    )

    override val specialKeys: Set<String> = mappingPairs.map { it.second }.toSet()

    override val specialKeyMap: Map<String, List<String>> = buildMap {
        for ((commands, sKey) in mappingPairs) {
            commands.forEach { name ->
                val cmdWithSlash = if (name.startsWith("\\")) name else "\\$name"
                merge(cmdWithSlash, listOf(sKey), List<String>::plus)
            }
        }
    }

    fun getAllFileInputs(project: Project): Collection<LatexCommands> = getByName(SpecialKeys.FILE_INPUTS, project)

    fun getAllFileInputsInFileset(file: PsiFile): Collection<LatexCommands> = getByNameInFileSet(SpecialKeys.FILE_INPUTS, file)

    fun getAllFileInputs(project: Project, file: VirtualFile): Collection<LatexCommands> = getByName(SpecialKeys.FILE_INPUTS, project, GlobalSearchScope.fileScope(project, file))

    fun getPackageIncludes(project: Project, scope: GlobalSearchScope): Collection<LatexCommands> = getByName(SpecialKeys.PACKAGE_INCLUDES, project, scope)

    fun getPackageIncludes(project: Project, file: VirtualFile): Collection<LatexCommands> = getByName(SpecialKeys.PACKAGE_INCLUDES, project, GlobalSearchScope.fileScope(project, file))

    fun getRegularCommandDef(project: Project, scope: GlobalSearchScope): Collection<LatexCommands> = getByName(SpecialKeys.COMMAND_DEFINITIONS, project, scope)

    fun getAllCommandDefInFileset(file: PsiFile): Collection<LatexCommands> = getByNameInFileSet(SpecialKeys.COMMAND_DEFINITIONS, file)

    fun getAllDefinitions(project: Project, file: VirtualFile): Collection<LatexCommands> = getByName(SpecialKeys.ALL_DEFINITIONS, project, GlobalSearchScope.fileScope(project, file))

    fun processCommandDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.COMMAND_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    fun processEnvDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.ENV_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    /**
     * Get all glossary entries in the project fileset of the given file, considering only `.tex` files.
     */
    fun getAllGlossaryEntries(originalFile: PsiFile): Collection<LatexCommands> {
        val scope = LatexProjectStructure.getFilesetScopeFor(originalFile, onlyTexFiles = true)
        return getByName(SpecialKeys.GLOSSARY_ENTRY, scope)
    }

    fun forEachGlossaryEntry(originalFile: PsiFile, action: (LatexCommands) -> Unit) {
        val scope = LatexProjectStructure.getFilesetScopeFor(originalFile, onlyTexFiles = true)
        forEachByName(SpecialKeys.GLOSSARY_ENTRY, originalFile.project, scope, null, action)
    }
}

val NewSpecialCommandsIndex = NewSpecialCommandsIndexEx()
