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
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
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
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.includedPackagesInFileset
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber
import org.jdom.Element
import java.io.File
import java.nio.file.Path
import java.util.Locale

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexCommandLineState>(project, factory, name), LocatableConfiguration {

    companion object {

        internal const val DEFAULT_LATEXMK_EXTRA_ARGUMENTS = "-synctex=1"
    }

    var compiler: LatexCompiler? = null
    var compilerPath: String? = null
    var pdfViewer: PdfViewer? = null
    var viewerCommand: String? = null

    var compilerArguments: String? = null
        set(compilerArguments) {
            field = compilerArguments?.trim()
            if (field?.isEmpty() == true) {
                field = null
            }
        }

    var expandMacrosEnvVariables = false
    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    var beforeRunCommand: String? = null

    var mainFilePath: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
            executionState.clearInitialization()
        }

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

    @Transient
    internal var stepSchemaStatus: StepSchemaReadStatus = StepSchemaReadStatus.MISSING

    @Transient
    internal var stepSchemaTypes: List<String> = emptyList()

    /** Whether the pdf viewer should claim focus after compilation. */
    var requireFocus = true

    /** Whether the run configuration is currently auto-compiling.     */
    var isAutoCompiling = false

    @Volatile
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

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexSettingsEditor(project)

    override fun createAdditionalTabComponents(
        manager: AdditionalTabComponentManager,
        startedProcess: ProcessHandler?
    ) {
        super.createAdditionalTabComponents(manager, startedProcess)

        if (manager is LogConsoleManagerBase && startedProcess != null) {
            manager.addAdditionalTabComponent(
                LatexLogTabComponent(project, executionState.resolvedMainFile, startedProcess),
                "LaTeX-Log",
                AllIcons.Vcs.Changelist,
                false
            )
        }
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError(
                "Run configuration is invalid: no compiler selected"
            )
        }
        if (mainFilePath.isNullOrBlank()) {
            throw RuntimeConfigurationError("Run configuration is invalid: no main LaTeX file path selected")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        // Manual run boundary: reruns in aux chain set isFirstRunConfig=false, so do not reset there.
        if (executionState.isFirstRunConfig) {
            executionState.prepareForManualRun()
        }

        val desiredPipeline = LatexRunStepsMigrationPolicy.chooseExecutionPipeline(stepSchemaStatus)
        if (desiredPipeline == ExecutionPipelineMode.STEPS) {
            val plan = nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder.build(stepSchemaTypes)
            if (plan.steps.isNotEmpty() && isSupportedByCurrentStepEngine()) {
                if (plan.unsupportedTypes.isNotEmpty()) {
                    Log.warn("Unsupported compile-step types in schema: ${plan.unsupportedTypes.joinToString(", ")}")
                }
                return nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState(this, environment, plan)
            }
            if (plan.steps.isEmpty()) {
                Log.warn("Compile-step schema was present but no supported steps were found. Falling back to legacy pipeline.")
            }
            else {
                Log.info("Compile-step schema is present, but current configuration is not yet supported by the step engine. Falling back to legacy pipeline.")
            }
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

    private fun isSupportedByCurrentStepEngine(): Boolean {
        // Phase 2 safeguard: keep legacy behavior for configurations that still depend on auxiliary run-config chains.
        if (compiler != LatexCompiler.LATEXMK) return false
        if (compileTwice) return false
        if (getAllAuxiliaryRunConfigs().isNotEmpty()) return false
        return true
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)
        LatexRunConfigurationPersistence.readInto(this, element)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)
        LatexRunConfigurationPersistence.writeFrom(this, element)
    }

    /**
     * Create a new bib run config and add it to the set.
     */
    internal fun generateBibRunConfig() {
        val mainFile = executionState.resolvedMainFile ?: LatexRunConfigurationStaticSupport.resolveMainFile(this)
        val psiFile = mainFile?.let { PsiManager.getInstance(project).findFile(it) }
            ?: executionState.psiFile?.element
            ?: return
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
            addBibRunConfig(defaultCompiler, mainFile, compilerFromMagicComment?.second)
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

    /**
     * All run configs in the chain except the LaTeX ones.
     */
    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> = bibRunConfigs + makeindexRunConfigs + externalToolRunConfigs

    private fun addBibRunConfig(
        defaultCompiler: BibliographyCompiler,
        mainFile: VirtualFile?,
        compilerArguments: String? = null
    ) {
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val bibSettings = runManager.createConfiguration("", LatexConfigurationFactory(BibtexRunConfigurationType()))
        val bibtexRunConfiguration = bibSettings.configuration as BibtexRunConfiguration

        bibtexRunConfiguration.compiler = defaultCompiler
        if (compilerArguments != null) bibtexRunConfiguration.compilerArguments = compilerArguments
        bibtexRunConfiguration.mainFile = mainFile
        bibtexRunConfiguration.setSuggestedName()
        bibtexRunConfiguration.setDefaultDistribution(getLatexDistributionType())

        if (!getLatexDistributionType().isMiktex(project, mainFile)) {
            if (mainFile != null && executionState.resolvedOutputDir != mainFile.parent) {
                bibtexRunConfiguration.environmentVariables = bibtexRunConfiguration.environmentVariables.with(
                    mapOf(
                        "BIBINPUTS" to mainFile.parent.path,
                        "BSTINPUTS" to mainFile.parent.path + File.pathSeparator
                    )
                )
            }
        }

        runManager.addConfiguration(bibSettings)
        bibRunConfigs += setOf(bibSettings)
    }

    fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == null

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
            val mainFile = executionState.resolvedMainFile
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

    fun setSuggestedName() {
        suggestedName()?.let { name = it }
    }

    override fun isGeneratedName(): Boolean {
        val fileNameWithoutExtension = LatexRunConfigurationStaticSupport.mainFileNameWithoutExtension(this) ?: return false
        return fileNameWithoutExtension == name
    }

    fun setAuxRunConfigIds(ids: Set<String>) {
        bibRunConfigIds = ids.toMutableSet()
    }

    fun setMakeindexRunConfigIds(ids: Set<String>) {
        makeindexRunConfigIds = ids.toMutableSet()
    }

    fun setExternalToolRunConfigIds(ids: Set<String>) {
        externalToolRunConfigIds = ids.toMutableSet()
    }

    fun getBibRunConfigIds(): Set<String> = bibRunConfigIds
    fun getMakeindexRunConfigIds(): Set<String> = makeindexRunConfigIds
    fun getExternalToolRunConfigIds(): Set<String> = externalToolRunConfigIds

    override fun suggestedName(): String? = LatexRunConfigurationStaticSupport.mainFileNameWithoutExtension(this)

    override fun toString(): String = "LatexRunConfiguration{" + "compiler=" + compiler +
        ", compilerPath=" + compilerPath +
        ", mainFilePath=" + mainFilePath +
        ", outputFormat=" + outputFormat +
        '}'.toString()

    override fun clone(): RunConfiguration {
        val cloned = super.clone() as LatexRunConfiguration
        cloned.executionState = LatexRunExecutionState()
        cloned.stepSchemaStatus = StepSchemaReadStatus.MISSING
        cloned.stepSchemaTypes = emptyList()
        return cloned
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
}
