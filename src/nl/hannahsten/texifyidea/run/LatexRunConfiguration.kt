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
import com.intellij.ide.macro.MacroManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
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
import nl.hannahsten.texifyidea.run.legacy.LatexCommandLineState
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewers
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.step.CompileStep
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.run.ui.LatexOutputPath
import nl.hannahsten.texifyidea.run.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.allCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
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
) : RunConfigurationBase<LatexCommandLineState>(project, factory, name), LocatableConfiguration, CommonProgramRunConfigurationParameters {

    companion object {

        private const val TEXIFY_PARENT = "texify"
        private const val COMPILER_PATH = "compiler-path"
        private const val SUMATRA_PATH = "sumatra-path"
        private const val PDF_VIEWER = "pdf-viewer"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val MAIN_FILE = "main-file"
        private const val OUTPUT_PATH = "output-path"
        private const val AUXIL_PATH = "auxil-path"
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val BIB_RUN_CONFIG = "bib-run-config"
        private const val MAKEINDEX_RUN_CONFIG = "makeindex-run-config"
        private const val COMPILE_STEP = "compile-step"
        private const val COMPILE_STEP_NAME_ATTR = "name"

        // For backwards compatibility
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
    }

    var compilerPath: String? = null // todo replaced by custom compiler?
    var sumatraPath: String? = null
    var pdfViewer: PdfViewer? = null
    var viewerCommand: String? = null

    // todo this isn't used anymore? replaced by get/setEnvs()
    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    /** Resolves to [mainFile], if resolvable. Can contain macros. */
    var mainFileString: String? = null

    // Cached resolved value of mainFileString
    var mainFile: VirtualFile? = null
        // Private to keep mainFileString up to date
        private set(value) {
            field = value
            this.outputPath.mainFile = value
            this.outputPath.contentRoot = getMainFileContentRoot()
        }

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: PsiFile? = null

    /** Path to the directory containing the output files. */
    var outputPath = LatexOutputPath("out", getMainFileContentRoot(), mainFile, project)

    /** Path to the directory containing the auxiliary files. */
    var auxilPath = LatexOutputPath("auxil", getMainFileContentRoot(), mainFile, project)

    var compileTwice = false
    var outputFormat: OutputFormat = OutputFormat.PDF

    /**
     * Use [getLatexDistributionType] to take the Project SDK into account.
     */
    internal var latexDistribution = LatexDistributionType.PROJECT_SDK

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig = false
    var isFirstRunConfig = true

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun = false

    /** Whether the pdf viewer is allowed to claim focus after compilation. */
    var allowFocusChange = true

    private var bibRunConfigIds = mutableSetOf<String>()
    var bibRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = bibRunConfigIds.mapNotNull {
            RunManagerImpl.getInstanceImpl(project).getConfigurationById(it)
        }.toSet()
        set(bibRunConfigs) {
            bibRunConfigIds = mutableSetOf()
            bibRunConfigs.forEach {
                bibRunConfigIds.add(it.uniqueID)
            }
        }

    private var makeindexRunConfigIds = mutableSetOf<String>()
    var makeindexRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = makeindexRunConfigIds.mapNotNull {
            RunManagerImpl.getInstanceImpl(project).getConfigurationById(it)
        }.toSet()
        set(makeindexRunConfigs) {
            makeindexRunConfigIds = mutableSetOf()
            makeindexRunConfigs.forEach {
                makeindexRunConfigIds.add(it.uniqueID)
            }
        }

    private var externalToolRunConfigIds = mutableSetOf<String>()
    var externalToolRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = externalToolRunConfigIds.mapNotNull {
            RunManagerImpl.getInstanceImpl(project).getConfigurationById(it)
        }.toSet()
        set(externalToolRunConfigs) {
            externalToolRunConfigIds = mutableSetOf()
            externalToolRunConfigs.forEach {
                externalToolRunConfigIds.add(it.uniqueID)
            }
        }

    // In order to propagate information about which files need to be cleaned up at the end between one run of the run config
    // (for example makeindex) and the last run, we save this information temporarily here while the run configuration is running.
    val filesToCleanUp = mutableListOf<File>()

    val compileSteps: MutableList<CompileStep> = mutableListOf()

    override fun getDefaultOptionsClass(): Class<out LatexRunConfigurationOptions> {
        // Data holder for the options
        return LatexRunConfigurationOptions::class.java
    }

    override fun getOptions(): LatexRunConfigurationOptions {
        return super.getOptions() as LatexRunConfigurationOptions
    }

    fun getConfigOptions() = options

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return LatexSettingsEditor(this)
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (getConfigOptions().compiler == null) {
            throw RuntimeConfigurationError(
                "Run configuration is invalid: no compiler selected"
            )
        }
        if (mainFile == null) {
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
        environmentVariables = EnvironmentVariablesData.readExternal(parent)

        // Read main file.
        val filePath = parent.getChildText(MAIN_FILE)
        setMainFile(filePath)

        // Read output path
        val outputPathString = parent.getChildText(OUTPUT_PATH)
        if (outputPathString != null) {
            if (outputPathString.endsWith("/bin")) {
                this.outputPath = LatexOutputPath("out", getMainFileContentRoot(), mainFile, project)
            }
            else {
                this.outputPath = LatexOutputPath("out", getMainFileContentRoot(), mainFile, project)
                this.outputPath.pathString = outputPathString
            }
        }

        // Read auxil path
        val auxilPathString = parent.getChildText(AUXIL_PATH)
        if (auxilPathString != null) {
            this.auxilPath = LatexOutputPath("auxil", getMainFileContentRoot(), mainFile, project)
            this.auxilPath.pathString = auxilPathString
        }

        // Backwards compatibility
        runReadAction {
            val auxDirBoolean = parent.getChildText(AUX_DIR)
            if (auxDirBoolean != null && this.auxilPath.virtualFile == null && this.mainFile != null) {
                // If there is no auxil path yet but this option still exists,
                // guess the output path in the same way as it was previously done
                val usesAuxDir = java.lang.Boolean.parseBoolean(auxDirBoolean)
                val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
                val path = if (usesAuxDir) moduleRoot?.path + "/auxil" else this.mainFile!!.parent.path
                this.auxilPath.virtualFile = LocalFileSystem.getInstance().findFileByPath(path)
            }
            val outDirBoolean = parent.getChildText(OUT_DIR)
            if (outDirBoolean != null && this.outputPath.virtualFile == null && this.mainFile != null) {
                val usesOutDir = java.lang.Boolean.parseBoolean(outDirBoolean)
                val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
                val path = if (usesOutDir) moduleRoot?.path + "/out" else this.mainFile!!.parent.path
                this.outputPath.virtualFile = LocalFileSystem.getInstance().findFileByPath(path)
            }
        }

        // Read whether to compile twice
        val compileTwiceBoolean = parent.getChildText(COMPILE_TWICE)
        if (compileTwiceBoolean == null) {
            this.compileTwice = false
        }
        else {
            this.compileTwice = compileTwiceBoolean.toBoolean()
        }

        // Read output format.
        this.outputFormat = OutputFormat.byNameIgnoreCase(parent.getChildText(OUTPUT_FORMAT))

        // Read LatexDistribution
        this.latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))

        // Read whether the run config has been run
        this.hasBeenRun = parent.getChildText(HAS_BEEN_RUN)?.toBoolean() ?: false

        // Read bibliography run configurations, which is a list of ids
        val bibRunConfigElt = parent.getChildText(BIB_RUN_CONFIG)
        // Assume the list is of the form [id 1,id 2]
        this.bibRunConfigIds = bibRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()

        // Read makeindex run configurations
        val makeindexRunConfigElt = parent.getChildText(MAKEINDEX_RUN_CONFIG)
        if (makeindexRunConfigElt != null) {
            this.makeindexRunConfigIds = makeindexRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()
        }

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
        this.environmentVariables.writeExternal(parent)
        parent.addContent(Element(MAIN_FILE).also { it.text = mainFileString ?: "" })
        parent.addContent(Element(OUTPUT_PATH).also { it.text = outputPath.virtualFile?.path ?: outputPath.pathString })
        parent.addContent(Element(AUXIL_PATH).also { it.text = auxilPath.virtualFile?.path ?: auxilPath.pathString })
        parent.addContent(Element(COMPILE_TWICE).also { it.text = compileTwice.toString() })
        parent.addContent(Element(OUTPUT_FORMAT).also { it.text = outputFormat.name })
        parent.addContent(Element(LATEX_DISTRIBUTION).also { it.text = latexDistribution.name })
        parent.addContent(Element(HAS_BEEN_RUN).also { it.text = hasBeenRun.toString() })
        parent.addContent(Element(BIB_RUN_CONFIG).also { it.text = bibRunConfigIds.toString() })
        parent.addContent(Element(MAKEINDEX_RUN_CONFIG).also { it.text = makeindexRunConfigIds.toString() })

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
        if (!latexDistribution.isMiktex()) {
            // Only if default, because the user could have changed it after creating the run config but before running
            if (mainFile != null && outputPath.virtualFile != mainFile.parent) {
                bibtexRunConfiguration.environmentVariables = bibtexRunConfiguration.environmentVariables.with(mapOf("BIBINPUTS" to mainFile.parent.path, "BSTINPUTS" to mainFile.parent.path + ":"))
            }
        }

        runManager.addConfiguration(bibSettings)

        bibRunConfigs = bibRunConfigs + setOf(bibSettings)
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
            addBibRunConfig(defaultCompiler, mainFile, compilerFromMagicComment?.second)
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
        return bibRunConfigs + makeindexRunConfigs + externalToolRunConfigs
    }

    /**
     * Looks up the corresponding [VirtualFile] and sets [LatexRunConfiguration.mainFile].
     */
    fun setMainFile(mainFilePathWithMacro: String, context: DataContext = DataContext.EMPTY_CONTEXT) {
        // Save the original string, for UI representation
        mainFileString = mainFilePathWithMacro

        // todo does it work if running a file directly after opening project? since context is only given when saving a run config?
        val mainFilePath = MacroManager.getInstance().expandMacrosInString(mainFilePathWithMacro, true, context) ?: return
        // Check if the file is valid and exists
        val mainFile = LocalFileSystem.getInstance().findFileByPath(mainFilePath)
        if (mainFile?.extension == "tex") {
            this.mainFile = mainFile
            return
        }
        else {
            // Maybe it is a relative path
            // Possibly we could use more information from the given context
            if (!Path.of(mainFilePath).isAbsolute) {
                ProjectRootManager.getInstance(project).contentRoots.forEach {
                    val file = it.findFileByRelativePath(mainFilePath)
                    if (file?.extension == "tex") {
                        this.mainFile = file
                        return
                    }
                }
            }
        }

        this.mainFile = null
    }

    fun setDefaultPdfViewer() {
        pdfViewer = InternalPdfViewer.firstAvailable()
    }

    fun setDefaultOutputFormat() {
        outputFormat = OutputFormat.PDF
    }

    fun setDefaultDistribution(project: Project) {
        latexDistribution = LatexSdkUtil.getDefaultLatexDistributionType(project)
    }

    /**
     * Get LaTeX distribution type, when 'Use project SDK' is selected map it to a [LatexDistributionType].
     */
    fun getLatexDistributionType(): LatexDistributionType {
        return if (latexDistribution != LatexDistributionType.PROJECT_SDK) {
            latexDistribution
        }
        else {
            LatexSdkUtil.getLatexProjectSdkType(project)?.getLatexDistributionType() ?: LatexDistributionType.TEXLIVE
        }
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder when used.
     */
    fun getAuxilDirectory(): VirtualFile? {
        return if (latexDistribution.isMiktex()) {
            auxilPath.getAndCreatePath()
        }
        else {
            outputPath.getAndCreatePath()
        }
    }

    fun setSuggestedName() {
        setName(suggestedName())
    }

    override fun isGeneratedName(): Boolean {
        if (mainFile == null) {
            return false
        }

        val name = mainFile!!.nameWithoutExtension
        return name == getName()
    }

    // Path to output file (e.g. pdf)
    override fun getOutputFilePath(): String {
        val outputDir = outputPath.getAndCreatePath()
        return "${outputDir?.path}/" + mainFile!!
            .nameWithoutExtension + "." + if (outputFormat == OutputFormat.DEFAULT) "pdf" else outputFormat.toString()
            .toLowerCase()
    }

    /**
     * Set [outputPath]
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        this.outputPath.virtualFile = findVirtualFileByAbsoluteOrRelativePath(fileOutputPath, project)
        this.outputPath.pathString = fileOutputPath
    }

    /**
     * Get the content root of the main file.
     */
    fun getMainFileContentRoot(): VirtualFile? {
        if (mainFile == null) return null
        return runReadAction {
            return@runReadAction ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
        }
    }

    /**
     * Set [auxilPath]
     */
    fun setFileAuxilPath(fileAuxilPath: String) {
        if (fileAuxilPath.isBlank()) return
        this.auxilPath.virtualFile = findVirtualFileByAbsoluteOrRelativePath(fileAuxilPath, project)
        // Update backing string (will be used as path in the UI, to enable macro support)
        this.auxilPath.pathString = fileAuxilPath
    }

    /**
     * Whether an auxil or out directory is used, i.e. whether not both are set to the directory of the main file
     */
    fun usesAuxilOrOutDirectory(): Boolean {
        val usesAuxilDir = auxilPath.getAndCreatePath() != mainFile?.parent
        val usesOutDir = outputPath.getAndCreatePath() != mainFile?.parent

        return usesAuxilDir || usesOutDir
    }

    override fun suggestedName(): String? {
        return if (mainFile == null) {
            null
        }
        else {
            mainFile!!.nameWithoutExtension
        }
    }

    override fun toString(): String {
        return "LatexRunConfiguration{" + "compiler=" + getConfigOptions().compiler +
                ", compilerPath=" + compilerPath +
            ", sumatraPath=" + sumatraPath +
            ", mainFile=" + mainFile +
            ", outputFormat=" + outputFormat +
            '}'.toString()
    }

    // Explicitly deep clone references, otherwise a copied run config has references to the original objects
    override fun clone(): RunConfiguration {
        return super.clone().also {
            val runConfiguration = it as? LatexRunConfiguration ?: return@also
            runConfiguration.outputPath = this.outputPath.clone()
            runConfiguration.auxilPath = this.auxilPath.clone()
        }
    }

    override fun getProgramParameters() = options.compilerArguments

    override fun setProgramParameters(value: String?) {
        options.compilerArguments = value
    }

    override fun getWorkingDirectory(): String? = options.workingDirectory ?: PathUtil.toSystemDependentName(project.basePath)

    override fun setWorkingDirectory(value: String?) {
        val normalized = PathUtil.toSystemIndependentName(value?.ifBlank { null }?.trim())
        options.workingDirectory = if (normalized != project.basePath) normalized else null
    }

    fun hasDefaultWorkingDirectory(): Boolean {
        if (workingDirectory == null || mainFile == null) return false
        return Path.of(workingDirectory).toAbsolutePath() == Path.of(mainFile?.path).parent.toAbsolutePath()
    }

    fun hasDefaultOutputFormat() = outputFormat == OutputFormat.PDF

    fun hasDefaultLatexDistribution() = latexDistribution == LatexDistributionType.PROJECT_SDK

    override fun getEnvs() = options.env

    override fun setEnvs(envs: MutableMap<String, String>) {
        options.env = envs
    }

    override fun isPassParentEnvs() = options.isPassParentEnv

    override fun setPassParentEnvs(passParentEnvs: Boolean) {
        options.isPassParentEnv = passParentEnvs
    }
}
