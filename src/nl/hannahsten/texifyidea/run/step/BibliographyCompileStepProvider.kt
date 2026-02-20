package nl.hannahsten.texifyidea.run.step

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.framework.base.deepClonePolymorphic
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.lang.predefined.CommandNames
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.bibtex.BiberCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibtexCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber
import java.io.File
import java.util.*

object BibliographyCompileStepProvider : StepProvider {

    override val name = "Bibliography"

    override val icon = TexifyIcons.BUILD_BIB

    override val id = "bibliography"

    override fun createStep(configuration: LatexRunConfiguration): Step {
        val step = BibliographyCompileStep(this, configuration)
        val (compiler, arguments) = guessCompiler(configuration)
        step.state.compiler = compiler
        step.state.compilerArguments = arguments
        step.state.workingDirectory = guessWorkingDirectory(configuration)?.path
        setEnvironmentVariables(configuration, step)
        step.state.mainFileName = configuration.options.mainFile.resolve()?.nameWithoutExtension
        return step
    }

    override fun createIfRequired(runConfiguration: LatexRunConfiguration): List<Step> {
        // This check is expensive, only check if we need to add steps if this is the first time running the run config
        if (runConfiguration.options.lastRun != null) return emptyList()

        // If the user has already added bib steps, we don't second-guess her
        if (runConfiguration.compileSteps.filterIsInstance<BibliographyCompileStep>().isNotEmpty()) return emptyList()

        if (runConfiguration.options.compiler?.includesBibtex == true) return emptyList()

        // todo if (runConfig.mainFile?.psiFile(runConfig.project)?.includedPackagesInFileset()?.contains(LatexPackage.CITATION_STYLE_LANGUAGE)) return emptyList()

        val step = createStep(runConfiguration)
        // If no suitable compiler could be found, assume bibtex is not needed
        if ((step as BibliographyCompileStep).state.compiler == null) return emptyList()

        var steps = listOf(step)

        // If using chapterbib, use those steps instead
        steps = createChapterBibStepsIfRequired(runConfiguration, step).let { it.ifEmpty { steps } }

        return listOf(step)
    }

    /**
     * Check if any environment variables are needed, and add them if so.
     */
    private fun setEnvironmentVariables(runConfig: LatexRunConfiguration, step: BibliographyCompileStep) {
        with(runConfig) {
            // On non-MiKTeX systems, add bibinputs for bibtex to work
            if (!options.latexDistribution.isMiktex(runConfig.project)) {
                val mainFile = options.mainFile.resolve()
                // Only if default, because the user could have changed it after creating the run config but before running
                if (mainFile != null && options.outputPath.getOrCreateOutputPath(mainFile, project) != mainFile.parent) {
                    step.state.envs = step.getEnvironmentVariables().with(
                        mapOf(
                            "BIBINPUTS" to mainFile.parent.path,
                            // As seen in issue 2165, appending a colon (like with TEXINPUTS) may not work on Windows,
                            // however it may be necessary on Mac/Linux as seen in #2249.
                            "BSTINPUTS" to mainFile.parent.path + File.pathSeparator
                        )
                    ).envs
                }
            }
        }
    }

    /**
     * Check if chapterbib is used, and if so create the necessary steps, otherwise return an empty list.
     * This method can take a large amount of time, so it should only be called when the user really wants to run the run config.
     *
     * @param defaultStep Default step, will be used as template (for example, the compiler of this step will be used).
     */
    private fun createChapterBibStepsIfRequired(runConfig: LatexRunConfiguration, defaultStep: BibliographyCompileStep): List<BibliographyCompileStep> {
        val psiFile = runConfig.options.mainFile.resolve()?.psiFile(runConfig.project) ?: return emptyList()

        // When chapterbib is used, every chapter has its own bibliography and needs its own run config
        val usesChapterbib = psiFile.includedPackagesInFileset().contains(LatexLib.CHAPTERBIB)

        if (!usesChapterbib) return emptyList()

        val steps = mutableListOf<BibliographyCompileStep>()

        val allBibliographyCommands = NewCommandsIndex.getByNameInFileSet(CommandNames.BIBLIOGRAPHY, psiFile)

        // We know that there can only be one bibliography per top-level \include,
        // however not all of them may contain a bibliography, and the ones
        // that do have one can have it in any included file
        psiFile.traverseCommands()
            .filter { it.name == CommandNames.INCLUDE }
            .flatMap { command -> command.requiredParameters() }
            .forEach { filename ->
                // Find all the files of this chapter, then check if any of the bibliography commands appears in a file in this chapter
                val chapterMainFile = psiFile.findFile(filename.text, supportsAnyExtension = true)
                    ?: return@forEach

                val chapterFiles = chapterMainFile.referencedFileSet()
                    .toMutableSet().apply { add(chapterMainFile) }

                val chapterHasBibliography = allBibliographyCommands.any { it.containingFile in chapterFiles }

                if (chapterHasBibliography) {
                    // todo does cloning work?
                    steps.add(defaultStep.deepClonePolymorphic().apply { state.mainFileName = chapterMainFile.virtualFile.nameWithoutExtension })
                }
            }
        return steps
    }

    /**
     * Guess which compiler is needed.
     * @return Pair of compiler and extra compiler arguments (not the file itself).
     */
    private fun guessCompiler(runConfig: LatexRunConfiguration): Pair<BibliographyCompiler?, String?> {
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
                val compiler = CompilerMagic.bibliographyCompilerByExecutableName[compilerString.lowercase(Locale.getDefault())] ?: return@lazy null
                val compilerArguments = runCommand.removePrefix(compilerString)
                    .trim()
                Pair(compiler, compilerArguments)
            }

            return when {
                compilerFromMagicComment != null -> compilerFromMagicComment!!
                psiFile?.hasBibliography() == true -> Pair(BibtexCompiler, null)
                psiFile?.usesBiber() == true -> Pair(BiberCompiler, null)
                else -> return Pair(null, null)
            }
        }
    }

    private fun guessWorkingDirectory(runConfig: LatexRunConfiguration): VirtualFile? = runConfig.getAuxilDirectory()
        ?: runConfig.options.mainFile.resolve()?.parent
        ?: runConfig.project.guessProjectDir()
}