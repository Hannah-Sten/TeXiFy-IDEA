package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.LatexTemplateConfigurationFactory
import nl.hannahsten.texifyidea.run.compiler.bibtex.BiberCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibtexCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationOptions
import nl.hannahsten.texifyidea.util.allCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.hasBibliography
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.usesBiber

object BibliographyCompileStepProvider : StepProvider {

    override val name = "Bibliography"

    override val icon = TexifyIcons.BUILD_BIB

    override val id = "bibliography"

    override fun createStep(configuration: LatexRunConfiguration) = BibliographyCompileStep(this, configuration)

    // todo step creation
    fun createStepIfNeeded() {
//        if (!runConfig.getConfigOptions().hasBeenRun) {
//            // Only at this moment we know the user really wants to run the run configuration, so only now we do the expensive check of
//            // checking for bibliography commands
//            if (runConfig.bibRunConfigs.isEmpty() && !compiler.includesBibtex) {
//                runConfig.generateBibRunConfig()
//
//                runConfig.bibRunConfigs.forEach {
//                    val bibSettings = it
//
//                    // Pass necessary latex run configurations settings to the bibtex run configuration.
//                    (bibSettings.configuration as? BibtexRunConfiguration)?.apply {
//                        // Check if the aux, out, or src folder should be used as bib working dir.
//                        this.bibWorkingDir = runConfig.getAuxilDirectory()
//                    }
//                }
//            }
//        }
    }

    /**
     * Create a new bib run config and add it to the set.
     */
    private fun addBibRunConfig(defaultCompiler: SupportedBibliographyCompiler, mainFile: VirtualFile?, compilerArguments: String? = null, project: Project, options: LatexRunConfigurationOptions) {
        val runManager = RunManagerImpl.getInstanceImpl(project)

        val bibSettings = runManager.createConfiguration(
            "",
            LatexTemplateConfigurationFactory(BibtexRunConfigurationType())
        )

        val bibtexRunConfiguration = bibSettings.configuration as BibtexRunConfiguration

        bibtexRunConfiguration.compiler = defaultCompiler
        if (compilerArguments != null) bibtexRunConfiguration.compilerArguments = compilerArguments
        bibtexRunConfiguration.mainFile = mainFile
        bibtexRunConfiguration.setSuggestedName()

        // On non-MiKTeX systems, add bibinputs for bibtex to work
        if (!options.latexDistribution.isMiktex()) {
            // Only if default, because the user could have changed it after creating the run config but before running
            if (mainFile != null && options.outputPath.getOrCreateOutputPath(mainFile, project) != mainFile.parent) {
                bibtexRunConfiguration.environmentVariables = bibtexRunConfiguration.environmentVariables.with(
                    mapOf(
                        "BIBINPUTS" to mainFile.parent.path,
                        "BSTINPUTS" to mainFile.parent.path + ":"
                    )
                )
            }
        }

        runManager.addConfiguration(bibSettings)

//        bibRunConfigs = bibRunConfigs + setOf(bibSettings)
    }

    /**
     * Generate a Bibtex run configuration, after trying to guess whether the user wants to use bibtex or biber as compiler.
     */
    internal fun generateBibRunConfig(runConfig: LatexRunConfiguration) {
        with(runConfig) {
            // Get a pair of Bib compiler and compiler arguments.
            val compilerFromMagicComment: Pair<SupportedBibliographyCompiler, String>? by lazy {
                val runCommand = psiFile?.allParentMagicComments()
                    ?.value(DefaultMagicKeys.BIBTEXCOMPILER) ?: return@lazy null
                val compilerString = if (runCommand.contains(' ')) {
                    runCommand.let { it.subSequence(0, it.indexOf(' ')) }.trim()
                        .toString()
                }
                else runCommand
                val compiler = CompilerMagic.bibliographyCompilerByExecutableName[compilerString.toLowerCase()] ?: return@lazy null
                val compilerArguments = runCommand.removePrefix(compilerString)
                    .trim()
                Pair(compiler, compilerArguments)
            }

            val defaultCompiler = when {
                compilerFromMagicComment != null -> compilerFromMagicComment!!.first
                psiFile?.hasBibliography() == true -> BibtexCompiler
                psiFile?.usesBiber() == true -> BiberCompiler
                else -> return // Do not auto-generate a bib run config when we can't detect bibtex
            }

            // When chapterbib is used, every chapter has its own bibliography and needs its own run config
            val usesChapterbib = psiFile?.includedPackages()?.contains(LatexPackage.CHAPTERBIB) == true

            if (!usesChapterbib) {
                addBibRunConfig(defaultCompiler, options.mainFile.resolve(), compilerFromMagicComment?.second, project, options)
            }
            else if (psiFile != null) {
                val allBibliographyCommands =
                    psiFile!!.commandsInFileSet().filter { it.name == LatexGenericRegularCommand.BIBLIOGRAPHY.cmd }

                // We know that there can only be one bibliography per top-level \include,
                // however not all of them may contain a bibliography, and the ones
                // that do have one can have it in any included file
                psiFile!!.allCommands()
                    .filter { it.name == LatexGenericRegularCommand.INCLUDE.cmd }
                    .flatMap { command -> command.requiredParameters }
                    .forEach { filename ->
                        // Find all the files of this chapter, then check if any of the bibliography commands appears in a file in this chapter
                        val chapterMainFile = psiFile!!.findFile(filename)
                            ?: return@forEach

                        val chapterFiles = chapterMainFile.referencedFileSet()
                            .toMutableSet().apply { add(chapterMainFile) }

                        val chapterHasBibliography = allBibliographyCommands.any { it.containingFile in chapterFiles }

                        if (chapterHasBibliography) {
                            addBibRunConfig(defaultCompiler, chapterMainFile.virtualFile, compilerFromMagicComment?.second, project, options)
                        }
                    }
            }
        }
    }
}