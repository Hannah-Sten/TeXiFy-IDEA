package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.util.execution.ParametersListUtil
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
        runConfiguration.auxilPathRaw = ""
        runConfiguration.workingDirectory = null
        runConfiguration.compiler = nl.hannahsten.texifyidea.run.compiler.LatexCompiler.LATEXMK

        val magicComments = container.allParentMagicComments()

        val runCommand = magicComments.value(DefaultMagicKeys.COMPILER)
        val runProgram = magicComments.value(DefaultMagicKeys.PROGRAM)
        val magicMode = compileModeFromMagicCommand(runCommand ?: runProgram)
        val packageMode = preferredCompileModeForPackages(container.includedPackagesInFileset())
        runConfiguration.compileMode = magicMode ?: packageMode ?: LatexmkCompileMode.PDFLATEX_PDF
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

internal fun preferredCompileModeForPackages(packages: Set<LatexLib>): LatexmkCompileMode? {
    if (packages.any { it in PackageMagic.unicodePreferredEnginesPackages }) {
        return LatexmkCompileMode.LUALATEX_PDF
    }
    return null
}

internal fun compileModeFromMagicCommand(command: String?): LatexmkCompileMode? {
    if (command.isNullOrBlank()) return null
    val parsed = ParametersListUtil.parse(command).filter { it.isNotBlank() }
    if (parsed.isEmpty()) return null

    val executable = parsed.first().normalizedExecutable()
    val flags = parsed.drop(1).map { it.lowercase() }

    return when (executable) {
        "latexmk" -> compileModeFromLatexmkFlags(flags)
        "pdflatex" -> LatexmkCompileMode.PDFLATEX_PDF
        "xelatex" -> LatexmkCompileMode.XELATEX_PDF
        "lualatex" -> LatexmkCompileMode.LUALATEX_PDF
        "latex" -> LatexmkCompileMode.LATEX_DVI
        "tectonic", "arara" -> null
        else -> null
    }
}

private fun compileModeFromLatexmkFlags(flags: List<String>): LatexmkCompileMode {
    if (flags.any { it == "-pdflatex" || it.startsWith("-pdflatex=") }) {
        return LatexmkCompileMode.CUSTOM
    }
    if (flags.contains("-lualatex")) {
        return LatexmkCompileMode.LUALATEX_PDF
    }
    if (flags.contains("-xelatex")) {
        return if (flags.contains("-xdv")) LatexmkCompileMode.XELATEX_XDV else LatexmkCompileMode.XELATEX_PDF
    }
    if (flags.contains("-latex")) {
        if (flags.contains("-ps")) return LatexmkCompileMode.LATEX_PS
        if (flags.contains("-dvi")) return LatexmkCompileMode.LATEX_DVI
    }

    return LatexmkCompileMode.PDFLATEX_PDF
}

private fun String.normalizedExecutable(): String {
    val fileName = substringAfterLast('/').substringAfterLast('\\')
    return fileName
        .lowercase()
        .removeSuffix(".exe")
        .removeSuffix(".cmd")
        .removeSuffix(".bin")
        .removeSuffix(".bat")
}
