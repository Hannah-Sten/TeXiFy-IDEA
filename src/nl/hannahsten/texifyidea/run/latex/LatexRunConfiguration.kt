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
import com.intellij.openapi.application.*
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import nl.hannahsten.texifyidea.index.NewCommandsIndex
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexGenericRegularCommand
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.SumatraViewer
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.findVirtualFileByAbsoluteOrRelativePath
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.magic.cmd
import nl.hannahsten.texifyidea.util.parser.hasBibliography
import nl.hannahsten.texifyidea.util.parser.usesBiber
import nl.hannahsten.texifyidea.util.runInBackgroundNonBlocking
import org.jdom.Element
import java.io.File
import java.nio.file.InvalidPathException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<LatexCommandLineState>(project, factory, name), LocatableConfiguration {

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
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val BIB_RUN_CONFIG = "bib-run-config"
        private const val MAKEINDEX_RUN_CONFIG = "makeindex-run-config"
        private const val EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES = "expand-macros-in-environment-variables"

        // For backwards compatibility
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
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

    var mainFile: VirtualFile? = null
        set(value) {
            field = value
            this.outputPath.mainFile = value
            this.outputPath.contentRoot = getMainFileContentRoot()
            this.auxilPath.mainFile = value
            this.auxilPath.contentRoot = getMainFileContentRoot()
        }

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: SmartPsiElementPointer<PsiFile>? = null

    /** Path to the directory containing the output files. */
    var outputPath = LatexOutputPath("out", getMainFileContentRoot(), mainFile, project)

    /** Path to the directory containing the auxiliary files. */
    var auxilPath = LatexOutputPath("auxil", getMainFileContentRoot(), mainFile, project)

    var compileTwice = false
    var outputFormat: Format = Format.PDF

    /**
     * Use [getLatexDistributionType] to take the Project SDK into account.
     */
    internal var latexDistribution = LatexDistributionType.PROJECT_SDK

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig = false
    var isFirstRunConfig = true

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun = false

    /** Whether the pdf viewer should claim focus after compilation. */
    var requireFocus = true

    /** Whether the run configuration is currently auto-compiling.     */
    var isAutoCompiling = false

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

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return LatexSettingsEditor(project)
    }

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
        val filter = RegexpFilter(
            environment.project,
            "^\$FILE_PATH$:\$LINE$"
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

        // Read compiler.
        val compilerName = parent.getChildText(COMPILER)
        try {
            this.compiler = LatexCompiler.valueOf(compilerName)
        }
        catch (e: IllegalArgumentException) {
            this.compiler = null
        }

        // Read compiler custom path.
        val compilerPathRead = parent.getChildText(COMPILER_PATH)
        this.compilerPath = if (compilerPathRead.isNullOrEmpty()) null else compilerPathRead

        // Read pdf viewer.
        val viewerName = parent.getChildText(PDF_VIEWER)
        // For backwards compatibility (0.10.3 and earlier), handle uppercase names
        this.pdfViewer = PdfViewer.availableViewers.firstOrNull { it.name == viewerName || it.name?.uppercase() == viewerName } ?: PdfViewer.firstAvailableViewer

        this.requireFocus = parent.getChildText(REQUIRE_FOCUS)?.toBoolean() ?: true

        // Read custom pdf viewer command
        val viewerCommandRead = parent.getChildText(VIEWER_COMMAND)
        this.viewerCommand = if (viewerCommandRead.isNullOrEmpty()) null else viewerCommandRead

        // Backwards compatibility (0.10.3 and earlier): path to SumatraPDF executable
        parent.getChildText(SUMATRA_PATH)?.let { folder ->
            // the previous setting was the folder containing the SumatraPDF executable
            runInBackgroundNonBlocking(project, "Set SumatraPDF path") {
                // a write action
                val settings = TexifySettings.getInstance()
                val isSumatraPathSet = readAction { settings.pathToSumatra != null }
                if (isSumatraPathSet) {
                    return@runInBackgroundNonBlocking
                }
                val path = try {
                    Path(folder).resolve("SumatraPDF.exe")
                }
                catch (e: InvalidPathException) {
                    return@runInBackgroundNonBlocking
                    // If the path is invalid, we just ignore it
                }
                if (SumatraViewer.trySumatraPath(path)) {
                    writeAction {
                        TexifySettings.getInstance().pathToSumatra = path.absolutePathString()
                    }
                }
            }
        }

        // Read compiler arguments.
        val compilerArgumentsRead = parent.getChildText(COMPILER_ARGUMENTS)
        compilerArguments = if (compilerArgumentsRead.isNullOrEmpty()) null else compilerArgumentsRead

        // Read environment variables
        environmentVariables = EnvironmentVariablesData.readExternal(parent)

        // Read whether to expand macros in run configuration
        val expandMacrosRunConfigurationBoolean = parent.getChildText(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES)
        if (expandMacrosRunConfigurationBoolean == null) {
            this.expandMacrosEnvVariables = false
        }
        else {
            this.expandMacrosEnvVariables = expandMacrosRunConfigurationBoolean.toBoolean()
        }

        val beforeRunCommandRead = parent.getChildText(BEFORE_RUN_COMMAND)
        beforeRunCommand = if (beforeRunCommandRead.isNullOrEmpty()) null else beforeRunCommandRead

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
        val auxDirBoolean = parent.getChildText(AUX_DIR)
        if (auxDirBoolean != null && this.auxilPath.virtualFile == null && this.mainFile != null) {
            // If there is no auxil path yet but this option still exists,
            // guess the output path in the same way as it was previously done
            val usesAuxDir = java.lang.Boolean.parseBoolean(auxDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
            val path = if (usesAuxDir) moduleRoot?.path + "/auxil" else this.mainFile!!.parent.path
            this.auxilPath.pathString = path
        }
        val outDirBoolean = parent.getChildText(OUT_DIR)
        if (outDirBoolean != null && this.outputPath.virtualFile == null && this.mainFile != null) {
            val usesOutDir = java.lang.Boolean.parseBoolean(outDirBoolean)
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
            val path = if (usesOutDir) moduleRoot?.path + "/out" else this.mainFile!!.parent.path
            this.outputPath.pathString = path
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
        this.outputFormat = Format.byNameIgnoreCase(parent.getChildText(OUTPUT_FORMAT))

        // Read LatexDistribution
        this.latexDistribution = LatexDistributionType.valueOfIgnoreCase(parent.getChildText(LATEX_DISTRIBUTION))

        // Read whether the run config has been run
        this.hasBeenRun = parent.getChildText(HAS_BEEN_RUN)?.toBoolean() ?: false

        // Read bibliography run configurations, which is a list of ids
        val bibRunConfigElt = parent.getChildText(BIB_RUN_CONFIG)
        // Assume the list is of the form [id 1,id 2]
        this.bibRunConfigIds = bibRunConfigElt?.drop(1)?.dropLast(1)?.split(", ")?.toMutableSet() ?: mutableSetOf()

        // Read makeindex run configurations
        val makeindexRunConfigElt = parent.getChildText(MAKEINDEX_RUN_CONFIG)
        if (makeindexRunConfigElt != null) {
            this.makeindexRunConfigIds = makeindexRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()
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

        parent.addContent(Element(COMPILER).also { it.text = compiler?.name ?: "" })
        parent.addContent(Element(COMPILER_PATH).also { it.text = compilerPath ?: "" })
        parent.addContent(Element(PDF_VIEWER).also { it.text = pdfViewer?.name ?: "" })
        parent.addContent(Element(REQUIRE_FOCUS).also { it.text = requireFocus.toString() })
        parent.addContent(Element(VIEWER_COMMAND).also { it.text = viewerCommand ?: "" })
        parent.addContent(Element(COMPILER_ARGUMENTS).also { it.text = this.compilerArguments ?: "" })
        this.environmentVariables.writeExternal(parent)
        parent.addContent(Element(EXPAND_MACROS_IN_ENVIRONMENT_VARIABLES).also { it.text = expandMacrosEnvVariables.toString() })
        parent.addContent(Element(BEFORE_RUN_COMMAND).also { it.text = this.beforeRunCommand ?: "" })
        parent.addContent(Element(MAIN_FILE).also { it.text = mainFile?.path ?: "" })
        parent.addContent(Element(OUTPUT_PATH).also { it.text = outputPath.virtualFile?.path ?: outputPath.pathString })
        parent.addContent(Element(AUXIL_PATH).also { it.text = auxilPath.virtualFile?.path ?: auxilPath.pathString })
        parent.addContent(Element(COMPILE_TWICE).also { it.text = compileTwice.toString() })
        parent.addContent(Element(OUTPUT_FORMAT).also { it.text = outputFormat.name })
        parent.addContent(Element(LATEX_DISTRIBUTION).also { it.text = latexDistribution.name })
        parent.addContent(Element(HAS_BEEN_RUN).also { it.text = hasBeenRun.toString() })
        parent.addContent(Element(BIB_RUN_CONFIG).also { it.text = bibRunConfigIds.toString() })
        parent.addContent(Element(MAKEINDEX_RUN_CONFIG).also { it.text = makeindexRunConfigIds.toString() })
    }

    /**
     * Create a new bib run config and add it to the set.
     */
    private fun addBibRunConfig(defaultCompiler: BibliographyCompiler, mainFile: VirtualFile?, compilerArguments: String? = null) {
        val runManager = RunManagerImpl.getInstanceImpl(project)

        val bibSettings = runManager.createConfiguration(
            "",
            LatexConfigurationFactory(BibtexRunConfigurationType())
        )

        val bibtexRunConfiguration = bibSettings.configuration as BibtexRunConfiguration

        bibtexRunConfiguration.compiler = defaultCompiler
        if (compilerArguments != null) bibtexRunConfiguration.compilerArguments = compilerArguments
        bibtexRunConfiguration.mainFile = mainFile
        bibtexRunConfiguration.setSuggestedName()
        bibtexRunConfiguration.setDefaultDistribution(latexDistribution)

        // On non-MiKTeX systems, add bibinputs for bibtex to work
        if (!latexDistribution.isMiktex(project)) {
            // Only if default, because the user could have changed it after creating the run config but before running
            if (mainFile != null && outputPath.virtualFile != mainFile.parent) {
                // As seen in issue 2165, appending a colon (like with TEXINPUTS) may not work on Windows,
                // however it may be necessary on Mac/Linux as seen in #2249.
                bibtexRunConfiguration.environmentVariables = bibtexRunConfiguration.environmentVariables.with(mapOf("BIBINPUTS" to mainFile.parent.path, "BSTINPUTS" to mainFile.parent.path + File.pathSeparator))
            }
        }

        runManager.addConfiguration(bibSettings)

        bibRunConfigs = bibRunConfigs + setOf(bibSettings)
    }

    /**
     * Generate a Bibtex run configuration, after trying to guess whether the user wants to use bibtex or biber as compiler.
     */
    internal fun generateBibRunConfig() {
        val psiFile = this.psiFile?.element ?: return // Do not auto-generate a bib run config when there is no psi file
        // Get a pair of Bib compiler and compiler arguments.
        val compilerFromMagicComment: Pair<BibliographyCompiler, String>? by lazy {
            val runCommand = psiFile.allParentMagicComments()
                .value(DefaultMagicKeys.BIBTEXCOMPILER) ?: return@lazy null
            val compilerString = if (runCommand.contains(' ')) {
                runCommand.let { it.subSequence(0, it.indexOf(' ')) }.trim()
                    .toString()
            }
            else runCommand
            val compiler = BibliographyCompiler.valueOf(compilerString.uppercase(Locale.getDefault()))
            val compilerArguments = runCommand.removePrefix(compilerString)
                .trim()
            Pair(compiler, compilerArguments)
        }

        val defaultCompiler = when {
            compilerFromMagicComment != null -> compilerFromMagicComment!!.first
            psiFile.hasBibliography() -> BibliographyCompiler.BIBTEX
            psiFile.usesBiber() -> BibliographyCompiler.BIBER
            else -> return // Do not auto-generate a bib run config when we can't detect bibtex
        }

        // When chapterbib is used, every chapter has its own bibliography and needs its own run config
        val usesChapterbib = psiFile.includedPackages().contains(LatexPackage.CHAPTERBIB)

        if (!usesChapterbib) {
            addBibRunConfig(defaultCompiler, mainFile, compilerFromMagicComment?.second)
        }
        else {
            val allBibliographyCommands =
                NewCommandsIndex.getByNameInFileSet(LatexGenericRegularCommand.BIBLIOGRAPHY.cmd, psiFile)

            // We know that there can only be one bibliography per top-level \include,
            // however not all of them may contain a bibliography, and the ones
            // that do have one can have it in any included file
            psiFile.traverseCommands()
                .filter { it.name == LatexGenericRegularCommand.INCLUDE.cmd }
                .flatMap { command -> command.requiredParametersText() }
                .forEach { filename ->
                    // Find all the files of this chapter, then check if any of the bibliography commands appears in a file in this chapter
                    val chapterMainFile = psiFile.findFile(filename, supportsAnyExtension = true)
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
    fun setMainFile(mainFilePath: String) {
        if (mainFilePath.isBlank()) {
            this.mainFile = null
            return
        }
        val isDispatchThread = ApplicationManager.getApplication().isDispatchThread
        val fileSystem = LocalFileSystem.getInstance()
        // Check if the file is valid and exists
        val mainFile = if (isDispatchThread) {
            fileSystem.findFileByPath(mainFilePath)
        }
        else {
            // this is a read action
            ReadAction.compute<VirtualFile?, Throwable> { fileSystem.findFileByPath(mainFilePath) }
        }

        if (mainFile?.extension == "tex") {
            this.mainFile = mainFile
            return
        }
        // Maybe it is a relative path
        val projectRootManager =
            ProjectRootManager.getInstance(project)
        val contentRoots = if (isDispatchThread) {
            projectRootManager.contentRoots
        }
        else {
            // a read action again
            ReadAction.compute<Array<VirtualFile>, Throwable> { projectRootManager.contentRoots }
        }
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
            LatexSdkUtil.getLatexDistributionType(project) ?: LatexDistributionType.TEXLIVE
        }
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder when used.
     */
    fun getAuxilDirectory(): VirtualFile? {
        return if (latexDistribution.isMiktex(project)) {
            // If we are using MiKTeX it might still be we are not using an auxil directory, so then fall back to the out directory
            auxilPath.getAndCreatePath() ?: outputPath.getAndCreatePath()
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
            .nameWithoutExtension + "." + if (outputFormat == Format.DEFAULT) "pdf"
        else outputFormat.toString()
            .lowercase(Locale.getDefault())
    }

    /**
     * Set [outputPath]
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.isBlank()) return
        this.outputPath.virtualFile = findVirtualFileByAbsoluteOrRelativePath(fileOutputPath, project)
        // If not possible to resolve directly, we might resolve it later
        if (this.outputPath.virtualFile == null) {
            this.outputPath.pathString = fileOutputPath
        }
    }

    /**
     * Get the content root of the main file.
     */
    fun getMainFileContentRoot(): VirtualFile? {
        if (mainFile == null) return null
        if (!project.isInitialized) return null
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
        // If not possible to resolve directly, we might resolve it later
        if (this.auxilPath.virtualFile == null) {
            this.auxilPath.pathString = fileAuxilPath
        }
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
        return "LatexRunConfiguration{" + "compiler=" + compiler +
                ", compilerPath=" + compilerPath +
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
}
