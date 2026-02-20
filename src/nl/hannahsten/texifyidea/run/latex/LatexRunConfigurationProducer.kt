package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.common.isSameContextFile
import nl.hannahsten.texifyidea.run.common.isTexFile
import nl.hannahsten.texifyidea.run.common.resolveLatexContextFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.findTectonicTomlFile
import nl.hannahsten.texifyidea.util.files.hasTectonicTomlFile
import nl.hannahsten.texifyidea.util.includedPackagesInFileset

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
        if (!isLatexRunConfigurationEnabled(TexifySettings.getState().runConfigLatexmkMode)) {
            return false
        }

        val (container, mainFile) = resolveLatexContextFile(context) ?: return false

        if (!isTexFile(mainFile)) {
            return false
        }

        // Change the main file as given by the template run configuration to the current file
        runConfiguration.mainFile = mainFile
        runConfiguration.psiFile = container.createSmartPointer()
        runConfiguration.setSuggestedName()
        // Avoid changing the outputPath of the template run config (which is a shallow clone)
        runConfiguration.outputPath = runConfiguration.outputPath.clone()
        runConfiguration.auxilPath = runConfiguration.auxilPath.clone()

        val runCommand = container.allParentMagicComments().value(DefaultMagicKeys.COMPILER)
        val runProgram = container.allParentMagicComments().value(DefaultMagicKeys.PROGRAM)
        // citation-style-language package should use lualatex by default, unless overridden by magic comments above.
        val csl = if (container.includedPackagesInFileset().contains(LatexLib.CITATION_STYLE_LANGUAGE)) LatexCompiler.LUALATEX.name else null
        val command = runCommand ?: runProgram ?: csl ?: return true
        val compiler = if (command.contains(' ')) {
            command.let { it.subSequence(0, it.indexOf(' ')) }.trim().toString()
        }
        else command
        runConfiguration.compiler = LatexCompiler.byExecutableName(compiler)
        runConfiguration.compilerArguments = command.removePrefix(compiler).trim()

        runConfiguration.workingDirectory = if (runConfiguration.compiler == LatexCompiler.TECTONIC && mainFile.hasTectonicTomlFile()) mainFile.findTectonicTomlFile()!!.parent.path else LatexOutputPath.MAIN_FILE_STRING
        return true
    }

    override fun isConfigurationFromContext(
        runConfiguration: LatexRunConfiguration,
        context: ConfigurationContext
    ): Boolean = isSameContextFile(runConfiguration.mainFile, context)
}

internal fun isLatexRunConfigurationEnabled(mode: TexifySettings.RunConfigLatexmkMode): Boolean =
    mode != TexifySettings.RunConfigLatexmkMode.LATEXMK_ONLY
