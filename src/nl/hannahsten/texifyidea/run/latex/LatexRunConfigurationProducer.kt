package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.latex.step.LatexStepAutoConfigurator

/**
 * @author Hannah Schellekens
 */
class LatexRunConfigurationProducer : LazyRunConfigurationProducer<LatexRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory = LatexConfigurationFactory(latexRunConfigurationType())

    override fun setupConfigurationFromContext(
        runConfiguration: LatexRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val container = location.psiElement.containingFile ?: return false
        val mainFile = container.virtualFile ?: return false

        val extension = mainFile.extension ?: return false
        if (!extension.equals(LatexFileType.defaultExtension, ignoreCase = true)) {
            return false
        }

        // Change the main file as given by the template run configuration to the current file
        runConfiguration.mainFilePath = LatexRunConfigurationStaticSupport.toProjectRelativePathOrAbsolute(runConfiguration, mainFile)
        runConfiguration.executionState.psiFile = container.createSmartPointer()
        runConfiguration.setSuggestedName()

        runConfiguration.configOptions.steps = LatexStepAutoConfigurator.configureFromContext(
            runConfiguration,
            container,
            mainFile,
        ).toMutableList()
        return true
    }

    override fun isConfigurationFromContext(
        runConfiguration: LatexRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val psiFile = context.dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        val currentFile = psiFile.virtualFile ?: return false
        return LatexRunConfigurationStaticSupport.resolveMainFile(runConfiguration) == currentFile
    }
}
