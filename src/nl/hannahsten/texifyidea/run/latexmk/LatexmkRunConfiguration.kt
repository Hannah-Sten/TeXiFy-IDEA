package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.diagnostic.logging.LogConsoleManagerBase
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.AdditionalTabComponentManager
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfiguration
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompilationCapabilities
import nl.hannahsten.texifyidea.run.latex.LatexCompilationRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexOutputPath
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
import org.jdom.Element
import java.io.File
import java.util.Locale

class LatexmkRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexmkCommandLineState>(project, factory, name), LocatableConfiguration, LatexCompilationRunConfiguration {

    companion object {

        private const val PARENT = "texify-latexmk"
        private const val COMPILER_PATH = "compiler-path"
        private const val PDF_VIEWER = "pdf-viewer"
        private const val REQUIRE_FOCUS = "require-focus"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val BEFORE_RUN_COMMAND = "before-run-command"
        private const val MAIN_FILE = "main-file"
        private const val OUTPUT_PATH = "output-path"
        private const val AUXIL_PATH = "auxil-path"
        private const val WORKING_DIRECTORY = "working-directory"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"

        private const val ENGINE_MODE = "engine-mode"
        private const val CUSTOM_ENGINE_COMMAND = "custom-engine-command"
        private const val LATEXMK_OUTPUT_FORMAT = "latexmk-output-format"
        private const val CITATION_TOOL = "citation-tool"
        private const val EXTRA_ARGUMENTS = "extra-arguments"
    }

    override var compiler: LatexCompiler? = LatexCompiler.LATEXMK
    override var compilerPath: String? = null
    override var pdfViewer: PdfViewer? = null
    override var viewerCommand: String? = null

    override var compilerArguments: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    override var expandMacrosEnvVariables = false
    override var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    override var beforeRunCommand: String? = null

    override var mainFile: VirtualFile? = null
        set(value) {
            field = value
            outputPath.mainFile = value
            auxilPath.mainFile = value
        }

    override var outputPath = LatexOutputPath("out", mainFile, project)
    override var auxilPath = LatexOutputPath("auxil", mainFile, project)

    override var workingDirectory: String? = null

    override var compileTwice = false
    override var outputFormat: LatexCompiler.Format = LatexCompiler.Format.DEFAULT

    override var latexDistribution: LatexDistributionType = LatexDistributionType.MODULE_SDK

    override var isLastRunConfig = false
    override var isFirstRunConfig = true
    override var hasBeenRun = false
    override var requireFocus = true
    override var isAutoCompiling = false

    override var bibRunConfigs: Set<RunnerAndConfigurationSettings> = emptySet()
    override var makeindexRunConfigs: Set<RunnerAndConfigurationSettings> = emptySet()
    override var externalToolRunConfigs: Set<RunnerAndConfigurationSettings> = emptySet()

    override val filesToCleanUp = mutableListOf<File>()
    override val filesToCleanUpIfEmpty = mutableSetOf<File>()

    override val compilationCapabilities = LatexCompilationCapabilities(
        handlesBib = true,
        handlesMakeindex = true,
        handlesCompileCount = true,
        supportsAuxDir = true,
        supportsOutputFormatSet = true,
    )

    var engineMode: LatexmkEngineMode = LatexmkEngineMode.PDFLATEX

    var customEngineCommand: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    var latexmkOutputFormat: LatexmkOutputFormat = LatexmkOutputFormat.DEFAULT

    var citationTool: LatexmkCitationTool = LatexmkCitationTool.AUTO

    var extraArguments: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = LatexmkSettingsEditor(project)

    override fun createAdditionalTabComponents(manager: AdditionalTabComponentManager, startedProcess: ProcessHandler?) {
        super.createAdditionalTabComponents(manager, startedProcess)
        if (manager is LogConsoleManagerBase && startedProcess != null) {
            manager.addAdditionalTabComponent(LatexLogTabComponent(project, mainFile, startedProcess), "LaTeX-Log", AllIcons.Vcs.Changelist, false)
        }
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (mainFile == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        compiler = LatexCompiler.LATEXMK
        compileTwice = false
        outputFormat = LatexCompiler.Format.DEFAULT
        compilerArguments = buildLatexmkArguments()
        return LatexmkCommandLineState(environment, this)
    }

    override fun getOutputFilePath(): String {
        val outputDir = outputPath.getAndCreatePath() ?: mainFile?.parent
        val extension = latexmkOutputFormat.extension.lowercase(Locale.getDefault())
        return "${outputDir?.path}/${mainFile?.nameWithoutExtension ?: "main"}.$extension"
    }

    fun buildLatexmkArguments(): String = LatexmkCommandBuilder.buildStructuredArguments(this)

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

        val parent = element.getChild(PARENT) ?: return

        compilerPath = parent.getChildText(COMPILER_PATH).takeUnless { it.isNullOrBlank() }

        val viewerName = parent.getChildText(PDF_VIEWER)
        pdfViewer = PdfViewer.availableViewers.firstOrNull { it.name == viewerName || it.name?.uppercase() == viewerName } ?: PdfViewer.firstAvailableViewer

        requireFocus = parent.getChildText(REQUIRE_FOCUS)?.toBoolean() ?: true
        viewerCommand = parent.getChildText(VIEWER_COMMAND).takeUnless { it.isNullOrBlank() }

        environmentVariables = EnvironmentVariablesData.readExternal(parent)
        expandMacrosEnvVariables = parent.getChildText(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)?.toBoolean() ?: false

        beforeRunCommand = parent.getChildText(BEFORE_RUN_COMMAND).takeUnless { it.isNullOrBlank() }

        setMainFile(parent.getChildText(MAIN_FILE) ?: "")

        parent.getChildText(OUTPUT_PATH)?.let {
            outputPath = LatexOutputPath("out", mainFile, project)
            outputPath.pathString = it
        }

        parent.getChildText(AUXIL_PATH)?.let {
            auxilPath = LatexOutputPath("auxil", mainFile, project)
            auxilPath.pathString = it
        }

        workingDirectory = parent.getChildText(WORKING_DIRECTORY) ?: LatexOutputPath.MAIN_FILE_STRING
        latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))

        hasBeenRun = parent.getChildText(HAS_BEEN_RUN)?.toBoolean() ?: false

        engineMode = parent.getChildText(ENGINE_MODE)?.let { runCatching { LatexmkEngineMode.valueOf(it) }.getOrNull() } ?: LatexmkEngineMode.PDFLATEX
        customEngineCommand = parent.getChildText(CUSTOM_ENGINE_COMMAND)
        latexmkOutputFormat = parent.getChildText(LATEXMK_OUTPUT_FORMAT)?.let { runCatching { LatexmkOutputFormat.valueOf(it) }.getOrNull() } ?: LatexmkOutputFormat.DEFAULT
        citationTool = parent.getChildText(CITATION_TOOL)?.let { runCatching { LatexmkCitationTool.valueOf(it) }.getOrNull() } ?: LatexmkCitationTool.AUTO
        extraArguments = parent.getChildText(EXTRA_ARGUMENTS)
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        val parent = element.getChild(PARENT) ?: Element(PARENT).also { element.addContent(it) }
        parent.removeContent()

        parent.addContent(Element(COMPILER_PATH).also { it.text = compilerPath ?: "" })
        parent.addContent(Element(PDF_VIEWER).also { it.text = pdfViewer?.name ?: "" })
        parent.addContent(Element(REQUIRE_FOCUS).also { it.text = requireFocus.toString() })
        parent.addContent(Element(VIEWER_COMMAND).also { it.text = viewerCommand ?: "" })
        environmentVariables.writeExternal(parent)
        parent.addContent(Element(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES).also { it.text = expandMacrosEnvVariables.toString() })
        parent.addContent(Element(BEFORE_RUN_COMMAND).also { it.text = beforeRunCommand ?: "" })
        parent.addContent(Element(MAIN_FILE).also { it.text = mainFile?.path ?: "" })
        parent.addContent(Element(OUTPUT_PATH).also { it.text = outputPath.virtualFile?.path ?: outputPath.pathString })
        parent.addContent(Element(AUXIL_PATH).also { it.text = auxilPath.virtualFile?.path ?: auxilPath.pathString })
        parent.addContent(Element(WORKING_DIRECTORY).also { it.text = workingDirectory ?: LatexOutputPath.MAIN_FILE_STRING })
        parent.addContent(Element(LATEX_DISTRIBUTION).also { it.text = latexDistribution.name })
        parent.addContent(Element(HAS_BEEN_RUN).also { it.text = hasBeenRun.toString() })

        parent.addContent(Element(ENGINE_MODE).also { it.text = engineMode.name })
        parent.addContent(Element(CUSTOM_ENGINE_COMMAND).also { it.text = customEngineCommand ?: "" })
        parent.addContent(Element(LATEXMK_OUTPUT_FORMAT).also { it.text = latexmkOutputFormat.name })
        parent.addContent(Element(CITATION_TOOL).also { it.text = citationTool.name })
        parent.addContent(Element(EXTRA_ARGUMENTS).also { it.text = extraArguments ?: "" })
    }

    override fun getAllAuxiliaryRunConfigs(): Set<RunnerAndConfigurationSettings> = emptySet()

    override fun getResolvedWorkingDirectory(): String? = if (!workingDirectory.isNullOrBlank() && mainFile != null) {
        workingDirectory?.replace(LatexOutputPath.MAIN_FILE_STRING, mainFile!!.parent.path)
    }
    else {
        mainFile?.parent?.path
    }

    override fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == LatexOutputPath.MAIN_FILE_STRING

    override fun setMainFile(mainFilePath: String) {
        if (mainFilePath.isBlank()) {
            mainFile = null
            return
        }
        val fileSystem = LocalFileSystem.getInstance()
        val found = fileSystem.findFileByPath(mainFilePath)
        if (found?.extension == "tex") {
            mainFile = found
            return
        }
        val contentRoots = ProjectRootManager.getInstance(project).contentRoots
        for (contentRoot in contentRoots) {
            val file = contentRoot.findFileByRelativePath(mainFilePath)
            if (file?.extension == "tex") {
                mainFile = file
                return
            }
        }
        mainFile = null
    }

    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        outputPath.virtualFile = findVirtualFileByAbsoluteOrRelativePath(fileOutputPath, project)
        if (outputPath.virtualFile == null) {
            outputPath.pathString = fileOutputPath
        }
    }

    override fun setFileAuxilPath(fileAuxilPath: String) {
        if (fileAuxilPath.isBlank()) return
        auxilPath.virtualFile = findVirtualFileByAbsoluteOrRelativePath(fileAuxilPath, project)
        if (auxilPath.virtualFile == null) {
            auxilPath.pathString = fileAuxilPath
        }
    }

    override fun setSuggestedName() {
        suggestedName()?.let { name = it }
    }

    override fun suggestedName(): String? = mainFile?.nameWithoutExtension?.plus(" (latexmk)")

    override fun isGeneratedName(): Boolean {
        val file = mainFile ?: return false
        return getName() == "${file.nameWithoutExtension} (latexmk)"
    }

    fun setDefaultPdfViewer() {
        pdfViewer = PdfViewer.firstAvailableViewer
    }

    fun setDefaultLatexDistribution() {
        latexDistribution = LatexDistributionType.MODULE_SDK
    }

    fun setDefaultOutputFormat() {
        latexmkOutputFormat = LatexmkOutputFormat.DEFAULT
    }

    override fun getLatexSdk(): Sdk? = when (latexDistribution) {
        LatexDistributionType.MODULE_SDK -> {
            val sdk = mainFile?.let { LatexSdkUtil.getLatexSdkForFile(it, project) }
                ?: LatexSdkUtil.getLatexProjectSdk(project)
            if (sdk?.sdkType is LatexSdk) sdk else null
        }

        LatexDistributionType.PROJECT_SDK -> {
            val sdk = LatexSdkUtil.getLatexProjectSdk(project)
            if (sdk?.sdkType is LatexSdk) sdk else null
        }

        else -> null
    }

    override fun getLatexDistributionType(): LatexDistributionType {
        val sdk = getLatexSdk()
        val type = (sdk?.sdkType as? LatexSdk?)?.getLatexDistributionType(sdk) ?: latexDistribution
        return if (type == LatexDistributionType.MODULE_SDK || type == LatexDistributionType.PROJECT_SDK) {
            LatexDistributionType.TEXLIVE
        }
        else {
            type
        }
    }

    override fun getAuxilDirectory(): VirtualFile? = if (getLatexDistributionType().isMiktex(project, mainFile)) {
        auxilPath.getAndCreatePath() ?: outputPath.getAndCreatePath()
    }
    else {
        outputPath.getAndCreatePath()
    }

    override fun usesAuxilOrOutDirectory(): Boolean {
        val usesAuxilDir = auxilPath.getAndCreatePath() != mainFile?.parent
        val usesOutDir = outputPath.getAndCreatePath() != mainFile?.parent
        return usesAuxilDir || usesOutDir
    }

    override fun generateBibRunConfig() {
        // latexmk handles bibliography itself.
    }

    override fun clone(): RunConfiguration {
        return super.clone().also {
            val runConfiguration = it as? LatexmkRunConfiguration ?: return@also
            runConfiguration.outputPath = outputPath.clone()
            runConfiguration.auxilPath = auxilPath.clone()
        }
    }
}
