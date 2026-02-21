package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.nameWithSlash
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber
import java.io.File
import java.util.Locale

internal class LatexAuxChainResolver(private val runConfig: LatexRunConfiguration) {

    fun generateBibRunConfig() {
        val psiFile = runConfig.psiFile?.element ?: return
        val compilerFromMagicComment: Pair<BibliographyCompiler, String>? by lazy {
            val runCommand = psiFile.allParentMagicComments()
                .value(DefaultMagicKeys.BIBTEXCOMPILER) ?: return@lazy null
            val compilerString = if (runCommand.contains(' ')) {
                runCommand.let { it.subSequence(0, it.indexOf(' ')) }.trim().toString()
            }
            else runCommand
            val compiler = BibliographyCompiler.valueOf(compilerString.uppercase(Locale.getDefault()))
            val compilerArguments = runCommand.removePrefix(compilerString).trim()
            Pair(compiler, compilerArguments)
        }

        val defaultCompiler = when {
            compilerFromMagicComment != null -> compilerFromMagicComment!!.first
            psiFile.hasBibliography() -> BibliographyCompiler.BIBTEX
            psiFile.usesBiber() -> BibliographyCompiler.BIBER
            else -> return
        }

        val usesChapterbib = psiFile.includedPackagesInFileset().contains(LatexLib.CHAPTERBIB)

        if (!usesChapterbib) {
            addBibRunConfig(defaultCompiler, runConfig.mainFile, compilerFromMagicComment?.second)
            return
        }

        val allBibliographyCommands = NewCommandsIndex.getByNameInFileSet(CommandNames.BIBLIOGRAPHY, psiFile)
        psiFile.traverseCommands()
            .filter { it.nameWithSlash == CommandNames.INCLUDE }
            .flatMap { command -> command.requiredParametersText() }
            .forEach { filename ->
                val chapterMainFile = psiFile.findFile(filename, supportsAnyExtension = true) ?: return@forEach
                val chapterFiles = chapterMainFile.referencedFileSet().toMutableSet().apply { add(chapterMainFile) }
                val chapterHasBibliography = allBibliographyCommands.any { it.containingFile in chapterFiles }
                if (chapterHasBibliography) {
                    addBibRunConfig(defaultCompiler, chapterMainFile.virtualFile, compilerFromMagicComment?.second)
                }
            }
    }

    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> = runConfig.bibRunConfigs + runConfig.makeindexRunConfigs + runConfig.externalToolRunConfigs

    private fun addBibRunConfig(defaultCompiler: BibliographyCompiler, mainFile: VirtualFile?, compilerArguments: String? = null) {
        val runManager = RunManagerImpl.getInstanceImpl(runConfig.project)

        val bibSettings = runManager.createConfiguration(
            "",
            LatexConfigurationFactory(BibtexRunConfigurationType())
        )

        val bibtexRunConfiguration = bibSettings.configuration as BibtexRunConfiguration

        bibtexRunConfiguration.compiler = defaultCompiler
        if (compilerArguments != null) bibtexRunConfiguration.compilerArguments = compilerArguments
        bibtexRunConfiguration.mainFile = mainFile
        bibtexRunConfiguration.setSuggestedName()
        bibtexRunConfiguration.setDefaultDistribution(runConfig.getLatexDistributionType())

        if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile)) {
            if (mainFile != null && LatexPathResolver.resolveOutputDir(runConfig) != mainFile.parent) {
                bibtexRunConfiguration.environmentVariables = bibtexRunConfiguration.environmentVariables.with(
                    mapOf(
                        "BIBINPUTS" to mainFile.parent.path,
                        "BSTINPUTS" to mainFile.parent.path + File.pathSeparator
                    )
                )
            }
        }

        runManager.addConfiguration(bibSettings)
        runConfig.bibRunConfigs += setOf(bibSettings)
    }
}
