package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.util.files.findTectonicTomlFile
import nl.hannahsten.texifyidea.util.files.hasTectonicTomlFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import java.nio.file.Path

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

        val magicComments = container.allParentMagicComments()

        val runCommand = magicComments.value(DefaultMagicKeys.COMPILER)
        val runProgram = magicComments.value(DefaultMagicKeys.PROGRAM)
        // citation-style-language package should use lualatex by default, unless overridden by magic comments above.
        val csl = if (container.includedPackagesInFileset().contains(LatexLib.CITATION_STYLE_LANGUAGE)) LatexCompiler.LUALATEX.name else null
        val command = runCommand ?: runProgram ?: csl ?: return true
        val compiler = if (command.contains(' ')) {
            command.let { it.subSequence(0, it.indexOf(' ')) }.trim().toString()
        }
        else command
        val selectedCompiler = LatexCompiler.byExecutableName(compiler)
        val commandArguments = command.removePrefix(compiler).trim().ifBlank { null }
        when (selectedCompiler) {
            LatexCompiler.LATEXMK -> {
                val step = runConfiguration.ensurePrimaryCompileStepLatexmk()
                step.compilerArguments = commandArguments
                step.latexmkCompileMode = LatexmkCompileMode.AUTO
            }

            else -> {
                val step = runConfiguration.ensurePrimaryCompileStepClassic()
                step.compiler = selectedCompiler
                step.compilerArguments = commandArguments
            }
        }

        runConfiguration.workingDirectory = if (selectedCompiler == LatexCompiler.TECTONIC && mainFile.hasTectonicTomlFile()) {
            Path.of(mainFile.findTectonicTomlFile()!!.parent.path)
        }
        else {
            null
        }
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
