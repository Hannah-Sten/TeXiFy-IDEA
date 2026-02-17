package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory

class LatexmkRunConfigurationProducer : LazyRunConfigurationProducer<LatexmkRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory = LatexConfigurationFactory(latexmkRunConfigurationType())

    override fun setupConfigurationFromContext(
        runConfiguration: LatexmkRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val container = location.psiElement.containingFile ?: return false
        val mainFile = container.virtualFile ?: return false

        val extension = mainFile.extension
        val texExtension = LatexFileType.defaultExtension
        if (extension == null || !extension.equals(texExtension, ignoreCase = true)) {
            return false
        }

        runConfiguration.mainFile = mainFile
        runConfiguration.setSuggestedName()
        runConfiguration.outputPath = runConfiguration.outputPath.clone()
        runConfiguration.auxilPath = runConfiguration.auxilPath.clone()
        runConfiguration.workingDirectory = null
        runConfiguration.compiler = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.LATEXMK
        return true
    }

    override fun isConfigurationFromContext(
        runConfiguration: LatexmkRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val mainFile = runConfiguration.mainFile
        val psiFile = context.dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        val currentFile = psiFile.virtualFile ?: return false
        return mainFile?.path == currentFile.path
    }
}
