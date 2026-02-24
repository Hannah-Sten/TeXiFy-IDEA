package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.Log
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

    var compiler: LatexCompiler?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepOptions -> step.compiler
            is LatexmkCompileStepOptions -> LatexCompiler.LATEXMK
            else -> null
        }
        set(value) {
            if (value == null) {
                return
            }
            if (value == LatexCompiler.LATEXMK) {
                ensurePrimaryCompileStepLatexmk()
            }
            else {
                ensurePrimaryCompileStepClassic().compiler = value
            }
        }

    var compilerPath: String?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepOptions -> step.compilerPath
            is LatexmkCompileStepOptions -> step.compilerPath
            else -> null
        }
        set(value) {
            val normalized = value?.trim()?.ifEmpty { null }
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepOptions -> step.compilerPath = normalized
                is LatexmkCompileStepOptions -> step.compilerPath = normalized
                else -> ensurePrimaryCompileStepClassic().compilerPath = normalized
            }
        }

    var compilerArguments: String?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepOptions -> step.compilerArguments
            is LatexmkCompileStepOptions -> step.compilerArguments
            else -> null
        }
        set(value) {
            val normalized = value?.trim()?.ifEmpty { null }
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepOptions -> step.compilerArguments = normalized
                is LatexmkCompileStepOptions -> step.compilerArguments = normalized
                else -> ensurePrimaryCompileStepClassic().compilerArguments = normalized
            }
        }

    var outputFormat: Format
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepOptions -> step.outputFormat
            else -> Format.PDF
        }
        set(value) {
            ensurePrimaryCompileStepClassic().outputFormat = value
        }

    var beforeRunCommand: String?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepOptions -> step.beforeRunCommand
            is LatexmkCompileStepOptions -> step.beforeRunCommand
            else -> null
        }
        set(value) {
            val normalized = value?.trim()?.ifEmpty { null }
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepOptions -> step.beforeRunCommand = normalized
                is LatexmkCompileStepOptions -> step.beforeRunCommand = normalized
                else -> ensurePrimaryCompileStepClassic().beforeRunCommand = normalized
            }
        }

    var latexmkCompileMode: LatexmkCompileMode
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCompileMode ?: LatexmkCompileMode.AUTO
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCompileMode = value
        }

    var latexmkCustomEngineCommand: String?
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCustomEngineCommand
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCustomEngineCommand = value?.trim()?.ifEmpty { null }
        }

    var latexmkCitationTool: LatexmkCitationTool
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkCitationTool ?: LatexmkCitationTool.AUTO
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCitationTool = value
        }

    var latexmkExtraArguments: String?
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepOptions)?.latexmkExtraArguments
            ?: DEFAULT_LATEXMK_EXTRA_ARGUMENTS
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkExtraArguments = value?.trim()?.ifEmpty { null }
        }

    var compileTwice: Boolean
        get() = steps.count { it.enabled && (it.type == LatexStepType.LATEX_COMPILE || it.type == LatexStepType.LATEXMK_COMPILE) } > 1
        set(value) {
            val compileIndices = steps.withIndex()
                .filter { (_, step) -> step.enabled && (step.type == LatexStepType.LATEX_COMPILE || step.type == LatexStepType.LATEXMK_COMPILE) }
                .map { it.index }
            if (compileIndices.isEmpty()) {
                steps.add(0, LatexCompileStepOptions())
                if (value) {
                    steps.add(1, LatexCompileStepOptions())
                }
                return
            }
            if (value && compileIndices.size < 2) {
                steps.add(compileIndices.first() + 1, steps[compileIndices.first()].deepCopy())
            }
            if (!value && compileIndices.size > 1) {
                compileIndices.drop(1).asReversed().forEach { index -> steps.removeAt(index) }
            }
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

    var viewerCommand: String?
        get() = activeOrPrimaryViewerStep()?.customViewerCommand
        set(value) {
            ensurePrimaryViewerStep().customViewerCommand = value?.trim()?.ifEmpty { null }
        }

    var requireFocus: Boolean
        get() = activeOrPrimaryViewerStep()?.requireFocus ?: true
        set(value) {
            ensurePrimaryViewerStep().requireFocus = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexSettingsEditor(this)

    override fun createAdditionalTabComponents(
        manager: AdditionalTabComponentManager,
        startedProcess: ProcessHandler?
    ) {
        super.createAdditionalTabComponents(manager, startedProcess)
    }

    @Suppress("unused")
    private fun createLegacyLogTabComponent(startedProcess: ProcessHandler): LatexLogTabComponent =
        LatexLogTabComponent(project, executionState.resolvedMainFile, startedProcess)

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
        val configuredPlan = LatexRunStepPlanBuilder.build(configuredSteps)
        if (configuredPlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types in schema: ${configuredPlan.unsupportedTypes.joinToString(", ")}")
        }

        if (configuredPlan.steps.isNotEmpty()) {
            return LatexStepRunState(this, environment, configuredPlan, configuredSteps)
        }

        throw ExecutionException("No executable compile steps were configured.")
    }

    fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == null

    fun setDefaultCompiler() {
        ensurePrimaryCompileStepClassic().compiler = LatexCompiler.PDFLATEX
    }

    fun setDefaultPdfViewer() {
        ensurePrimaryViewerStep().pdfViewerName = PdfViewer.firstAvailableViewer?.name
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
        cloned.configOptions.mainFilePath = configOptions.mainFilePath
        cloned.configOptions.workingDirectoryPath = configOptions.workingDirectoryPath
        cloned.configOptions.outputPath = configOptions.outputPath
        cloned.configOptions.auxilPath = configOptions.auxilPath
        cloned.configOptions.latexDistribution = configOptions.latexDistribution
        cloned.configOptions.expandMacrosEnvVariables = configOptions.expandMacrosEnvVariables
        cloned.configOptions.passParentEnvironmentVariables = configOptions.passParentEnvironmentVariables
        cloned.configOptions.environmentVariables = configOptions.environmentVariables.map { it.deepCopy() }.toMutableList()
        cloned.configOptions.steps = configOptions.steps.map { it.deepCopy() }.toMutableList()
        return cloned
    }

    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        this.outputPath = pathOrNull(fileOutputPath)
    }

    fun setFileAuxilPath(fileAuxilPath: String) {
        if (fileAuxilPath.isBlank()) return
        this.auxilPath = pathOrNull(fileAuxilPath)
    }

    internal fun activateStepForExecution(stepId: String?) {
        activeStepIdForExecution = stepId
    }

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

    private fun ensurePrimaryCompileStepClassic(): LatexCompileStepOptions {
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

    private fun ensurePrimaryCompileStepLatexmk(): LatexmkCompileStepOptions {
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
