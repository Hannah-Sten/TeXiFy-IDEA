package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project

/**
 * Expands IDE path macros used in run-configuration path fields.
 * Falls back to user-defined macro lookup when macros remain unresolved.
 */
internal object LatexPathMacroSupport {

    private val macroPattern = Regex("""\$([A-Za-z0-9_.-]+)\$""")

    fun expandPath(raw: String, project: Project): String {
        if (raw.isBlank()) {
            return raw
        }
        val ideExpanded = PathMacroManager.getInstance(project).expandPath(raw)
        if (!ideExpanded.contains('$')) {
            return ideExpanded
        }
        val macros = PathMacros.getInstance()
        return macroPattern.replace(ideExpanded) { match ->
            macros.getValue(match.groupValues[1]) ?: match.value
        }
    }
}
