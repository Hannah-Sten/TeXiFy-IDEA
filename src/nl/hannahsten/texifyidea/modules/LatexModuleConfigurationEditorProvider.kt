package nl.hannahsten.texifyidea.modules

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleConfigurationEditor
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState

/**
 * Provides module configuration editors for LaTeX modules in the Project Structure dialog.
 * This enables SDK selection per module, supporting multi-module projects with different LaTeX SDKs.
 */
class LatexModuleConfigurationEditorProvider : ModuleConfigurationEditorProvider {

    override fun createEditors(state: ModuleConfigurationState): Array<ModuleConfigurationEditor> {
        val module = state.currentRootModel?.module ?: return emptyArray()

        // Only provide editors for LaTeX modules
        if (!isLatexModule(module)) {
            return emptyArray()
        }

        return arrayOf(
            // Content roots editor (Sources, Resources, etc.)
            CommonContentEntriesEditor(module.name, state),
            // SDK/Dependencies editor - this is what enables module SDK selection
            // ClasspathEditor provides the SDK dropdown in the "Dependencies" tab
            ClasspathEditor(state)
        )
    }

    private fun isLatexModule(module: Module): Boolean = ModuleType.get(module).id == LatexModuleType.ID
}
