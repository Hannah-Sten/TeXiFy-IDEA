package nl.hannahsten.texifyidea.index

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands

/**
 * @author Hannah Schellekens
 */
abstract class IndexCommandsUtilBase(
    indexKey: StubIndexKey<String, LatexCommands>
) : IndexUtilBase<LatexCommands>(LatexCommands::class.java, indexKey) {

    /**
     * Get all the commands that are in a given set of names (without slash).
     */
    fun getCommandsByNames(names: Set<String>, project: Project, scope: GlobalSearchScope, useCache: Boolean = true): Collection<LatexCommands> {
        return getItems(project, scope, useCache).filter { runReadAction { it.name in names } }
    }

    /**
     * See [getCommandsByNames]
     */
    suspend fun getCommandsByNamesNonBlocking(names: Set<String>, project: Project, scope: GlobalSearchScope, useCache: Boolean = true): Collection<LatexCommands> {
        return getItemsNonBlocking(project, scope, useCache).filter { smartReadAction(project) { it.name in names } }
    }

    /**
     * Get all the commands that have a given name (without slash).
     */
    fun getCommandsByName(name: String, project: Project, scope: GlobalSearchScope): Collection<LatexCommands> {
        val nameSlash = if (name.startsWith('\\')) name else "\\$name"
        return getCommandsByNames(setOf(nameSlash), project, scope)
    }

    /**
     * Get all the commands with a certain name in a certain file. (with slash)
     */
    fun getCommandsByName(name: String, file: PsiFile): Collection<LatexCommands> {
        val nameSlash = if (name.startsWith('\\')) name else "\\$name"
        return getCommandsByName(nameSlash, file.project, GlobalSearchScope.fileScope(file))
    }

    /**
     * Get all commands that have the given names in a certain file. (with slash).
     */
    fun getCommandsByNames(file: PsiFile, vararg names: String): Collection<LatexCommands> {
        return getCommandsByNames(setOf(*names), file.project, GlobalSearchScope.fileScope(file))
    }
}