package nl.hannahsten.texifyidea.run.latex

import com.intellij.diagnostic.logging.LogConsoleManagerBase
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.common.addTextChild
import nl.hannahsten.texifyidea.run.common.getOrCreateAndClearParent
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.runInBackgroundNonBlocking
import org.jdom.Element
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexCommandLineState>(project, factory, name), LocatableConfiguration, LatexCompilationRunConfiguration {

    companion object {

        private const val TEXIFY_PARENT = "texify"
        private const val COMPILER = "compiler"
        private const val COMPILER_PATH = "compiler-path"
        private const val SUMATRA_PATH = "sumatra-path"
        private const val PDF_VIEWER = "pdf-viewer"
        private const val REQUIRE_FOCUS = "require-focus"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val COMPILER_ARGUMENTS = "compiler-arguments"
        private const val BEFORE_RUN_COMMAND = "before-run-command"
        private const val MAIN_FILE = "main-file"
        private const val OUTPUT_PATH = "output-path"
        private const val AUXIL_PATH = "auxil-path"
        private const val WORKING_DIRECTORY = "working-directory"
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val BIB_RUN_CONFIG = "bib-run-config"
        private const val MAKEINDEX_RUN_CONFIG = "makeindex-run-config"
        private const val EXTERNAL_TOOL_RUN_CONFIG = "external-tool-run-config"
        private const val BIB_RUN_CONFIGS = "bib-run-configs"
        private const val MAKEINDEX_RUN_CONFIGS = "makeindex-run-configs"
        private const val EXTERNAL_TOOL_RUN_CONFIGS = "external-tool-run-configs"
        private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"
        private const val LATEXMK_COMPILE_MODE = "latexmk-compile-mode"
        private const val LATEXMK_CUSTOM_ENGINE_COMMAND = "latexmk-custom-engine-command"
        private const val LATEXMK_CITATION_TOOL = "latexmk-citation-tool"
        private const val LATEXMK_EXTRA_ARGUMENTS = "latexmk-extra-arguments"

        // For backwards compatibility
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
        private const val DEFAULT_LATEXMK_EXTRA_ARGUMENTS = "-synctex=1"
    }

    var compiler: LatexCompiler? = null
    override var compilerPath: String? = null
    override var pdfViewer: PdfViewer? = null
    var viewerCommand: String? = null

    override var compilerArguments: String? = null
        set(compilerArguments) {
            field = compilerArguments?.trim()
            if (field?.isEmpty() == true) {
                field = null
            }
        }

    var expandMacrosEnvVariables = false
    override var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    var beforeRunCommand: String? = null

    override var mainFile: VirtualFile? = null

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: SmartPsiElementPointer<PsiFile>? = null

    /** Path to the directory containing the output files. */
    var outputPath: Path? = LatexPathResolver.defaultOutputPath

    /** Path to the directory containing the auxiliary files. */
    var auxilPath: Path? = LatexPathResolver.defaultAuxilPath

    var workingDirectory: Path? = null

    var compileTwice = false
    var outputFormat: Format = Format.PDF
    var latexmkCompileMode: LatexmkCompileMode = LatexmkCompileMode.AUTO
    var latexmkCustomEngineCommand: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }
    var latexmkCitationTool: LatexmkCitationTool = LatexmkCitationTool.AUTO
    var latexmkExtraArguments: String? = DEFAULT_LATEXMK_EXTRA_ARGUMENTS
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    /**
     * The LaTeX distribution to use for this run configuration.
     *
     * SDKs are used as an optional way to provide explicit paths for a distribution type.
     * When no SDK is configured for a distribution type, the plugin falls back to finding
     * executables in PATH. This way, we can support also IDE's without explicit SDK configuration
     * (e.g. PyCharm).
     *
     * Note: This approach means you cannot select between multiple SDKs of the same type
     * (e.g., TeX Live 2023 vs TeX Live 2024) directly in the run configuration. To use a
     * specific SDK version, configure it as the project SDK or module SDK and select
     * PROJECT_SDK or MODULE_SDK.
     */
    var latexDistribution: LatexDistributionType = LatexDistributionType.MODULE_SDK

    @Transient
    var executionState: LatexRunExecutionState = LatexRunExecutionState()

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig: Boolean
        get() = executionState.isLastRunConfig
        set(value) {
            executionState.isLastRunConfig = value
        }

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun: Boolean
        get() = executionState.hasBeenRun
        set(value) {
            executionState.hasBeenRun = value
        }

    /** Whether the pdf viewer should claim focus after compilation. */
    var requireFocus = true

    /** Whether the run configuration is currently auto-compiling.     */
    override var isAutoCompiling = false

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
    val filesToCleanUpIfEmpty = mutableSetOf<File>()

    @Transient
    private val auxChainResolver = LatexAuxChainResolver(this)

    @Transient
    private val latexmkModeService = LatexmkModeService(this)

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexSettingsEditor(project)

    override fun createAdditionalTabComponents(
        manager: AdditionalTabComponentManager,
        startedProcess: ProcessHandler?
    ) {
        super.createAdditionalTabComponents(manager, startedProcess)

        if (manager is LogConsoleManagerBase && startedProcess != null) {
            manager.addAdditionalTabComponent(LatexLogTabComponent(project, mainFile, startedProcess), "LaTeX-Log", AllIcons.Vcs.Changelist, false)
        }
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError(
                "Run configuration is invalid: no compiler selected"
            )
        }
        if (mainFile == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        if (compiler == LatexCompiler.LATEXMK) {
            compilerArguments = buildLatexmkArguments()
        }

        val filter = RegexpFilter(
            environment.project,
            $$"^$FILE_PATH$:$LINE$"
        )

        val state = LatexCommandLineState(
            environment,
            this
        )
        state.addConsoleFilters(filter)
        return state
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        // NOTE: do not use runReadAction here as it may cause deadlock when other threads try to get run configurations from a write lock

        val parent = element.getChild(TEXIFY_PARENT) ?: return

        readCoreFields(parent)
        readPathAndCompileFields(parent)
        readLatexmkFields(parent)
        readLegacyPathFields(parent)
        readAuxConfigIds(parent)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        val parent = getOrCreateAndClearParent(element, TEXIFY_PARENT)
        writeCoreFields(parent)
        writePathAndCompileFields(parent)
        writeLatexmkFields(parent)
        writeAuxConfigIds(parent)
    }

    private fun readCoreFields(parent: Element) {
        val compilerName = parent.getChildText(COMPILER)
        compiler = runCatching { LatexCompiler.valueOf(compilerName) }.getOrNull()

        val compilerPathRead = parent.getChildText(COMPILER_PATH)
        compilerPath = if (compilerPathRead.isNullOrEmpty()) null else compilerPathRead
        val viewerName = parent.getChildText(PDF_VIEWER)
        pdfViewer = PdfViewer.availableViewers.firstOrNull { it.name == viewerName || it.name?.uppercase() == viewerName }
            ?: PdfViewer.firstAvailableViewer
        requireFocus = parent.getChildText(REQUIRE_FOCUS)?.toBoolean() ?: true

        val viewerCommandRead = parent.getChildText(VIEWER_COMMAND)
        viewerCommand = if (viewerCommandRead.isNullOrEmpty()) null else viewerCommandRead

        // Backwards compatibility (0.10.3 and earlier): path to SumatraPDF executable
        parent.getChildText(SUMATRA_PATH)?.let { folder ->
            runInBackgroundNonBlocking(project, "Set SumatraPDF path") {
                val settings = TexifySettings.getState()
                val isSumatraPathSet = readAction { settings.pathToSumatra != null }
                if (isSumatraPathSet) {
                    return@runInBackgroundNonBlocking
                }
                val path = try {
                    Path.of(folder).resolve("SumatraPDF.exe")
                }
                catch (_: InvalidPathException) {
                    return@runInBackgroundNonBlocking
                }
                if (SumatraViewer.trySumatraPath(path)) {
                    @Suppress("UnstableApiUsage")
                    writeAction {
                        TexifySettings.getState().pathToSumatra = path.absolutePathString()
                    }
                }
            }
        }
        val compilerArgumentsRead = parent.getChildText(COMPILER_ARGUMENTS)
        compilerArguments = if (compilerArgumentsRead.isNullOrEmpty()) null else compilerArgumentsRead
        environmentVariables = EnvironmentVariablesData.readExternal(parent)
        expandMacrosEnvVariables = parent.getChildText(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)?.toBoolean() ?: false

        val beforeRunCommandRead = parent.getChildText(BEFORE_RUN_COMMAND)
        beforeRunCommand = if (beforeRunCommandRead.isNullOrEmpty()) null else beforeRunCommandRead

        setMainFile(parent.getChildText(MAIN_FILE))
        latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))
        hasBeenRun = parent.getChildText(HAS_BEEN_RUN)?.toBoolean() ?: false
    }

    private fun readPathAndCompileFields(parent: Element) {
        parent.getChildText(OUTPUT_PATH)?.let { outputPathString ->
            outputPath = if (isInvalidJetBrainsBinPath(outputPathString)) LatexPathResolver.defaultOutputPath
            else pathOrNull(outputPathString)
        }

        parent.getChildText(AUXIL_PATH)?.let { auxilPathString ->
            auxilPath = pathOrNull(auxilPathString)
        }

        val workingDirectoryText = parent.getChildText(WORKING_DIRECTORY)
        workingDirectory = when {
            workingDirectoryText.isNullOrBlank() -> null
            workingDirectoryText == LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER -> null
            else -> pathOrNull(workingDirectoryText)
        }

        compileTwice = parent.getChildText(COMPILE_TWICE)?.toBoolean() ?: false
        if (compiler == LatexCompiler.LATEXMK) {
            compileTwice = false
        }
        outputFormat = Format.byNameIgnoreCase(parent.getChildText(OUTPUT_FORMAT))
    }

    private fun readLatexmkFields(parent: Element) {
        latexmkCompileMode = parent.getChildText(LATEXMK_COMPILE_MODE)
            ?.let { runCatching { LatexmkCompileMode.valueOf(it) }.getOrNull() }
            ?: LatexmkCompileMode.AUTO
        latexmkCustomEngineCommand = parent.getChildText(LATEXMK_CUSTOM_ENGINE_COMMAND)
        latexmkCitationTool = parent.getChildText(LATEXMK_CITATION_TOOL)
            ?.let { runCatching { LatexmkCitationTool.valueOf(it) }.getOrNull() }
            ?: LatexmkCitationTool.AUTO
        latexmkExtraArguments = parent.getChildText(LATEXMK_EXTRA_ARGUMENTS) ?: DEFAULT_LATEXMK_EXTRA_ARGUMENTS
    }

    private fun readLegacyPathFields(parent: Element) {
        val auxDirBoolean = parent.getChildText(AUX_DIR)
        if (auxDirBoolean != null && auxilPath == null && mainFile != null) {
            val usesAuxDir = java.lang.Boolean.parseBoolean(auxDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
            val path = if (usesAuxDir) moduleRoot?.path + "/auxil" else mainFile!!.parent.path
            auxilPath = pathOrNull(path)
        }

        val outDirBoolean = parent.getChildText(OUT_DIR)
        if (outDirBoolean != null && outputPath == null && mainFile != null) {
            val usesOutDir = java.lang.Boolean.parseBoolean(outDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
            val path = if (usesOutDir) moduleRoot?.path + "/out" else mainFile!!.parent.path
            outputPath = pathOrNull(path)
        }
    }

    private fun readAuxConfigIds(parent: Element) {
        bibRunConfigIds = LatexRunConfigurationSerializer.readRunConfigIds(parent, BIB_RUN_CONFIGS, BIB_RUN_CONFIG)
        makeindexRunConfigIds = LatexRunConfigurationSerializer.readRunConfigIds(parent, MAKEINDEX_RUN_CONFIGS, MAKEINDEX_RUN_CONFIG)
        externalToolRunConfigIds = LatexRunConfigurationSerializer.readRunConfigIds(parent, EXTERNAL_TOOL_RUN_CONFIGS, EXTERNAL_TOOL_RUN_CONFIG)
    }

    private fun writeCoreFields(parent: Element) {
        parent.addTextChild(COMPILER, compiler?.name ?: "")
        parent.addTextChild(COMPILER_ARGUMENTS, compilerArguments ?: "")
        parent.addTextChild(COMPILER_PATH, compilerPath ?: "")
        parent.addTextChild(PDF_VIEWER, pdfViewer?.name ?: "")
        parent.addTextChild(REQUIRE_FOCUS, requireFocus.toString())
        parent.addTextChild(VIEWER_COMMAND, viewerCommand ?: "")
        environmentVariables.writeExternal(parent)
        parent.addTextChild(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES, expandMacrosEnvVariables.toString())
        parent.addTextChild(BEFORE_RUN_COMMAND, beforeRunCommand ?: "")
        parent.addTextChild(MAIN_FILE, mainFile?.path ?: "")
        parent.addTextChild(WORKING_DIRECTORY, workingDirectory?.toString() ?: LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER)
        parent.addTextChild(LATEX_DISTRIBUTION, latexDistribution.name)
        parent.addTextChild(HAS_BEEN_RUN, hasBeenRun.toString())
    }

    private fun writePathAndCompileFields(parent: Element) {
        parent.addTextChild(OUTPUT_PATH, outputPath?.toString() ?: "")
        parent.addTextChild(AUXIL_PATH, auxilPath?.toString() ?: "")
        parent.addTextChild(COMPILE_TWICE, compileTwice.toString())
        parent.addTextChild(OUTPUT_FORMAT, outputFormat.name)
    }

    private fun writeLatexmkFields(parent: Element) {
        parent.addTextChild(LATEXMK_COMPILE_MODE, latexmkCompileMode.name)
        parent.addTextChild(LATEXMK_CUSTOM_ENGINE_COMMAND, latexmkCustomEngineCommand ?: "")
        parent.addTextChild(LATEXMK_CITATION_TOOL, latexmkCitationTool.name)
        parent.addTextChild(LATEXMK_EXTRA_ARGUMENTS, latexmkExtraArguments ?: "")
    }

    private fun writeAuxConfigIds(parent: Element) {
        LatexRunConfigurationSerializer.writeRunConfigIds(parent, BIB_RUN_CONFIGS, bibRunConfigIds)
        LatexRunConfigurationSerializer.writeRunConfigIds(parent, MAKEINDEX_RUN_CONFIGS, makeindexRunConfigIds)
        LatexRunConfigurationSerializer.writeRunConfigIds(parent, EXTERNAL_TOOL_RUN_CONFIGS, externalToolRunConfigIds)
    }

    /**
     * Create a new bib run config and add it to the set.
     */
    internal fun generateBibRunConfig() = auxChainResolver.generateBibRunConfig()

    /**
     * All run configs in the chain except the LaTeX ones.
     */
    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> = auxChainResolver.getAllAuxiliaryRunConfigs()

    override fun getResolvedWorkingDirectory(): Path? {
        val pathString = if (workingDirectory != null && mainFile != null) {
            workingDirectory?.toString()?.replace(LatexPathResolver.MAIN_FILE_PARENT_PLACEHOLDER, mainFile!!.parent.path)
        }
        else {
            mainFile?.parent?.path
        }
        return pathString?.let { pathOrNull(it) }
    }

    override fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == null

    /**
     * Looks up the corresponding [VirtualFile] and sets [LatexRunConfiguration.mainFile].
     *
     * See [readExternal]: NOTE: do not use runReadAction here as it may cause deadlock when other threads try to get run configurations from a write lock
     *
     */
    fun setMainFile(mainFilePath: String) {
        if (mainFilePath.isBlank()) {
            this.mainFile = null
            return
        }
        val fileSystem = LocalFileSystem.getInstance()
        // Check if the file is valid and exists
        val mainFile = fileSystem.findFileByPath(mainFilePath)

        if (mainFile?.extension == "tex") {
            this.mainFile = mainFile
            return
        }
        // Maybe it is a relative path
        val projectRootManager =
            ProjectRootManager.getInstance(project)
        val contentRoots = projectRootManager.contentRoots

        for (contentRoot in contentRoots) {
            // Check if the file exists in the content root
            val file = contentRoot.findFileByRelativePath(mainFilePath)
            if (file?.extension == "tex") {
                this.mainFile = file
                return
            }
        }
        this.mainFile = null
    }

    fun setDefaultCompiler() {
        compiler = LatexCompiler.PDFLATEX
    }

    fun setDefaultPdfViewer() {
        pdfViewer = PdfViewer.firstAvailableViewer
    }

    fun setDefaultOutputFormat() {
        outputFormat = Format.PDF
    }

    fun setDefaultLatexDistribution() {
        // Default to MODULE_SDK which provides the best experience for multi-module projects
        latexDistribution = LatexDistributionType.MODULE_SDK
    }

    /**
     * Resolve module and project SDK to a LaTeX SDK if possible, otherwise return null.
     */
    fun getLatexSdk(): Sdk? = when (latexDistribution) {
        LatexDistributionType.MODULE_SDK -> {
            val sdk = mainFile?.let { LatexSdkUtil.getLatexSdkForFile(it, project) }
                ?: LatexSdkUtil.getLatexProjectSdk(project)
            if (sdk?.sdkType is LatexSdk) sdk else null
        }

        LatexDistributionType.PROJECT_SDK -> {
            val sdk = LatexSdkUtil.getLatexProjectSdk(this.project)
            if (sdk?.sdkType is LatexSdk) sdk else null
        }

        else -> null
    }

    /**
     * Get the effective LaTeX distribution type for this run configuration.
     *
     * For MODULE_SDK and PROJECT_SDK: resolves to the actual distribution type of the SDK.
     * For concrete distribution types: returns the type directly.
     * Returns TEXLIVE as fallback when no SDK is configured.
     */
    fun getLatexDistributionType(): LatexDistributionType {
        val sdk = getLatexSdk()
        val type = (sdk?.sdkType as? LatexSdk?)?.getLatexDistributionType(sdk) ?: latexDistribution
        // It could be, user has selected module/project SDK but it's not valid, in that case use default
        return if (type == LatexDistributionType.MODULE_SDK || type == LatexDistributionType.PROJECT_SDK) {
            LatexDistributionType.TEXLIVE
        }
        else {
            type
        }
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder when used.
     */
    override fun getAuxilDirectory(): VirtualFile? = if (getLatexDistributionType().isMiktex(project, mainFile)) {
        // If we are using MiKTeX it might still be we are not using an auxil directory, so then fall back to the out directory
        LatexPathResolver.resolveAuxDir(this) ?: LatexPathResolver.resolveOutputDir(this)
    }
    else {
        LatexPathResolver.resolveOutputDir(this)
    }

    override fun getOutputDirectory(): VirtualFile? = LatexPathResolver.resolveOutputDir(this)

    fun setSuggestedName() {
        suggestedName()?.let { name = it }
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
        val outputDir = LatexPathResolver.resolveOutputDir(this) ?: mainFile?.parent
        val extension = if (compiler == LatexCompiler.LATEXMK) {
            effectiveLatexmkCompileMode().extension.lowercase(Locale.getDefault())
        }
        else if (outputFormat == Format.DEFAULT) {
            "pdf"
        }
        else {
            outputFormat.toString().lowercase(Locale.getDefault())
        }
        return "${outputDir?.path}/${mainFile!!.nameWithoutExtension}.$extension"
    }

    /**
     * Set [outputPath]
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        this.outputPath = pathOrNull(fileOutputPath)
    }

    /**
     * Set [auxilPath]
     */
    fun setFileAuxilPath(fileAuxilPath: String) {
        if (fileAuxilPath.isBlank()) return
        this.auxilPath = pathOrNull(fileAuxilPath)
    }

    /**
     * Whether an auxil or out directory is used, i.e. whether not both are set to the directory of the main file
     */
    fun usesAuxilOrOutDirectory(): Boolean {
        val usesAuxilDir = LatexPathResolver.resolveAuxDir(this) != mainFile?.parent
        val usesOutDir = LatexPathResolver.resolveOutputDir(this) != mainFile?.parent

        return usesAuxilDir || usesOutDir
    }

    override fun suggestedName(): String? = if (mainFile == null) {
        null
    }
    else {
        mainFile!!.nameWithoutExtension
    }

    override fun toString(): String = "LatexRunConfiguration{" + "compiler=" + compiler +
        ", compilerPath=" + compilerPath +
        ", mainFile=" + mainFile +
        ", outputFormat=" + outputFormat +
        '}'.toString()

    fun buildLatexmkArguments(): String = latexmkModeService.buildArguments()

    fun effectiveLatexmkCompileMode(): LatexmkCompileMode = latexmkModeService.effectiveCompileMode()

    override fun clone(): RunConfiguration = super.clone()
}
