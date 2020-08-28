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
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.allParentMagicComments
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.util.allCommands
import nl.hannahsten.texifyidea.util.files.commandsInFileSet
import nl.hannahsten.texifyidea.util.files.findFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.hasBibliography
import nl.hannahsten.texifyidea.util.includedPackages
import nl.hannahsten.texifyidea.util.usesBiber
import org.jdom.Element

/**
 * @author Hannah Schellekens, Sten Wessel
 */
class LatexRunConfiguration constructor(project: Project,
                                        factory: ConfigurationFactory,
                                        name: String
) : RunConfigurationBase<LatexCommandLineState>(project, factory, name), LocatableConfiguration {

    companion object {
        private const val TEXIFY_PARENT = "texify"
        private const val COMPILER = "compiler"
        private const val COMPILER_PATH = "compiler-path"
        private const val SUMATRA_PATH = "sumatra-path"
        private const val VIEWER_COMMAND = "viewer-command"
        private const val COMPILER_ARGUMENTS = "compiler-arguments"
        private const val MAIN_FILE = "main-file"
        private const val OUTPUT_PATH = "output-path"
        private const val AUXIL_PATH = "auxil-path"
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val LATEX_DISTRIBUTION = "latex-distribution"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val BIB_RUN_CONFIG = "bib-run-config"
        private const val MAKEINDEX_RUN_CONFIG = "makeindex-run-config"

        // For backwards compatibility
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
    }

    var compiler: LatexCompiler? = null
    var compilerPath: String? = null
    var sumatraPath: String? = null
    var viewerCommand: String? = null

    var compilerArguments: String? = null
        set(compilerArguments) {
            field = compilerArguments?.trim()
            if (field?.isEmpty() == true) {
                field = null
            }
        }

    var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT

    var mainFile: VirtualFile? = null

    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: PsiFile? = null

    /** Path to the directory containing the output files. */
    var outputPath: VirtualFile? = null
        get() {
            // When the user modifies the run configuration template, then this variable will magically be replaced with the
            // path to the /bin folder of IntelliJ, without the setter being called.
            return if (field?.path?.endsWith("/bin") == true) {
                field = null
                field
            }
            else {
                field
            }
        }

    /** Path to the directory containing the auxiliary files. */
    var auxilPath: VirtualFile? = null
        get() {
            // When the user modifies the run configuration template, then this variable will magically be replaced with the
            // path to the /bin folder of IntelliJ, without the setter being called.
            return if (field?.path?.endsWith("/bin") == true) {
                field = null
                field
            }
            else {
                field
            }
        }

    var compileTwice = false
    var outputFormat: Format = Format.PDF
    var latexDistribution: LatexDistributionType = LatexDistributionType.TEXLIVE

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig = false
    var isFirstRunConfig = true

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun = false

    /** Whether the pdf viewer is allowed to claim focus after compilation. */
    var allowFocusChange = true

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

    // todo support multiple makeindex run configs
    private var makeindexRunConfigId = ""
    var makeindexRunConfig: RunnerAndConfigurationSettings?
        get() = RunManagerImpl.getInstanceImpl(project)
                .getConfigurationById(makeindexRunConfigId)
        set(makeindexRunConfig) {
            this.makeindexRunConfigId = makeindexRunConfig?.uniqueID ?: ""
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return LatexSettingsEditor(project)
    }

    override fun createAdditionalTabComponents(manager: AdditionalTabComponentManager,
                                               startedProcess: ProcessHandler?) {
        super.createAdditionalTabComponents(manager, startedProcess)

        if (manager is LogConsoleManagerBase && startedProcess != null) {
            manager.addAdditionalTabComponent(LatexLogTabComponent(project, mainFile, startedProcess), "LaTeX-Log", AllIcons.Vcs.Changelist, false)
        }
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError(
                    "Run configuration is invalid: no compiler selected")
        }
        if (mainFile == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
    }

    @Throws(ExecutionException::class)
    override fun getState(executor: Executor,
                          environment: ExecutionEnvironment): RunProfileState? {
        val filter = RegexpFilter(environment.project,
                "^\$FILE_PATH$:\$LINE$")

        val state = LatexCommandLineState(environment,
                this)
        state.addConsoleFilters(filter)
        return state
    }

    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element) {
        super<RunConfigurationBase>.readExternal(element)

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

        // Read SumatraPDF custom path
        val sumatraPathRead = parent.getChildText(SUMATRA_PATH)
        this.sumatraPath = if (sumatraPathRead.isNullOrEmpty()) null else sumatraPathRead

        // Read custom pdf viewer command
        val viewerCommandRead = parent.getChildText(VIEWER_COMMAND)
        this.viewerCommand = if (viewerCommandRead.isNullOrEmpty()) null else viewerCommandRead

        // Read compiler arguments.
        val compilerArgumentsRead = parent.getChildText(COMPILER_ARGUMENTS)
        compilerArguments = if (compilerArgumentsRead.isNullOrEmpty()) null else compilerArgumentsRead

        // Read environment variables
        environmentVariables = EnvironmentVariablesData.readExternal(parent)

        // Read main file.
        val filePath = parent.getChildText(MAIN_FILE)
        setMainFile(filePath)

        val fileSystem = LocalFileSystem.getInstance()

        // Read output path
        val outputPathString = parent.getChildText(OUTPUT_PATH)
        if (outputPathString != null) {
            if (outputPathString.endsWith("/bin")) {
                this.outputPath = getDefaultOutputPath()
            }
            else {
                this.outputPath = fileSystem.findFileByPath(outputPathString)
            }
        }

        // Read auxil path
        val auxilPathString = parent.getChildText(AUXIL_PATH)
        if (auxilPathString != null) {
            this.auxilPath = fileSystem.findFileByPath(auxilPathString)
        }

        // Backwards compatibility
        runReadAction {
            val auxDirBoolean = parent.getChildText(AUX_DIR)
            if (auxDirBoolean != null && this.auxilPath == null && this.mainFile != null) {
                // If there is no auxil path yet but this option still exists,
                // guess the output path in the same way as it was previously done
                val usesAuxDir = java.lang.Boolean.parseBoolean(auxDirBoolean)
                val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
                val path = if (usesAuxDir) moduleRoot?.path + "/auxil" else this.mainFile!!.parent.path
                this.auxilPath = LocalFileSystem.getInstance().findFileByPath(path)
            }
            val outDirBoolean = parent.getChildText(OUT_DIR)
            if (outDirBoolean != null && this.outputPath == null && this.mainFile != null) {
                val usesOutDir = java.lang.Boolean.parseBoolean(outDirBoolean)
                val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(this.mainFile!!)
                val path = if (usesOutDir) moduleRoot?.path + "/out" else this.mainFile!!.parent.path
                this.outputPath = LocalFileSystem.getInstance().findFileByPath(path)
            }
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
        this.bibRunConfigIds = bibRunConfigElt.drop(1).dropLast(1).split(", ").toMutableSet()

        // Read makeindex run configuration
        val makeindexRunConfigElt = parent.getChildText(MAKEINDEX_RUN_CONFIG)
        this.makeindexRunConfigId = makeindexRunConfigElt ?: ""
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
        parent.addContent(Element(SUMATRA_PATH).also { it.text = sumatraPath ?: "" })
        parent.addContent(Element(VIEWER_COMMAND).also { it.text = viewerCommand ?: "" })
        parent.addContent(Element(COMPILER_ARGUMENTS).also { it.text = this.compilerArguments ?: "" })
        this.environmentVariables.writeExternal(parent)
        parent.addContent(Element(MAIN_FILE).also { it.text = mainFile?.path ?: "" })
        parent.addContent(Element(OUTPUT_PATH).also { it.text = outputPath?.path ?: "" })
        parent.addContent(Element(AUXIL_PATH).also { it.text = auxilPath?.path ?: "" })
        parent.addContent(Element(COMPILE_TWICE).also { it.text = compileTwice.toString() })
        parent.addContent(Element(OUTPUT_FORMAT).also { it.text = outputFormat.name })
        parent.addContent(Element(LATEX_DISTRIBUTION).also { it.text = latexDistribution.name })
        parent.addContent(Element(HAS_BEEN_RUN).also { it.text = hasBeenRun.toString() })
        parent.addContent(Element(BIB_RUN_CONFIG).also { it.text = bibRunConfigIds.toString() })
        parent.addContent(Element(MAKEINDEX_RUN_CONFIG).also { it.text = makeindexRunConfigId })
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

        runManager.addConfiguration(bibSettings)

        bibRunConfigs = bibRunConfigs + setOf(bibSettings)
    }

    /**
     * Generate a Bibtex run configuration, after trying to guess whether the user wants to use bibtex or biber as compiler.
     */
    internal fun generateBibRunConfig() {
        // Get a pair of Bib compiler and compiler arguments.
        val compilerFromMagicComment: Pair<BibliographyCompiler, String>? by lazy {
            val runCommand = psiFile?.allParentMagicComments()
                    ?.value(DefaultMagicKeys.BIBTEXCOMPILER) ?: return@lazy null
            val compilerString = if (runCommand.contains(' ')) {
                runCommand.let { it.subSequence(0, it.indexOf(' ')) }.trim()
                        .toString()
            }
            else runCommand
            val compiler = BibliographyCompiler.valueOf(compilerString.toUpperCase())
            val compilerArguments = runCommand.removePrefix(compilerString)
                    .trim()
            Pair(compiler, compilerArguments)
        }

        val defaultCompiler = when {
            compilerFromMagicComment != null -> compilerFromMagicComment!!.first
            psiFile?.hasBibliography() == true -> BibliographyCompiler.BIBTEX
            psiFile?.usesBiber() == true -> BibliographyCompiler.BIBER
            else -> return // Do not auto-generate a bib run config when we can't detect bibtex
        }

        // On non-MiKTeX systems, override outputPath to disable the out/ directory by default for bibtex to work
        if (!latexDistribution.isMiktex()) {
            // Only if default, because the user could have changed it after creating the run config but before running
            if (isDefaultOutputPath() && mainFile != null) {
                outputPath = mainFile!!.parent
            }
        }

        // When chapterbib is used, every chapter has its own bibliography and needs its own run config
        val usesChapterbib = psiFile?.includedPackages()?.contains("chapterbib") == true

        if (!usesChapterbib) {
            addBibRunConfig(defaultCompiler, mainFile, compilerFromMagicComment?.second)
        }
        else if (psiFile != null) {
            val allBibliographyCommands = psiFile!!.commandsInFileSet().filter { it.name == "\\bibliography" }

            // We know that there can only be one bibliography per top-level \include,
            // however not all of them may contain a bibliography, and the ones
            // that do have one can have it in any included file
            psiFile!!.allCommands()
                    .filter { it.name == "\\include" }
                    .flatMap { command -> command.requiredParameters }
                    .forEach { filename ->
                        // Find all the files of this chapter, then check if any of the bibliography commands appears in a file in this chapter
                        val chapterMainFile = psiFile!!.findFile(filename)
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
     * Looks up the corresponding [VirtualFile] and sets [LatexRunConfiguration.mainFile].
     */
    fun setMainFile(mainFilePath: String) {
        val fileSystem = LocalFileSystem.getInstance()
        // Check if the file is valid and exists
        val mainFile = fileSystem.findFileByPath(mainFilePath)
        if (mainFile?.extension == "tex") {
            this.mainFile = mainFile
            return
        }
        else {
            // Maybe it is a relative path
            ProjectRootManager.getInstance(project).contentRoots.forEach {
                val file = it.findFileByRelativePath(mainFilePath)
                if (file?.extension == "tex") {
                    this.mainFile = file
                    return
                }
            }
        }

        this.mainFile = null
    }

    /**
     * Try to find the virtual file, as absolute path or relative to a content root.
     */
    fun findVirtualFileByPath(path: String): VirtualFile? {
        val fileSystem = LocalFileSystem.getInstance()

        val file = fileSystem.findFileByPath(path)
        if (file != null) {
            return file
        }
        else {
            // Maybe it is a relative path
            ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
                root.findFileByRelativePath(path)?.let { return it }
            }
        }

        return null
    }

    fun setDefaultCompiler() {
        compiler = LatexCompiler.PDFLATEX
    }

    fun setDefaultOutputFormat() {
        outputFormat = Format.PDF
    }

    fun setDefaultDistribution() {
        latexDistribution = LatexDistribution.defaultLatexDistribution
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder when used, or else the folder where the main file is, or null if there is no main file.
     */
    fun getAuxilDirectory(): VirtualFile? {
        val auxilDir = when {
            auxilPath != null && latexDistribution.isMiktex() -> auxilPath
            outputPath != null -> outputPath
            mainFile != null -> mainFile?.parent
            else -> null
        }
        return if (auxilDir?.path?.endsWith("/bin") == true) {
            mainFile?.parent
        }
        else {
            auxilDir
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
        val outputDir = if (outputPath != null) outputPath!!.path else mainFile?.parent?.path
        return "$outputDir/" + mainFile!!
                .nameWithoutExtension + "." + outputFormat.toString()
                .toLowerCase()
    }

    /**
     * Set [outputPath]
     */
    override fun setFileOutputPath(fileOutputPath: String) {
        if (fileOutputPath.endsWith("/bin")) {
            this.outputPath = getDefaultOutputPath()
        }
        else {
            this.outputPath = findVirtualFileByPath(fileOutputPath)
        }
    }

    /**
     * Assuming the main file is known, set a default output path if not already set.
     */
    fun setDefaultOutputPath() {
        if (outputPath != null || mainFile == null) return
        this.outputPath = getDefaultOutputPath()
    }

    private fun getDefaultOutputPath(): VirtualFile? {
        if (mainFile == null) return null
        var defaultOutputPath: VirtualFile? = null
        runReadAction {
            val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
            defaultOutputPath = LocalFileSystem.getInstance().findFileByPath(moduleRoot?.path + "/out")
        }
        return defaultOutputPath
    }

    /**
     * Whether the current output path is the default.
     */
    private fun isDefaultOutputPath() = getDefaultOutputPath() == outputPath

    /**
     * Assuming the main file is known, set a default auxil path if not already set.
     * Will be set to null if not used.
     */
    fun setDefaultAuxilPath() {
        // -aux-directory pdflatex flag only exists on MiKTeX, so disable auxil otherwise
        if (auxilPath != null || mainFile == null || !latexDistribution.isMiktex()) return
        val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
        this.auxilPath = LocalFileSystem.getInstance().findFileByPath(moduleRoot?.path + "/auxil")
    }

    /**
     * Set [auxilPath]
     */
    fun setFileAuxilPath(fileAuxilPath: String) {
        this.auxilPath = findVirtualFileByPath(fileAuxilPath)
    }

    /**
     * Whether an auxil or out directory is used, i.e. whether not both are set to the directory of the main file
     */
    fun usesAuxilOrOutDirectory(): Boolean {
        val usesAuxilDir = if (auxilPath == null) {
            false
        }
        else {
            auxilPath != mainFile?.parent
        }

        val usesOutDir = if (outputPath == null) {
            false
        }
        else {
            outputPath != mainFile?.parent
        }

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
                ", sumatraPath=" + sumatraPath +
                ", mainFile=" + mainFile +
                ", outputFormat=" + outputFormat +
                '}'.toString()
    }
}
