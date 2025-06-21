package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexCommands

abstract class NewLatexCommandsIndex : StringStubIndexExtension<LatexCommands>() {


    companion object{
        /**
         * A special key to retrieve all commands in the index.
         */
        const val KEY_ALL_COMMANDS : String = "_ALL_"
    }


    override fun getVersion(): Int {
        return LatexParserDefinition.Cache.FILE.stubVersion
    }


    fun getAllCommands(project: Project, scope: GlobalSearchScope): Collection<LatexCommands> {
        return StubIndex.getElements(
            key,
            KEY_ALL_COMMANDS,
            project,
            LatexFileFilterScope(scope),
            LatexCommands::class.java
        )
    }

    fun traverseAllCommands(
        project: Project,
        scope: GlobalSearchScope,
        action: (LatexCommands) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseAllElements(key, project, LatexFileFilterScope(scope), action)
    }


    fun traverseCommandsByName(
        project: Project,
        scope: GlobalSearchScope,
        name: String,
        action: (LatexCommands) -> Boolean
    ): Boolean {
        return StubIndexKt.traverseElements(key, name, project, LatexFileFilterScope(scope), action)
    }

}