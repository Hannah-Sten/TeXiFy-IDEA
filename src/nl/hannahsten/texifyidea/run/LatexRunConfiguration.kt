package nl.hannahsten.texifyidea.run

import com.intellij.configurationStore.deserializeAndLoadState
import com.intellij.configurationStore.serializeStateInto
import com.intellij.execution.CommonProgramRunConfigurationParameters
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.PathUtil
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.compiler.bibtex.BiberCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibtexCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler.OutputFormat
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewers
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.step.CompileStep
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.run.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.allCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.hasBibliography
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.usesBiber
import org.jdom.Element
import java.io.File
import java.nio.file.Path

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration constructor(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexRunState>(project, factory, name), LocatableConfiguration, CommonProgramRunConfigurationParameters {

    companion object {

        private const val TEXIFY_PARENT = "texify"
        private const val COMPILER_PATH = "compiler-path"
        private const val SUMATRA_PATH = "sumatra-path"
        private const val PDF_VIEWER = "pdf-viewer"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val COMPILE_STEP = "compile-step"
        private const val COMPILE_STEP_NAME_ATTR = "name"
    }

    var compilerPath: String? = null // todo replaced by custom compiler?

    var sumatraPath: String? = null // todo merge with CustomPdfViewer
    var pdfViewer: PdfViewer? = null
    var viewerCommand: String? = null // todo similar to CustomCompiler -> CustomPdfViewer

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: PsiFile? = null

    // In order to propagate information about which files need to be cleaned up at the end between one step
    // (for example makeindex) and the last step, we save this information temporarily here while the run configuration is running. todo check this
    val filesToCleanUp = mutableListOf<File>()

    /** A run configuration consists of one or more steps to execute. */
    val compileSteps: MutableList<CompileStep> = mutableListOf()

    override fun getDefaultOptionsClass(): Class<out LatexRunConfigurationOptions> {
        // Data holder for the options
        return LatexRunConfigurationOptions::class.java
    }

    public override fun getOptions(): LatexRunConfigurationOptions {
        return super.getOptions() as LatexRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return LatexSettingsEditor(this)
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (options.compiler == null) {
            throw RuntimeConfigurationError(
                "Run configuration is invalid: no compiler selected"
            )
        }
        if (options.mainFile.resolve() == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
        if (compileSteps.isEmpty()) {
            throw RuntimeConfigurationError("At least one compile step needs to be present")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        // todo
//        val filter = RegexpFilter(
//            environment.project,
//            "^\$FILE_PATH$:\$LINE$"
//        )
//
//        val state = LatexCommandLineState(
//            environment,
//            this
//        )
//        state.addConsoleFilters(filter)
//        return state

        // ExternalSystemRunnableState, GradleRunConfiguration, BuildView, MultipleBuildView
        return LatexRunState(this, environment)
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        val parent = element.getChild(TEXIFY_PARENT) ?: return

        // Read compiler custom path.
        val compilerPathRead = parent.getChildText(COMPILER_PATH)
        this.compilerPath = if (compilerPathRead.isNullOrEmpty()) null else compilerPathRead

        // Read SumatraPDF custom path
        val sumatraPathRead = parent.getChildText(SUMATRA_PATH)
        this.sumatraPath = if (sumatraPathRead.isNullOrEmpty()) null else sumatraPathRead

        // Read pdf viewer.
        val viewerName = parent.getChildText(PDF_VIEWER)
        try {
            this.pdfViewer = ExternalPdfViewers.getExternalPdfViewers().firstOrNull { it.name == viewerName }
                ?: InternalPdfViewer.valueOf(viewerName ?: "")
        }
        catch (e: IllegalArgumentException) {
            // Try to recover from old settings (when the pdf viewer was set in the TeXiFy settings instead of the run config).
            this.pdfViewer = TexifySettings.getInstance().pdfViewer
        }

        // Read custom pdf viewer command
        val viewerCommandRead = parent.getChildText(VIEWER_COMMAND)
        this.viewerCommand = if (viewerCommandRead.isNullOrEmpty()) null else viewerCommandRead

        // Read environment variables
        options.environmentVariables = EnvironmentVariablesData.readExternal(parent)

        // Read bibliography run configurations, which is a list of ids
//        val bibRunConfigElt = parent.getChildText(BIB_RUN_CONFIG)
        // Assume the list is of the form [id 1,id 2]
        // todo create bibsteps
//        this.bibRunConfigIds = bibRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()

        // Read makeindex run configurations
//        val makeindexRunConfigElt = parent.getChildText(MAKEINDEX_RUN_CONFIG)
//        if (makeindexRunConfigElt != null) {
            // todo create makeindex steps
//            this.makeindexRunConfigIds = makeindexRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()
//        }

        // Read compile steps
        // This should be the last option that is read, as it may depend on other options.
        for (compileStepElement in parent.getChildren(COMPILE_STEP)) {
            val key = compileStepElement.getAttributeValue(COMPILE_STEP_NAME_ATTR)
            val provider = CompilerMagic.compileStepProviders[key] ?: continue

            val step = provider.createStep(this)
            if (step is PersistentStateComponent<*>) {
                deserializeAndLoadState(step, compileStepElement)
            }

            this.compileSteps.add(step)
        }
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        var parent: Element? = element.getChild(TEXIFY_PARENT)

        // Create a new parent when there is no parent present.
        if (parent == null) {
            parent = Element(TEXIFY_PARENT)
            element.addContent(parent)
        }
        else {
            // Otherwise overwrite (remove + write).
            parent.removeContent()
        }

        parent.addContent(Element(COMPILER_PATH).also { it.text = compilerPath ?: "" })
        parent.addContent(Element(SUMATRA_PATH).also { it.text = sumatraPath ?: "" })
        parent.addContent(Element(PDF_VIEWER).also { it.text = pdfViewer?.name ?: "" })
        parent.addContent(Element(VIEWER_COMMAND).also { it.text = viewerCommand ?: "" })
        this.options.environmentVariables.writeExternal(parent)

        for (step in compileSteps) {
            val stepElement = Element(COMPILE_STEP)
            stepElement.setAttribute(COMPILE_STEP_NAME_ATTR, step.provider.id)

            if (step is PersistentStateComponent<*>) {
                serializeStateInto(step, stepElement)
            }

            parent.addContent(stepElement)
        }
    }

    /**
     * Create a new bib run config and add it to the set.
     */
    private fun addBibRunConfig(defaultCompiler: SupportedBibliographyCompiler, mainFile: VirtualFile?, compilerArguments: String? = null) {
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
    internal fun generateBibRunConfig() {
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
            addBibRunConfig(defaultCompiler, options.mainFile.resolve(), compilerFromMagicComment?.second)
        }
        else if (psiFile != null) {
            val allBibliographyCommands = psiFile!!.commandsInFileSet().filter { it.name == LatexGenericRegularCommand.BIBLIOGRAPHY.cmd }

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
                        addBibRunConfig(defaultCompiler, chapterMainFile.virtualFile, compilerFromMagicComment?.second)
                    }
                }
        }
    }

    /**
     * All run configs in the chain except the LaTeX ones.
     */
    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> {
//        return bibRunConfigs + makeindexRunConfigs + externalToolRunConfigs
        return emptySet()
    }

    fun setDefaultPdfViewer() {
        pdfViewer = InternalPdfViewer.firstAvailable()
    }

    /**
     * Get LaTeX distribution type, when 'Use project SDK' is selected map it to a [LatexDistributionType].
     */
    fun getLatexDistributionType(): LatexDistributionType {
        return if (options.latexDistribution != LatexDistributionType.PROJECT_SDK) {
            options.latexDistribution
        }
        else {
            LatexSdkUtil.getLatexProjectSdkType(project)?.getLatexDistributionType() ?: LatexDistributionType.TEXLIVE
        }
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder.
     */
    fun getAuxilDirectory(): VirtualFile? {
        return if (options.latexDistribution.isMiktex()) {
            options.auxilPath.getOrCreateOutputPath(options.mainFile.resolve(), project)
        }
        else {
            options.outputPath.getOrCreateOutputPath(options.mainFile.resolve(), project)
        }
    }

    fun setSuggestedName() {
        setName(suggestedName())
    }

    override fun isGeneratedName(): Boolean {
        if (options.mainFile.resolve() == null) {
            return false
        }

        val name = options.mainFile.resolve()!!.nameWithoutExtension
        return name == getName()
    }

    // Path to output file (e.g. pdf)
    override fun getOutputFilePath() = options.outputPath.getOutputFilePath(options, project)

    /**
     * Set output path (should be a directory). Should NOT contain macros (use [LatexRunConfigurationDirectoryOption.resolveAndSetPath] for that).
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        options.outputPath.setPath(fileOutputPath)
    }

    /**
     * Get the content root of the main file.
     */
    fun getMainFileContentRoot(): VirtualFile? {
        if (options.mainFile.resolve() == null) return null
        return runReadAction {
            return@runReadAction ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(options.mainFile.resolve()!!)
        }
    }

    override fun suggestedName(): String? {
        return if (options.mainFile.resolve() == null) {
            null
        }
        else {
            options.mainFile.resolve()!!.nameWithoutExtension
        }
    }

    override fun toString(): String {
        return "LatexRunConfiguration{" + "compiler=" + options.compiler +
                ", compilerPath=" + compilerPath +
                ", sumatraPath=" + sumatraPath +
                ", mainFile=" + options.mainFile.resolve() +
                ", outputFormat=" + options.outputFormat +
                '}'.toString()
    }

    // Explicitly deep clone references, otherwise a copied run config has references to the original objects
    // todo check if still needed, I don't think so
//    override fun clone(): RunConfiguration {
//        return super.clone().also {
//            val runConfiguration = it as? LatexRunConfiguration ?: return@also
//            runConfiguration.outputPath = this.outputPath.clone()
//            runConfiguration.auxilPath = this.auxilPath.clone()
//        }
//    }

    override fun getProgramParameters() = options.compilerArguments

    override fun setProgramParameters(value: String?) {
        options.compilerArguments = value
    }

    override fun getWorkingDirectory(): String? = options.workingDirectory.resolvedPath ?: PathUtil.toSystemDependentName(project.basePath)

    override fun setWorkingDirectory(resolvedPath: String?) {
        options.workingDirectory.setPath(resolvedPath)
    }

    fun hasDefaultWorkingDirectory(): Boolean {
        if (workingDirectory == null || options.mainFile.resolve() == null) return false
        return Path.of(workingDirectory).toAbsolutePath() == Path.of(options.mainFile.resolve()?.path).parent.toAbsolutePath()
    }

    fun hasDefaultOutputFormat() = options.outputFormat == OutputFormat.PDF

    fun hasDefaultLatexDistribution() = options.latexDistribution == LatexDistributionType.PROJECT_SDK

    override fun getEnvs() = options.env

    override fun setEnvs(envs: MutableMap<String, String>) {
        options.env = envs
    }

    override fun isPassParentEnvs() = options.isPassParentEnv

    override fun setPassParentEnvs(passParentEnvs: Boolean) {
        options.isPassParentEnv = passParentEnvs
    }
}
