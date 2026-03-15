package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.ide.macro.MacroManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

/**
 * Expands path and IDE macros used in run-configuration path fields.
 */
internal object LatexPathMacroSupport {

    private val macroPattern = Regex("""\$([A-Za-z0-9_.-]+)\$""")

    fun expandPath(raw: String, project: Project, mainFile: VirtualFile?): String {
        if (raw.isBlank()) {
            return raw
        }

        val module = moduleForFile(mainFile, project)
        val configuratorExpanded = expandWithProgramParameters(raw, project, module, mainFile)
        if (!configuratorExpanded.contains('$')) {
            return configuratorExpanded
        }

        val pathExpanded = PathMacroManager.getInstance(project).expandPath(configuratorExpanded)
        if (!pathExpanded.contains('$')) {
            return pathExpanded
        }

        val macros = PathMacros.getInstance()
        return macroPattern.replace(pathExpanded) { match ->
            macros.getValue(match.groupValues[1]) ?: match.value
        }
    }

    private fun moduleForFile(mainFile: VirtualFile?, project: Project): Module? {
        if (mainFile == null || !project.isInitialized) {
            return null
        }
        return ReadAction.compute<Module?, RuntimeException> {
            ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(mainFile, false)
        }
    }

    private fun expandWithProgramParameters(
        value: String,
        project: Project,
        module: Module?,
        mainFile: VirtualFile?,
    ): String {
        val configurator = ProgramParametersConfigurator()
        if (!MacroManager.containsMacros(value)) {
            return configurator.expandPathAndMacros(value, module, project) ?: value
        }
        val context = if (mainFile != null) {
            SimpleDataContext.getSimpleContext(CommonDataKeys.VIRTUAL_FILE, mainFile, DataContext.EMPTY_CONTEXT)
        }
        else {
            DataContext.EMPTY_CONTEXT
        }
        return ExecutionManagerImpl.withEnvironmentDataContext(context).use {
            configurator.expandPathAndMacros(value, module, project) ?: value
        }
    }
}
