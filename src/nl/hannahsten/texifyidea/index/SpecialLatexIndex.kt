package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic

object SpecialKeys {
    const val FILE_INPUTS = "file_inputs"
    const val COMMAND_DEFINITIONS = "command_def"
    const val ENV_DEFINITIONS = "env_def"
    const val ALL_DEFINITIONS = "all_def"
    const val PACKAGE_INCLUDES = "package_includes"
}

/**
 * This is a temporary solution to replace the old "traversing all commands" index.
 * It will record commands used in this project and classify them into groups.
 *
 * It should be gradually removed to use more efficient methods.
 */
class NewSpecialCommandsIndexEx : SpecialKeyStubIndexBase<LatexCommands>(LatexCommands::class.java) {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS_SPECIAL
    }

    override fun getVersion(): Int {
        return 101
    }

    override fun buildSearchFiles(baseFile: PsiFile): GlobalSearchScope {
        return buildLatexSearchFiles(baseFile)
    }

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    val mappingPairs = listOf(
        CommandMagic.defaultIncludeCommands to SpecialKeys.FILE_INPUTS,
        CommandMagic.commandDefinitionsAndRedefinitions to SpecialKeys.COMMAND_DEFINITIONS,
        CommandMagic.environmentDefinitions to SpecialKeys.ENV_DEFINITIONS,
        CommandMagic.definitions to SpecialKeys.ALL_DEFINITIONS,
        CommandMagic.packageInclusionCommands to SpecialKeys.PACKAGE_INCLUDES,
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

    fun getAllFileInputs(file: PsiFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.FILE_INPUTS, file.project, GlobalSearchScope.fileScope(file))
    }

    fun getAllPackageIncludes(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.PACKAGE_INCLUDES, project)
    }

    fun getAllCommandDef(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.COMMAND_DEFINITIONS, project)
    }

    fun getAllEnvDef(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.ENV_DEFINITIONS, project)
    }

    fun getAllEnvDef(file: PsiFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.ENV_DEFINITIONS, file)
    }

    fun processCommandDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.COMMAND_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    fun processEnvDef(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<LatexCommands>) {
        processByName(SpecialKeys.ENV_DEFINITIONS, scope.project!!, scope, filter, processor)
    }

    /*
    fun getCommandsInFiles(files: MutableSet<PsiFile>, originalFile: PsiFile): Collection<LatexCommands> {
        val project = originalFile.project
        val searchFiles = files.stream()
            .map { obj: PsiFile -> obj.virtualFile }
            .collect(Collectors.toSet())
        searchFiles.add(originalFile.virtualFile)
        val scope = GlobalSearchScope.filesScope(project, searchFiles)
        return NewSpecialCommandsIndex.getAll(project, scope)
    }

     */
    fun getAllCommandDefRelated(originalFile: PsiFile): Collection<LatexCommands> {
        return getAllCommandDef(originalFile.project)
    }

    fun getAllEnvDefRelated(originalFile: PsiFile): Collection<LatexCommands> {
        return getAllEnvDef(originalFile.project)
    }
}

val NewSpecialCommandsIndex = NewSpecialCommandsIndexEx()
