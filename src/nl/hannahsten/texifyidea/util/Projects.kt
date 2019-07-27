package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex

/**
 * Get a project [GlobalSearchScope] for this project.
 */
val Project.projectSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.projectScope(this)

/**
 * Get a [GlobalSearchScope] for the source folders in this project.
 */
val Project.sourceSetSearchScope: GlobalSearchScope
    get() {
        val rootManager = ProjectRootManager.getInstance(this)
        val files = rootManager.contentSourceRoots.asSequence()
                .flatMap { it.allChildFiles().asSequence() }
                .toSet()
        return GlobalSearchScope.filesWithoutLibrariesScope(this, files)
    }

/**
 * Looks for all defined document classes in the project.
 */
fun Project.findAvailableDocumentClasses(): Set<String> {
    val defines = LatexDefinitionIndex.getCommandsByName("ProvidesClass", this, sourceSetSearchScope)
    return defines.asSequence()
            .map { it.requiredParameters }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.firstOrNull() }
            .toSet()
}