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
        /** See [formatAsLabel] */
        // Exclude the first \ to allow renaming commands
        return name.substring(1).toSet().intersect(setOf('%', '~', '#', '\\')).isEmpty()
    }
}