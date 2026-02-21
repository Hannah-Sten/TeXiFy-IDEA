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
        val (container, mainFile) = resolveLatexContextFile(context) ?: return false

        if (!isTexFile(mainFile)) {
            return false
        }

        // Change the main file as given by the template run configuration to the current file
        runConfiguration.mainFilePath = LatexRunConfigurationStaticSupport.toProjectRelativePathOrAbsolute(runConfiguration, mainFile)
        runConfiguration.psiFile = container.createSmartPointer()
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
        runConfiguration.compiler = LatexCompiler.byExecutableName(compiler)
        runConfiguration.compilerArguments = command.removePrefix(compiler).trim()
        if (runConfiguration.compiler == LatexCompiler.LATEXMK) {
            runConfiguration.latexmkCompileMode = LatexmkCompileMode.AUTO
        }

        runConfiguration.workingDirectory = if (runConfiguration.compiler == LatexCompiler.TECTONIC && mainFile.hasTectonicTomlFile()) {
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
    ): Boolean = isSameContextFile(LatexRunConfigurationStaticSupport.resolveMainFile(runConfiguration), context)
}
