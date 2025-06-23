package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewCommandsIndexEx : NewLatexCommandsStubIndex() {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS
    }
}

val NewCommandsIndex = NewCommandsIndexEx()

class NewSpecialCommandsIndexEx : SpecialKeyStubIndexBase<LatexCommands>(LatexCommands::class.java) {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS_SPECIAL
    }

    val mappingPairs = listOf(
        CommandMagic.defaultIncludeCommands to SpecialKeys.INCLUDES,
        CommandMagic.commandDefinitionsAndRedefinitions to SpecialKeys.COMMAND_DEFINITIONS,
        CommandMagic.environmentDefinitions to SpecialKeys.ENV_DEFINITIONS
    )

    override val keyForAll: String
        get() = SpecialKeys.KEY_ALL_COMMANDS

    override val specialKeys: Set<String> = mappingPairs.map { it.second }.toSet()

    override val specialKeyMap: Map<String, List<String>> = buildMap {
        for ((commands, sKey) in mappingPairs) {
            commands.forEach { cmd ->
                merge(cmd, listOf(sKey), List<String>::plus)
            }
        }
    }

    object SpecialKeys {
        const val KEY_ALL_COMMANDS: String = "_ALL_"
        const val INCLUDES = "_INCLUDES_"
        const val COMMAND_DEFINITIONS = "_COMMAND_DEF_"
        const val ENV_DEFINITIONS = "_ENV_DEF_"
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
}

val NewSpecialCommandsIndex = NewSpecialCommandsIndexEx()

open class NewDefinitionIndexEx : NewLatexCommandsStubIndex() {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.DEFINITIONS_KEY
    }
}

object NewDefinitionIndex : NewDefinitionIndexEx()

open class NewIncludesIndexEx : NewLatexCommandsStubIndex() {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.INCLUDES
    }
}

object NewIncludesIndex : NewIncludesIndexEx()