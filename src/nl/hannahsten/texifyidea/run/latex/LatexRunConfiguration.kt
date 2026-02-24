package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.nio.file.Path

class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexRunConfigurationOptions>(project, factory, name), LocatableConfiguration {

    companion object {

        internal const val DEFAULT_LATEXMK_EXTRA_ARGUMENTS = "-synctex=1"
    }

    @Transient
    var executionState: LatexRunExecutionState = LatexRunExecutionState()

    var isAutoCompiling = false

    private var activeStepIdForExecution: String? = null

    override fun getOptions(): LatexRunConfigurationOptions =
        super.getOptions() as LatexRunConfigurationOptions

    internal val configOptions: LatexRunConfigurationOptions
        get() = getOptions()

    private var steps: MutableList<LatexStepRunConfigurationOptions>
        get() = configOptions.steps
        set(value) {
            configOptions.steps = value
            executionState.clearInitialization()
        }

    var mainFilePath: String?
        get() = configOptions.mainFilePath
        set(value) {
            configOptions.mainFilePath = value?.trim()?.ifEmpty { null }
            executionState.clearInitialization()
        }

    var outputPath: Path?
        get() = configOptions.outputPath?.let(::pathOrNull)
        set(value) {
            configOptions.outputPath = value?.toString() ?: LatexPathResolver.defaultOutputPath.toString()
            executionState.clearInitialization()
        }

    var auxilPath: Path?
        get() = configOptions.auxilPath?.let(::pathOrNull)
        set(value) {
            configOptions.auxilPath = value?.toString() ?: LatexPathResolver.defaultAuxilPath.toString()
            executionState.clearInitialization()
        }

    var workingDirectory: Path?
        get() = configOptions.workingDirectoryPath?.let(::pathOrNull)
        set(value) {
            configOptions.workingDirectoryPath = value?.toString()
            executionState.clearInitialization()
        }

    var environmentVariables: EnvironmentVariablesData
        get() = EnvironmentVariablesData.create(
            configOptions.environmentVariables
                .mapNotNull { entry ->
                    val key = entry.name?.trim()?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                    key to (entry.value ?: "")
                }
                .toMap(),
            configOptions.passParentEnvironmentVariables,
        )
        set(value) {
            configOptions.passParentEnvironmentVariables = value.isPassParentEnvs
            configOptions.environmentVariables = value.envs.map { (name, envValue) ->
                LatexRunConfigurationOptions.EnvironmentVariableEntry().apply {
                    this.name = name
                    this.value = envValue
                }
            }.toMutableList()
        }

    var expandMacrosEnvVariables: Boolean
        get() = configOptions.expandMacrosEnvVariables
        set(value) {
            configOptions.expandMacrosEnvVariables = value
        }

    var latexDistribution: LatexDistributionType
        get() = configOptions.latexDistribution
        set(value) {
            configOptions.latexDistribution = value
            executionState.clearInitialization()
        }

    var pdfViewer: PdfViewer?
        get() {
            val viewerName = activeOrPrimaryViewerStep()?.pdfViewerName
            return PdfViewer.availableViewers.firstOrNull { it.name == viewerName }
                ?: PdfViewer.firstAvailableViewer
        }
        set(value) {
            val step = ensurePrimaryViewerStep()
            step.pdfViewerName = value?.name
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexSettingsEditor(this)

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (mainFilePath.isNullOrBlank()) {
            throw RuntimeConfigurationError("Run configuration is invalid: no main LaTeX file path selected")
        }
        if (steps.none { it.enabled }) {
            throw RuntimeConfigurationError("Run configuration is invalid: no enabled compile steps")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        if (executionState.isFirstRunConfig) {
            executionState.prepareForManualRun()
        }

        configOptions.ensureDefaultSteps()

        val configuredSteps = steps.filter { it.enabled }
        if (configuredSteps.isEmpty()) {
            throw ExecutionException("No executable compile steps were configured.")
        }

        val hasExecutableStep = configuredSteps.any { step ->
            LatexRunStepProviders.find(step.type) != null
        }
        if (!hasExecutableStep) {
            throw ExecutionException("No executable compile steps were configured.")
        }

        return LatexStepRunState(this, environment, configuredSteps)
    }

    fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == null

    fun setDefaultCompiler() {
        ensurePrimaryCompileStepClassic().compiler = LatexCompiler.PDFLATEX
    }

    fun setDefaultPdfViewer() {
        ensurePrimaryViewerStep().pdfViewerName = PdfViewer.firstAvailableViewer.name
    }

    fun setDefaultOutputFormat() {
        ensurePrimaryCompileStepClassic().outputFormat = Format.PDF
    }

    fun setDefaultLatexDistribution() {
        latexDistribution = LatexDistributionType.MODULE_SDK
    }

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

    fun getLatexDistributionType(): LatexDistributionType {
        val sdk = getLatexSdk()
        val type = (sdk?.sdkType as? LatexSdk?)?.getLatexDistributionType(sdk) ?: latexDistribution
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

    override fun suggestedName(): String? = LatexRunConfigurationStaticSupport.mainFileNameWithoutExtension(this)

    override fun toString(): String = "LatexRunConfiguration{" +
        "mainFilePath=$mainFilePath" +
        ", steps=${steps.map { it.type }}" +
        '}'.toString()

    override fun clone(): RunConfiguration {
        val cloned = super.clone() as LatexRunConfiguration
        cloned.executionState = LatexRunExecutionState()
        cloned.isAutoCompiling = false
        cloned.activeStepIdForExecution = null
        cloned.configOptions.environmentVariables.clear()
        cloned.configOptions.environmentVariables.addAll(configOptions.environmentVariables.map { it.deepCopy() })
        cloned.configOptions.steps.clear()
        cloned.configOptions.steps.addAll(configOptions.steps.map { it.deepCopy() })
        return cloned
    }

    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        this.outputPath = pathOrNull(fileOutputPath)
    }

    internal fun activateStepForExecution(stepId: String?) {
        activeStepIdForExecution = stepId
    }

    internal fun activeCompileStep(): LatexStepRunConfigurationOptions? = activeOrPrimaryCompileStep()

    internal fun primaryCompileStep(): LatexStepRunConfigurationOptions? =
        steps.firstOrNull { it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions) }

    internal fun hasEnabledLatexmkStep(): Boolean =
        steps.any { it.enabled && it is LatexmkCompileStepOptions }

    internal fun activeCompiler(): LatexCompiler? = when (val step = activeOrPrimaryCompileStep()) {
        is LatexCompileStepOptions -> step.compiler
        is LatexmkCompileStepOptions -> LatexCompiler.LATEXMK
        else -> null
    }

    internal fun activeCompilerPath(): String? = when (val step = activeOrPrimaryCompileStep()) {
        is LatexCompileStepOptions -> step.compilerPath
        is LatexmkCompileStepOptions -> step.compilerPath
        else -> null
    }

    internal fun activeCompilerArguments(): String? = when (val step = activeOrPrimaryCompileStep()) {
        is LatexCompileStepOptions -> step.compilerArguments
        is LatexmkCompileStepOptions -> step.compilerArguments
        else -> null
    }

    internal fun activeOutputFormat(): Format = when (val step = activeOrPrimaryCompileStep()) {
        is LatexCompileStepOptions -> step.outputFormat
        else -> Format.PDF
    }

    internal fun activeBeforeRunCommand(): String? = when (val step = activeOrPrimaryCompileStep()) {
        is LatexCompileStepOptions -> step.beforeRunCommand
        is LatexmkCompileStepOptions -> step.beforeRunCommand
        else -> null
    }

    internal fun activeLatexmkCompileMode(): LatexmkCompileMode =
        (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCompileMode ?: LatexmkCompileMode.AUTO

    internal fun activeLatexmkCustomEngineCommand(): String? =
        (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCustomEngineCommand

    internal fun activeLatexmkCitationTool(): LatexmkCitationTool =
        (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCitationTool ?: LatexmkCitationTool.AUTO

    internal fun activeLatexmkExtraArguments(): String =
        (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkExtraArguments
            ?: DEFAULT_LATEXMK_EXTRA_ARGUMENTS

    private fun activeOrPrimaryCompileStep(): LatexStepRunConfigurationOptions? {
        val active = activeStepIdForExecution
            ?.let { id -> steps.firstOrNull { it.id == id && it.enabled } }
        if (active is LatexCompileStepOptions || active is LatexmkCompileStepOptions) {
            return active
        }

        return steps.firstOrNull {
            it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions)
        }
    }

    private fun activeOrPrimaryViewerStep(): PdfViewerStepOptions? {
        val active = activeStepIdForExecution
            ?.let { id -> steps.firstOrNull { it.id == id && it.enabled } }
        if (active is PdfViewerStepOptions) {
            return active
        }

        return steps.firstOrNull { it.enabled && it is PdfViewerStepOptions } as? PdfViewerStepOptions
    }

    internal fun ensurePrimaryCompileStepClassic(): LatexCompileStepOptions {
        val index = steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions)
        }
        return when {
            index < 0 -> LatexCompileStepOptions().also { steps.add(0, it) }
            steps[index] is LatexCompileStepOptions -> steps[index] as LatexCompileStepOptions
            else -> {
                val old = steps[index] as LatexmkCompileStepOptions
                LatexCompileStepOptions().also {
                    it.id = old.id
                    it.enabled = old.enabled
                    it.compiler = LatexCompiler.PDFLATEX
                    it.compilerPath = old.compilerPath
                    it.compilerArguments = old.compilerArguments
                    it.outputFormat = Format.PDF
                    it.beforeRunCommand = old.beforeRunCommand
                    it.selectedOptions = old.selectedOptions
                    steps[index] = it
                }
            }
        }
    }

    internal fun ensurePrimaryCompileStepLatexmk(): LatexmkCompileStepOptions {
        val index = steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepOptions || it is LatexmkCompileStepOptions)
        }
        return when {
            index < 0 -> LatexmkCompileStepOptions().also { steps.add(0, it) }
            steps[index] is LatexmkCompileStepOptions -> steps[index] as LatexmkCompileStepOptions
            else -> {
                val old = steps[index] as LatexCompileStepOptions
                LatexmkCompileStepOptions().also {
                    it.id = old.id
                    it.enabled = old.enabled
                    it.compilerPath = old.compilerPath
                    it.compilerArguments = old.compilerArguments
                    it.latexmkCompileMode = LatexmkCompileMode.AUTO
                    it.latexmkCustomEngineCommand = null
                    it.latexmkCitationTool = LatexmkCitationTool.AUTO
                    it.latexmkExtraArguments = DEFAULT_LATEXMK_EXTRA_ARGUMENTS
                    it.beforeRunCommand = old.beforeRunCommand
                    it.selectedOptions = old.selectedOptions
                    steps[index] = it
                }
            }
        }
    }

    private fun ensurePrimaryViewerStep(): PdfViewerStepOptions {
        val index = steps.indexOfFirst { it.enabled && it is PdfViewerStepOptions }
        return if (index >= 0) {
            steps[index] as PdfViewerStepOptions
        }
        else {
            PdfViewerStepOptions().also { steps.add(it) }
        }
    }
}
