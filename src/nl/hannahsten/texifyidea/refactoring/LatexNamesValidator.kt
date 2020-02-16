package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.util.formatAsLabel

/**
 * Check if user provided names during refactoring are valid.
 */
class LatexNamesValidator : NamesValidator {
    override fun isKeyword(name: String, project: Project?): Boolean {
        return false
    }

    override fun isIdentifier(name: String, project: Project?): Boolean {
        // Unfortunately this is a global rule
        // For now we assume the user is refactoring a label
        /** See [formatAsLabel] */
        return name.toSet().intersect(setOf('%', '~', '#', '\\')).isEmpty()
    }
}