package nl.hannahsten.texifyidea.run.macro

import com.intellij.ide.macro.FileDirMacro
import com.intellij.openapi.application.PathMacroContributor

/**
 * Provides custom macros to be used in text fields in the run configuration.
 */
class LatexPathMacroContributor : PathMacroContributor {
    override fun registerPathMacros(macros: MutableMap<String, String>, legacyMacros: MutableMap<String, String>) {
        // At this point, obviously we don't know the actual main file
        // However, when the macro needs to be resolved it does know the main file, see MainFileDirMacro, so the value here doesn't matter.
        macros["MainFileDir"] = MainFileDirMacro().description
    }
}