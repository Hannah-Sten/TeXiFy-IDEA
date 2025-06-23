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
    const val INCLUDES = "includes"
    const val COMMAND_DEFINITIONS = "command_def"
    const val ENV_DEFINITIONS = "env_def"
    const val ALL_DEFINITIONS = "all_def"
}

class NewSpecialCommandsIndexEx : SpecialKeyStubIndexBase<LatexCommands>(LatexCommands::class.java) {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS_SPECIAL
    }

    override fun getVersion(): Int {
        return 100
    }

    override fun buildSearchFiles(baseFile: PsiFile): GlobalSearchScope {
        return buildLatexSearchFiles(baseFile)
    }

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    val mappingPairs = listOf(
        CommandMagic.defaultIncludeCommands to SpecialKeys.INCLUDES,
        CommandMagic.commandDefinitionsAndRedefinitions to SpecialKeys.COMMAND_DEFINITIONS,
        CommandMagic.environmentDefinitions to SpecialKeys.ENV_DEFINITIONS,
        CommandMagic.definitions to SpecialKeys.ALL_DEFINITIONS
    )

    override val specialKeys: Set<String> = mappingPairs.map { it.second }.toSet()

    override val specialKeyMap: Map<String, List<String>> = buildMap {
        for ((commands, sKey) in mappingPairs) {
            commands.forEach { cmd ->
                merge(cmd, listOf(sKey), List<String>::plus)
            }
        }
    }

    fun getAllIncludes(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.INCLUDES, project)
    }

    fun getAllIncludes(file: PsiFile): Collection<LatexCommands> {
        return getByName(SpecialKeys.INCLUDES, file.project, GlobalSearchScope.fileScope(file))
    }

    fun getAllCommandDef(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.COMMAND_DEFINITIONS, project)
    }

    fun getAllEnvDef(project: Project): Collection<LatexCommands> {
        return getByName(SpecialKeys.ENV_DEFINITIONS, project)
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
        // TODO
        return getAllCommandDef(originalFile.project)
    }

    fun getAllEnvDefRelated(originalFile: PsiFile): Collection<LatexCommands> {
        // TODO
        return getAllEnvDef(originalFile.project)
    }

}

val NewSpecialCommandsIndex = NewSpecialCommandsIndexEx()
