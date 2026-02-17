package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.latex.LatexConfigurationFactory
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.magic.PackageMagic

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
        runConfiguration.outputPathRaw = LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
        runConfiguration.auxilPathRaw = "${LatexmkPathResolver.PROJECT_DIR_PLACEHOLDER}/aux"
        runConfiguration.workingDirectory = null
        runConfiguration.compiler = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.LATEXMK

        val magicComments = container.allParentMagicComments()

        val runCommand = magicComments.value(DefaultMagicKeys.COMPILER)
        val runProgram = magicComments.value(DefaultMagicKeys.PROGRAM)
        val magicEngine = engineFromMagicCommand(runCommand ?: runProgram)
        val packageEngine = preferredEngineForPackages(container.includedPackagesInFileset())
        runConfiguration.engineMode = magicEngine ?: packageEngine ?: LatexmkEngineMode.PDFLATEX
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

internal fun preferredEngineForPackages(packages: Set<LatexLib>): LatexmkEngineMode? {
    if (packages.any { it in PackageMagic.unicodePreferredEnginesPackages }) {
        return LatexmkEngineMode.LUALATEX
    }
    return null
}

internal fun engineFromMagicCommand(command: String?): LatexmkEngineMode? {
    if (command.isNullOrBlank()) return null
    val executable = command.substringBefore(' ').trim().lowercase()
    return when (executable) {
        "pdflatex", "pdflatex.exe", "pdflatex.bin", "pdflatex.cmd" -> LatexmkEngineMode.PDFLATEX
        "xelatex", "xelatex.exe", "xelatex.bin", "xelatex.cmd" -> LatexmkEngineMode.XELATEX
        "lualatex", "lualatex.exe", "lualatex.bin", "lualatex.cmd" -> LatexmkEngineMode.LUALATEX
        "latex", "latex.exe", "latex.bin", "latex.cmd" -> LatexmkEngineMode.LATEX
        else -> null
    }
}
