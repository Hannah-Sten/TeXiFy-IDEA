package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
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

/**
 * LaTeX run configuration backed by V2 model: common + step list + UI state.
 */
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

    /** Whether the run configuration is currently auto-compiling. */
    var isAutoCompiling = false

    /**
     * Legacy marker kept transient while tests are migrated to V2 model assertions.
     */
    @Transient
    internal var stepSchemaStatus: StepSchemaReadStatus = StepSchemaReadStatus.PARSED

    private var activeStepIdForExecution: String? = null

    override fun getOptions(): LatexRunConfigurationOptions =
        super.getOptions() as LatexRunConfigurationOptions

    internal var model: LatexRunConfigModel
        get() = options.model
        set(value) {
            options.model = value
            executionState.clearInitialization()
        }

    var mainFilePath: String?
        get() = model.common.mainFilePath
        set(value) {
            model.common.mainFilePath = value?.trim()?.ifEmpty { null }
            executionState.clearInitialization()
        }

    /** Path to the directory containing the output files. */
    var outputPath: Path?
        get() = model.common.outputPath
        set(value) {
            model.common.outputPath = value
            executionState.clearInitialization()
        }

    /** Path to the directory containing the auxiliary files. */
    var auxilPath: Path?
        get() = model.common.auxilPath
        set(value) {
            model.common.auxilPath = value
            executionState.clearInitialization()
        }

    var workingDirectory: Path?
        get() = model.common.workingDirectory
        set(value) {
            model.common.workingDirectory = value
            executionState.clearInitialization()
        }

    var environmentVariables: EnvironmentVariablesData
        get() = model.common.environmentVariables
        set(value) {
            model.common.environmentVariables = value
        }

    var expandMacrosEnvVariables: Boolean
        get() = model.common.expandMacrosEnvVariables
        set(value) {
            model.common.expandMacrosEnvVariables = value
        }

    var latexDistribution: LatexDistributionType
        get() = model.common.latexDistribution
        set(value) {
            model.common.latexDistribution = value
            executionState.clearInitialization()
        }

    var compiler: LatexCompiler?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepConfig -> step.compiler
            is LatexmkCompileStepConfig -> LatexCompiler.LATEXMK
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
            is LatexCompileStepConfig -> step.compilerPath
            is LatexmkCompileStepConfig -> step.compilerPath
            else -> null
        }
        set(value) {
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepConfig -> step.compilerPath = value?.trim()?.ifEmpty { null }
                is LatexmkCompileStepConfig -> step.compilerPath = value?.trim()?.ifEmpty { null }
                else -> ensurePrimaryCompileStepClassic().compilerPath = value?.trim()?.ifEmpty { null }
            }
        }

    var compilerArguments: String?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepConfig -> step.compilerArguments
            is LatexmkCompileStepConfig -> step.compilerArguments
            else -> null
        }
        set(value) {
            val normalized = value?.trim()?.ifEmpty { null }
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepConfig -> step.compilerArguments = normalized
                is LatexmkCompileStepConfig -> step.compilerArguments = normalized
                else -> ensurePrimaryCompileStepClassic().compilerArguments = normalized
            }
        }

    var outputFormat: Format
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepConfig -> step.outputFormat
            else -> Format.PDF
        }
        set(value) {
            ensurePrimaryCompileStepClassic().outputFormat = value
        }

    var beforeRunCommand: String?
        get() = when (val step = activeOrPrimaryCompileStep()) {
            is LatexCompileStepConfig -> step.beforeRunCommand
            is LatexmkCompileStepConfig -> step.beforeRunCommand
            else -> null
        }
        set(value) {
            val normalized = value?.trim()?.ifEmpty { null }
            when (val step = activeOrPrimaryCompileStep()) {
                is LatexCompileStepConfig -> step.beforeRunCommand = normalized
                is LatexmkCompileStepConfig -> step.beforeRunCommand = normalized
                else -> ensurePrimaryCompileStepClassic().beforeRunCommand = normalized
            }
        }

    var latexmkCompileMode: LatexmkCompileMode
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepConfig)?.latexmkCompileMode ?: LatexmkCompileMode.AUTO
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCompileMode = value
        }

    var latexmkCustomEngineCommand: String?
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepConfig)?.latexmkCustomEngineCommand
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCustomEngineCommand = value?.trim()?.ifEmpty { null }
        }

    var latexmkCitationTool: LatexmkCitationTool
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepConfig)?.latexmkCitationTool ?: LatexmkCitationTool.AUTO
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkCitationTool = value
        }

    var latexmkExtraArguments: String?
        get() = (activeOrPrimaryCompileStep() as? LatexmkCompileStepConfig)?.latexmkExtraArguments
            ?: DEFAULT_LATEXMK_EXTRA_ARGUMENTS
        set(value) {
            ensurePrimaryCompileStepLatexmk().latexmkExtraArguments = value?.trim()?.ifEmpty { null }
        }

    @Deprecated("Use explicit repeated compile steps in model.steps")
    var compileTwice: Boolean
        get() = model.steps.count { it.enabled && (it.type == LatexStepType.LATEX_COMPILE || it.type == LatexStepType.LATEXMK_COMPILE) } > 1
        set(value) {
            val compileIndices = model.steps.withIndex()
                .filter { (_, step) -> step.enabled && (step.type == LatexStepType.LATEX_COMPILE || step.type == LatexStepType.LATEXMK_COMPILE) }
                .map { it.index }
            if (compileIndices.isEmpty()) {
                model.steps.add(0, LatexCompileStepConfig())
                if (value) {
                    model.steps.add(1, LatexCompileStepConfig())
                }
                return
            }
            if (value && compileIndices.size < 2) {
                model.steps.add(compileIndices.first() + 1, model.steps[compileIndices.first()].deepCopy())
            }
            if (!value && compileIndices.size > 1) {
                compileIndices.drop(1).asReversed().forEach { index -> model.steps.removeAt(index) }
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

    /** Whether the pdf viewer should claim focus after compilation. */
    var requireFocus: Boolean
        get() = activeOrPrimaryViewerStep()?.requireFocus ?: true
        set(value) {
            ensurePrimaryViewerStep().requireFocus = value
        }

    /**
     * Legacy property kept as adapter while V2 model-based call sites are migrated.
     */
    @Deprecated("Use model.steps", ReplaceWith("model.steps.map { it.type }"))
    internal var stepSchemaTypes: List<String>
        get() = model.steps.map { it.type }
        set(value) {
            model.steps = value.mapNotNull(::defaultStepFor).toMutableList()
            if (model.steps.isEmpty()) {
                model.steps += LatexCompileStepConfig()
                model.steps += PdfViewerStepConfig()
            }
        }

    /**
     * Legacy adapter: by-type map now materialized over by-step-id map.
     */
    @Deprecated("Use model.ui.stepUiOptionIdsByStepId")
    internal var stepUiOptionIdsByType: MutableMap<String, MutableSet<String>>
        get() = model.ui.stepUiOptionIdsByStepId.entries
            .mapNotNull { (stepId, ids) ->
                val stepType = model.steps.firstOrNull { it.id == stepId }?.type ?: return@mapNotNull null
                stepType to ids
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, grouped) -> grouped.flatten().toMutableSet() }
            .toMutableMap()
        set(value) {
            val byStep = mutableMapOf<String, MutableSet<String>>()
            model.steps.forEach { step ->
                val ids = value[step.type]?.toMutableSet() ?: mutableSetOf()
                if (ids.isNotEmpty()) {
                    byStep[step.id] = ids
                }
            }
            model.ui.stepUiOptionIdsByStepId = byStep
        }

    var stepUiOptionIdsByStepId: MutableMap<String, MutableSet<String>>
        get() = model.ui.stepUiOptionIdsByStepId
        set(value) {
            model.ui.stepUiOptionIdsByStepId = value
        }

    @Deprecated("Auxiliary run-config bridge removed; use strong-typed steps in model.steps")
    var bibRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = emptySet()
        set(_) {}

    @Deprecated("Auxiliary run-config bridge removed; use strong-typed steps in model.steps")
    var makeindexRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = emptySet()
        set(_) {}

    @Deprecated("Auxiliary run-config bridge removed; use strong-typed steps in model.steps")
    var externalToolRunConfigs: Set<RunnerAndConfigurationSettings>
        get() = emptySet()
        set(_) {}

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
        if (model.steps.none { it.enabled }) {
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

        if (model.steps.none { it.enabled }) {
            model.steps = mutableListOf(LatexCompileStepConfig(), PdfViewerStepConfig())
        }

        val configuredSteps = model.steps.filter { it.enabled }
        val configuredPlan = LatexRunStepPlanBuilder.build(configuredSteps)
        if (configuredPlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types in schema: ${configuredPlan.unsupportedTypes.joinToString(", ")}")
        }

        if (configuredPlan.steps.isNotEmpty()) {
            return LatexStepRunState(this, environment, configuredPlan, configuredSteps)
        }

        throw ExecutionException("No executable compile steps were configured.")
    }

    @Deprecated("Auxiliary run-config bridge removed; use strong-typed steps in model.steps")
    internal fun generateBibRunConfig() {
        if (model.steps.any { it.type == LatexStepType.BIBTEX }) {
            return
        }
        val insertAfter = model.steps.indexOfFirst { it.type == LatexStepType.LATEX_COMPILE || it.type == LatexStepType.LATEXMK_COMPILE }
        val insertIndex = if (insertAfter >= 0) insertAfter + 1 else model.steps.size
        model.steps.add(insertIndex, BibtexStepConfig())
    }

    @Deprecated("Auxiliary run-config bridge removed; use strong-typed steps in model.steps")
    fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> = emptySet()

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
     */
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

    @Deprecated("Removed in V2")
    fun setAuxRunConfigIds(ids: Set<String>) {
    }

    @Deprecated("Removed in V2")
    fun setMakeindexRunConfigIds(ids: Set<String>) {
    }

    @Deprecated("Removed in V2")
    fun setExternalToolRunConfigIds(ids: Set<String>) {
    }

    @Deprecated("Removed in V2")
    fun getBibRunConfigIds(): Set<String> = emptySet()

    @Deprecated("Removed in V2")
    fun getMakeindexRunConfigIds(): Set<String> = emptySet()

    @Deprecated("Removed in V2")
    fun getExternalToolRunConfigIds(): Set<String> = emptySet()

    override fun toString(): String = "LatexRunConfiguration{" +
        "mainFilePath=$mainFilePath" +
        ", steps=${model.steps.map { it.type }}" +
        '}'.toString()

    override fun clone(): RunConfiguration {
        val cloned = super.clone() as LatexRunConfiguration
        cloned.executionState = LatexRunExecutionState()
        cloned.stepSchemaStatus = StepSchemaReadStatus.PARSED
        cloned.model = model.deepCopy()
        cloned.isAutoCompiling = false
        cloned.activeStepIdForExecution = null
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

    private fun activeOrPrimaryCompileStep(): LatexStepConfig? {
        val active = activeStepIdForExecution
            ?.let { id -> model.steps.firstOrNull { it.id == id && it.enabled } }
        if (active is LatexCompileStepConfig || active is LatexmkCompileStepConfig) {
            return active
        }

        return model.steps.firstOrNull {
            it.enabled && (it is LatexCompileStepConfig || it is LatexmkCompileStepConfig)
        }
    }

    private fun activeOrPrimaryViewerStep(): PdfViewerStepConfig? {
        val active = activeStepIdForExecution
            ?.let { id -> model.steps.firstOrNull { it.id == id && it.enabled } }
        if (active is PdfViewerStepConfig) {
            return active
        }

        return model.steps.firstOrNull { it.enabled && it is PdfViewerStepConfig } as? PdfViewerStepConfig
    }

    private fun ensurePrimaryCompileStepClassic(): LatexCompileStepConfig {
        val index = model.steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepConfig || it is LatexmkCompileStepConfig)
        }
        return when {
            index < 0 -> LatexCompileStepConfig().also { model.steps.add(0, it) }
            model.steps[index] is LatexCompileStepConfig -> model.steps[index] as LatexCompileStepConfig
            else -> {
                val old = model.steps[index] as LatexmkCompileStepConfig
                LatexCompileStepConfig(
                    id = old.id,
                    enabled = old.enabled,
                    compiler = LatexCompiler.PDFLATEX,
                    compilerPath = old.compilerPath,
                    compilerArguments = old.compilerArguments,
                    outputFormat = Format.PDF,
                    beforeRunCommand = old.beforeRunCommand,
                ).also { model.steps[index] = it }
            }
        }
    }

    private fun ensurePrimaryCompileStepLatexmk(): LatexmkCompileStepConfig {
        val index = model.steps.indexOfFirst {
            it.enabled && (it is LatexCompileStepConfig || it is LatexmkCompileStepConfig)
        }
        return when {
            index < 0 -> LatexmkCompileStepConfig().also { model.steps.add(0, it) }
            model.steps[index] is LatexmkCompileStepConfig -> model.steps[index] as LatexmkCompileStepConfig
            else -> {
                val old = model.steps[index] as LatexCompileStepConfig
                LatexmkCompileStepConfig(
                    id = old.id,
                    enabled = old.enabled,
                    compilerPath = old.compilerPath,
                    compilerArguments = old.compilerArguments,
                    latexmkCompileMode = LatexmkCompileMode.AUTO,
                    latexmkCustomEngineCommand = null,
                    latexmkCitationTool = LatexmkCitationTool.AUTO,
                    latexmkExtraArguments = DEFAULT_LATEXMK_EXTRA_ARGUMENTS,
                    beforeRunCommand = old.beforeRunCommand,
                ).also { model.steps[index] = it }
            }
        }
    }

    private fun ensurePrimaryViewerStep(): PdfViewerStepConfig {
        val index = model.steps.indexOfFirst { it.enabled && it is PdfViewerStepConfig }
        return if (index >= 0) {
            model.steps[index] as PdfViewerStepConfig
        }
        else {
            PdfViewerStepConfig().also { model.steps.add(it) }
        }
    }
}
