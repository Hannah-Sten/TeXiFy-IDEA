package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.ide.macro.Macro
import com.intellij.ide.macro.Macro.ExecutionCancelledException
import com.intellij.ide.macro.MacroManager
import com.intellij.ide.macro.PromptingMacro
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.execution.ParametersListUtil

class CustomContextProgramParametersConfigurator(val runConfiguration: LatexRunConfiguration) : ProgramParametersConfigurator() {

    // Taken from ProgramParametersConfigurator#expandPathAndMacros
    override fun expandPathAndMacros(s: String?, module: Module?, project: Project): String? {
        var path = s
        if (path != null) path = expandPath(path, module, project)
        if (path != null) path = expandMacros(path, projectContext(project, module, false), false)
        return path
    }

    // Taken from ProgramParametersConfigurator.expandMacros
    private fun expandMacros(path: String, fallbackDataContext: DataContext, applyParameterEscaping: Boolean): String {
        if (!Registry.`is`("allow.macros.for.run.configurations")) {
            return path
        }

        val context = createContext(fallbackDataContext)
        try {
            val macros = MacroManager.getInstance().macros
            return MacroManager.expandMacros(path, macros) { macro: Macro, occurence: String ->
                val value = StringUtil.notNullize(previewOrExpandMacro(macro, context, occurence))
                if (applyParameterEscaping) ParametersListUtil.escape(value) else value
            }
        }
        catch (ignore: ExecutionCancelledException) {
            return path // won't happen :)
        }
    }

    // Taken from ProgramParametersConfigurator.previewOrExpandMacro
    private fun previewOrExpandMacro(macro: Macro, dataContext: DataContext, occurence: String): String? {
        return try {
            if (macro is PromptingMacro) macro.expandOccurence(dataContext, occurence) else ReadAction.nonBlocking<String> { macro.expandOccurence(dataContext, occurence) }.executeSynchronously()
        }
        catch (e: ExecutionCancelledException) {
            null
        }
    }

    // Taken from ProgramParametersConfigurator.createContext
    private fun createContext(fallbackDataContext: DataContext): DataContext {
        val envContext = ExecutionManagerImpl.getEnvironmentDataContext()
        return if (envContext == null) fallbackDataContext
        else DataContext { dataId ->
            if (dataId == CommonDataKeys.VIRTUAL_FILE.name) {
                runConfiguration.mainFile
            } else {
                envContext.getData(dataId) ?: fallbackDataContext.getData(dataId)
            }
        }
    }
}