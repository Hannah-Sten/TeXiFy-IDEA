package nl.rubensten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import nl.rubensten.texifyidea.index.LatexDefinitionIndex

/**
 * Get a project [GlobalSearchScope] for this project.
 */
val Project.projectSearchScope: GlobalSearchScope
    get() = GlobalSearchScope.projectScope(this)

/**
 * Looks for all defined document classes in the project.
 */
fun Project.findAvailableDocumentClasses(): Set<String> {
    val defines = LatexDefinitionIndex.getCommandsByName("ProvidesClass", this, projectSearchScope)
    return defines.asSequence()
            .map { it.requiredParameters }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.firstOrNull() }
            .toSet()
}