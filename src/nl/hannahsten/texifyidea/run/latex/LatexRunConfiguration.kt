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
import nl.hannahsten.texifyidea.run.latex.flow.LatexStepRunState
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import org.jdom.Element
import java.nio.file.Path

class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexRunConfigurationOptions>(project, factory, name), LocatableConfiguration {

    companion object {

        internal const val DEFAULT_LATEXMK_EXTRA_ARGUMENTS = "-synctex=1"
    }

    var isAutoCompiling = false

    override fun getOptions(): LatexRunConfigurationOptions =
        super.getOptions() as LatexRunConfigurationOptions

    internal val configOptions: LatexRunConfigurationOptions
        get() = getOptions()

    private var steps: MutableList<LatexStepRunConfigurationOptions>
        get() = configOptions.steps
        set(value) {
            configOptions.steps = value
        }

    var mainFilePath: String?
        get() = configOptions.mainFilePath
        set(value) {
            configOptions.mainFilePath = value?.trim()?.ifEmpty { null }
        }

    var outputPath: Path?
        get() = configOptions.outputPath?.let(::pathOrNull)
        set(value) {
            configOptions.outputPath = value?.toString() ?: LatexPathResolver.defaultOutputPath.toString()
        }

    var auxilPath: Path?
        get() = configOptions.auxilPath?.let(::pathOrNull)
        set(value) {
            configOptions.auxilPath = value?.toString() ?: LatexPathResolver.defaultAuxilPath.toString()
        }

    var workingDirectory: Path?
        get() = configOptions.workingDirectoryPath?.let(::pathOrNull)
        set(value) {
            configOptions.workingDirectoryPath = value?.toString()
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
        }

    var pdfViewer: PdfViewer?
        get() {
            val viewerName = primaryViewerStep()?.pdfViewerName
            return PdfViewer.availableViewers.firstOrNull { it.name == viewerName }
                ?: PdfViewer.firstAvailableViewer
        }
        set(value) {
            val step = ensurePrimaryViewerStep()
            step.pdfViewerName = value?.name
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexSettingsEditor(this)

    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)
        LegacyLatexRunConfigMigration.migrateIfNeeded(this, element)
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (mainFilePath.isNullOrBlank()) {
            throw RuntimeConfigurationError("Run configuration is invalid: no main LaTeX file path selected")
        }
        if (LatexRunConfigurationStaticSupport.resolveMainFile(this) == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
        if (steps.isEmpty()) {
            throw RuntimeConfigurationError("Run configuration is invalid: no compile steps")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        configOptions.ensureDefaultSteps()

        val configuredSteps = steps
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

    fun getLatexSdk(): Sdk? = when (latexDistribution) {
        LatexDistributionType.MODULE_SDK -> {
            val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(this)
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
        cloned.isAutoCompiling = false
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

    internal fun primaryCompileStep(): LatexStepRunConfigurationOptions? =
        steps.firstOrNull { it is LatexCompileStepOptions || it is LatexmkCompileStepOptions }

    internal fun hasEnabledLatexmkStep(): Boolean =
        steps.any { it is LatexmkCompileStepOptions }

    internal fun primaryCompiler(): LatexCompiler? = when (val step = primaryCompileStep()) {
        is LatexCompileStepOptions -> step.compiler
        else -> null
    }

    internal fun primaryCompilerPath(): String? = when (val step = primaryCompileStep()) {
        is LatexCompileStepOptions -> step.compilerPath
        is LatexmkCompileStepOptions -> step.compilerPath
        else -> null
    }

    internal fun primaryViewerStep(): PdfViewerStepOptions? =
        steps.firstOrNull { it is PdfViewerStepOptions } as? PdfViewerStepOptions

    internal fun ensurePrimaryCompileStepLatexmk(): LatexmkCompileStepOptions {
        val index = steps.indexOfFirst {
            it is LatexCompileStepOptions || it is LatexmkCompileStepOptions
        }
        return when {
            index < 0 -> LatexmkCompileStepOptions().also { steps.add(0, it) }
            steps[index] is LatexmkCompileStepOptions -> steps[index] as LatexmkCompileStepOptions
            else -> {
                val old = steps[index] as LatexCompileStepOptions
                LatexmkCompileStepOptions().also {
                    it.id = old.id
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
        val index = steps.indexOfFirst { it is PdfViewerStepOptions }
        return if (index >= 0) {
            steps[index] as PdfViewerStepOptions
        }
        else {
            PdfViewerStepOptions().also { steps.add(it) }
        }
    }
}
