package nl.hannahsten.texifyidea.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.compiler.latex.PdflatexCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.SupportedLatexCompiler
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractOutputPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import nl.hannahsten.texifyidea.run.step.LatexCompileStepProvider
import nl.hannahsten.texifyidea.run.step.PdfViewerStep
import nl.hannahsten.texifyidea.run.step.PdfViewerStepProvider

/**
 * Create run configurations from context (from inside a LaTeX file).
 *
 * @author Hannah Schellekens
 */
class LatexRunConfigurationProducer : LazyRunConfigurationProducer<LatexRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return LatexTemplateConfigurationFactory(latexRunConfigurationType())
    }

    override fun setupConfigurationFromContext(
        runConfiguration: LatexRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val container = location.psiElement.containingFile ?: return false
        val mainFile = container.virtualFile ?: return false

        // Only activate on .tex files.
        val extension = mainFile.extension
        val texTension = LatexFileType.defaultExtension
        if (extension == null || !extension.equals(texTension, ignoreCase = true)) {
            return false
        }

        // Change the main file as given by the template run configuration to the current file
        runConfiguration.options.mainFile = LatexRunConfigurationPathOption(mainFile.path)
        runConfiguration.options.workingDirectory = LatexRunConfigurationPathOption.createDefaultWorkingDirectory(mainFile)
        runConfiguration.options.outputPath = LatexRunConfigurationAbstractOutputPathOption.getDefault("out", runConfiguration.project)
        runConfiguration.options.auxilPath = LatexRunConfigurationAbstractOutputPathOption.getDefault("auxil", runConfiguration.project)
        runConfiguration.options.compilerArguments = runConfiguration.options.compiler?.defaultArguments
        runConfiguration.psiFile = container
        runConfiguration.setSuggestedName()

        // Make sure the run configuration is at least valid
        if (runConfiguration.compileSteps.isEmpty()) {
            runConfiguration.compileSteps.add(LatexCompileStepProvider.createStep(runConfiguration))
            runConfiguration.compileSteps.add(PdfViewerStepProvider.createStep(runConfiguration))
        }
        else {
            // Clone the steps from the template so the template is not changed when editing these steps.
            val clonedSteps = runConfiguration.compileSteps.map { it.clone() }
            runConfiguration.compileSteps.clear()
            runConfiguration.compileSteps.addAll(clonedSteps)
        }
        runConfiguration.compileSteps.forEach { it.configuration = runConfiguration }

        // Set default pdf path to the pdf corresponding to the main file
        // Maybe we shouldn't do this if the user set a path in the template run config though
        runConfiguration.compileSteps.filterIsInstance<PdfViewerStep>().forEach {
            it.state.pdfFilePath = it.getDefaultPdfFilePathWithMacro()
        }

        // Check for magic comments
        val runCommand = container.allParentMagicComments().value(DefaultMagicKeys.COMPILER)
        val runProgram = container.allParentMagicComments().value(DefaultMagicKeys.PROGRAM)
        val command = runCommand ?: runProgram ?: return true
        val compiler = if (command.contains(' ')) {
            command.let { it.subSequence(0, it.indexOf(' ')) }.trim().toString()
        }
        else command
        runConfiguration.options.compiler = SupportedLatexCompiler.byExecutableName(compiler) ?: PdflatexCompiler
        runConfiguration.options.compilerArguments = command.removePrefix(compiler).trim()
        return true
    }

    override fun isConfigurationFromContext(
        runConfiguration: LatexRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val mainFile = runConfiguration.options.mainFile.resolve()
        val psiFile = context.dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        val currentFile = psiFile.virtualFile ?: return false
        return mainFile?.path == currentFile.path
    }
}