package nl.hannahsten.texifyidea.index

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
    fun getCommandsByNames(names: Set<String>, project: Project, scope: GlobalSearchScope): Collection<LatexCommands> {
        val result = ArrayList<LatexCommands>()

        for (key in getKeys(project)) {
            val commands = getItemsByName(key, project, scope)
            for (cmd in commands) {
                val token = cmd.name

                if (token in names) {
                    result.add(cmd)
                }
            }
        }

        return result
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