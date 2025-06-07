package nl.hannahsten.texifyidea.run

import com.intellij.configurationStore.deserializeAndLoadState
import com.intellij.configurationStore.serializeStateInto
import com.intellij.execution.CommonProgramRunConfigurationParameters
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
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
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler.OutputFormat
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationAbstractOutputPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationOptions
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationOutputPathOption
import nl.hannahsten.texifyidea.run.options.LatexRunConfigurationPathOption
import nl.hannahsten.texifyidea.run.step.Step
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.run.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import org.jdom.Element
import java.nio.file.Path

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexRunState>(project, factory, name), LocatableConfiguration, CommonProgramRunConfigurationParameters {

    companion object {

        private const val TEXIFY_PARENT = "texify"
        private const val COMPILE_STEP = "compile-step"
        private const val COMPILE_STEP_NAME_ATTR = "step-name" // Should avoid conflicts with any possible step state variables
    }

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    // todo if this is the main file, it should be updated when main file is set?
    var psiFile: PsiFile? = null

    /** A run configuration consists of one or more steps to execute. */
    val compileSteps: MutableList<Step> = mutableListOf()
        get() = field.onEach { it.configuration = this@LatexRunConfiguration }

    override fun clone(): LatexRunConfiguration {
        // Super already handles copying the options
        val new = super.clone() as? LatexRunConfiguration ?: LatexRunConfiguration(project, factory ?: LatexTemplateConfigurationFactory(latexRunConfigurationType()), name)
        val newSteps = compileSteps.map { it.clone() }.onEach { it.configuration = new }
        new.compileSteps.clear()
        new.compileSteps.addAll(newSteps)

        return new
    }

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
            throw RuntimeConfigurationError("Run configuration is invalid: no compiler selected")
        }
        if (options.mainFile.resolve() == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
        if (compileSteps.isEmpty()) {
            throw RuntimeConfigurationError("Run configuration is invalid: at least one compile step needs to be present")
        }
        compileSteps.firstOrNull { !it.isValid() }?.let { throw RuntimeConfigurationError("The ${it.name} is not valid") }
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return LatexRunState(this, environment)
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        val parent = element.getChild(TEXIFY_PARENT) ?: return

        this.options.environmentVariables = EnvironmentVariablesData.readExternal(parent)

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

        // When these options are set to their default setting, they are not serialized. So when reading the run configuration we have to restore these to their default.
        if (options.workingDirectory.isDefault()) {
            options.mainFile.resolve()?.let { mainFile ->
                options.workingDirectory = LatexRunConfigurationPathOption.createDefaultWorkingDirectory(mainFile)
            }
        }

        if (options.outputPath.isDefault()) {
            options.outputPath = LatexRunConfigurationAbstractOutputPathOption.getDefault("out", this.project)
        }

        if (options.auxilPath.isDefault()) {
            options.auxilPath = LatexRunConfigurationAbstractOutputPathOption.getDefault("aux", this.project)
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
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder.
     */
    fun getAuxilDirectory(): VirtualFile? {
        return if (options.latexDistribution.isMiktex(project)) {
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
     * Set output path (should be a directory). Should NOT contain macros (use [LatexRunConfigurationPathOption#resolveAndGetPath] for that).
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        options.outputPath = LatexRunConfigurationOutputPathOption(fileOutputPath)
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
                ", mainFile=" + options.mainFile.resolve() +
                ", outputFormat=" + options.outputFormat +
                '}'.toString()
    }

    // equals() is final and cannot be overridden
    fun myEquals(other: Any?): Boolean {
        if (other !is LatexRunConfiguration) return false
        // Not sure if this is completely right, but we use something similar in OtherRunConfigurationStep
        return other.type.id == this.type.id && other.name == this.name
    }

    override fun getProgramParameters() = options.compilerArguments

    override fun setProgramParameters(value: String?) {
        options.compilerArguments = value
    }

    override fun getWorkingDirectory(): String? = options.workingDirectory.resolvedPath ?: PathUtil.toSystemDependentName(project.basePath)

    override fun setWorkingDirectory(resolvedPath: String?) {
        options.workingDirectory = LatexRunConfigurationPathOption(resolvedPath)
    }

    fun hasDefaultWorkingDirectory(): Boolean {
        if (workingDirectory == null || options.mainFile.resolve() == null) return false
        return Path.of(workingDirectory!!).toAbsolutePath() == Path.of(options.mainFile.resolve()!!.path).parent.toAbsolutePath()
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
