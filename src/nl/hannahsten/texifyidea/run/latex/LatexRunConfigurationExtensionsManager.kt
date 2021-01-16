package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configuration.RunConfigurationExtensionsManager
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service

@Service
class LatexRunConfigurationExtensionsManager : RunConfigurationExtensionsManager<RunConfigurationBase<*>, RunConfigurationExtension>(RunConfigurationExtension.EP_NAME) {

    companion object {

        val instance: LatexRunConfigurationExtensionsManager
            get() = service()
    }
}
