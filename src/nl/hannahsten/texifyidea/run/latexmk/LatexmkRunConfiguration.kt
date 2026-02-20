package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.diagnostic.logging.LogConsoleManagerBase
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.index.projectstructure.pathOrNull
import nl.hannahsten.texifyidea.run.latex.LatexCompilationRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.common.addTextChild
import nl.hannahsten.texifyidea.run.common.getOrCreateAndClearParent
import nl.hannahsten.texifyidea.run.common.writeCommonCompilationFields
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.settings.sdk.LatexSdk
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import org.jdom.Element
import java.nio.file.Path
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

        private const val COMPILE_MODE = "compile-mode"
        private const val CUSTOM_ENGINE_COMMAND = "custom-engine-command"
        private const val CITATION_TOOL = "citation-tool"
        private const val EXTRA_ARGUMENTS = "extra-arguments"
        private const val DEFAULT_EXTRA_ARGUMENTS = "-synctex=1"
    }

    override var compilerPath: String? = null
    override var pdfViewer: PdfViewer? = null
    var viewerCommand: String? = null

    override var compilerArguments: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    var expandMacrosEnvVariables = false
    override var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    var beforeRunCommand: String? = null

    private var mainFilePath: String = ""

    override var mainFile: VirtualFile? = null
        set(value) {
            field = value
            if (value != null) {
                mainFilePath = value.path
            }
        }
    var outputPathRaw: String = LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
    var auxilPathRaw: String = ""

    var workingDirectory: Path? = null

    var latexDistribution: LatexDistributionType = LatexDistributionType.MODULE_SDK

    var hasBeenRun = false
    var requireFocus = true
    override var isAutoCompiling = false

    var compileMode: LatexmkCompileMode = LatexmkCompileMode.PDFLATEX_PDF

    var customEngineCommand: String? = null
        set(value) {
            field = value?.trim()?.ifEmpty { null }
        }

    var citationTool: LatexmkCitationTool = LatexmkCitationTool.AUTO

    var extraArguments: String? = DEFAULT_EXTRA_ARGUMENTS
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
        if (mainFile == null && mainFilePath.isBlank()) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
        if (mainFile == null && !mainFilePath.endsWith(".tex", ignoreCase = true)) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        resolveMainFileIfNeeded()
        compilerArguments = buildLatexmkArguments()
        return LatexmkCommandLineState(environment, this)
    }

    override fun getOutputFilePath(): String {
        val outputDir = LatexmkPathResolver.resolveOutputDir(this)?.toString() ?: mainFile?.parent?.path
        val extension = compileMode.extension.lowercase(Locale.getDefault())
        return "$outputDir/${mainFile?.nameWithoutExtension ?: "main"}.$extension"
    }

    override fun getResolvedWorkingDirectory(): Path? = workingDirectory ?: mainFile?.parent?.path?.let { Path.of(it) }

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

        outputPathRaw = parent.getChildText(OUTPUT_PATH).takeUnless { it.isNullOrBlank() } ?: LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
        auxilPathRaw = parent.getChildText(AUXIL_PATH) ?: ""

        workingDirectory = parent.getChildText(WORKING_DIRECTORY)?.let { workingDirectoryText ->
            when {
                workingDirectoryText.isBlank() -> null
                workingDirectoryText == LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER -> null
                else -> pathOrNull(workingDirectoryText)
            }
        }
        latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))

        hasBeenRun = parent.getChildText(HAS_BEEN_RUN)?.toBoolean() ?: false

        compileMode = parent.getChildText(COMPILE_MODE)?.let { runCatching { LatexmkCompileMode.valueOf(it) }.getOrNull() }
            ?: LatexmkCompileMode.PDFLATEX_PDF

        customEngineCommand = parent.getChildText(CUSTOM_ENGINE_COMMAND)
        citationTool = parent.getChildText(CITATION_TOOL)?.let { runCatching { LatexmkCitationTool.valueOf(it) }.getOrNull() } ?: LatexmkCitationTool.AUTO
        extraArguments = parent.getChildText(EXTRA_ARGUMENTS) ?: DEFAULT_EXTRA_ARGUMENTS
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super<RunConfigurationBase>.writeExternal(element)

        val parent = getOrCreateAndClearParent(element, PARENT)
        writeCommonCompilationFields(
            parent = parent,
            compilerPath = compilerPath,
            pdfViewerName = pdfViewer?.name,
            requireFocus = requireFocus,
            viewerCommand = viewerCommand,
            writeEnvironmentVariables = environmentVariables::writeExternal,
            expandMacrosEnvVariables = expandMacrosEnvVariables,
            beforeRunCommand = beforeRunCommand,
            mainFilePath = mainFile?.path ?: mainFilePath,
            workingDirectory = workingDirectory?.toString() ?: LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER,
            latexDistribution = latexDistribution.name,
            hasBeenRun = hasBeenRun,
        )
        parent.addTextChild(OUTPUT_PATH, outputPathRaw)
        parent.addTextChild(AUXIL_PATH, auxilPathRaw)
        parent.addTextChild(COMPILE_MODE, compileMode.name)
        parent.addTextChild(CUSTOM_ENGINE_COMMAND, customEngineCommand ?: "")
        parent.addTextChild(CITATION_TOOL, citationTool.name)
        parent.addTextChild(EXTRA_ARGUMENTS, extraArguments ?: "")
    }

    fun setMainFile(mainFilePath: String) {
        if (mainFilePath.isBlank()) {
            this.mainFilePath = ""
            mainFile = null
            return
        }
        this.mainFilePath = mainFilePath

        val fileSystem = LocalFileSystem.getInstance()
        if (ApplicationManager.getApplication().isDispatchThread) {
            val cachedFile = fileSystem.findFileByPathIfCached(mainFilePath)
            if (cachedFile?.extension == "tex") {
                mainFile = cachedFile
            }
            else if (mainFile?.path != mainFilePath) {
                mainFile = null
            }
            return
        }

        mainFile = resolveMainFile(mainFilePath)
    }

    fun getMainFilePath(): String = mainFile?.path ?: mainFilePath

    fun resolveMainFileIfNeeded(): VirtualFile? {
        if (mainFile != null || mainFilePath.isBlank()) {
            return mainFile
        }
        mainFile = resolveMainFile(mainFilePath)
        return mainFile
    }

    private fun resolveMainFile(mainFilePath: String): VirtualFile? {
        val fileSystem = LocalFileSystem.getInstance()
        val found = fileSystem.findFileByPath(mainFilePath)
        if (found?.extension == "tex") {
            return found
        }
        val contentRoots = ProjectRootManager.getInstance(project).contentRoots
        for (contentRoot in contentRoots) {
            val file = contentRoot.findFileByRelativePath(mainFilePath)
            if (file?.extension == "tex") {
                return file
            }
        }
        return null
    }

    override fun setFileOutputPath(fileOutputPath: String) {
        outputPathRaw = fileOutputPath.takeUnless { it.isBlank() } ?: LatexmkPathResolver.MAIN_FILE_PARENT_PLACEHOLDER
    }

    fun setFileAuxilPath(fileAuxilPath: String) {
        auxilPathRaw = fileAuxilPath
    }

    fun setSuggestedName() {
        suggestedName()?.let { name = it }
    }

    override fun suggestedName(): String? = mainFile?.nameWithoutExtension?.plus(" (latexmk)")

    override fun isGeneratedName(): Boolean {
        val file = mainFile ?: return false
        return name == "${file.nameWithoutExtension} (latexmk)"
    }

    fun setDefaultPdfViewer() {
        pdfViewer = PdfViewer.firstAvailableViewer
    }

    fun setDefaultLatexDistribution() {
        latexDistribution = LatexDistributionType.MODULE_SDK
    }

    fun setDefaultOutputFormat() {
        compileMode = LatexmkCompileMode.PDFLATEX_PDF
    }

    override fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == null

    fun getLatexSdk(): Sdk? = when (latexDistribution) {
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

    override fun getOutputDirectory(): VirtualFile? = LatexmkPathResolver.toVirtualFile(LatexmkPathResolver.resolveOutputDir(this))

    override fun getAuxilDirectory(): VirtualFile? {
        val outputDir = LatexmkPathResolver.resolveOutputDir(this)
        val auxilDir = LatexmkPathResolver.resolveAuxDir(this) ?: outputDir
        return LatexmkPathResolver.toVirtualFile(auxilDir)
    }

    override fun clone(): RunConfiguration = super.clone()
}
