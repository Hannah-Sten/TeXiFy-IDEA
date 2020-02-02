package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.*
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latex.ui.LatexSettingsEditor
import nl.hannahsten.texifyidea.util.LatexDistribution
import nl.hannahsten.texifyidea.util.hasBibliography
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

    var mainFile: VirtualFile? = null
    // Save the psifile which can be used to check whether to create a bibliography based on which commands are in the psifile
    // This is not done when creating the template run configuration in order to delay the expensive bibtex check
    var psiFile: PsiFile? = null

    /** Path to the directory containing the output files. */
    var outputPath: VirtualFile? = null
    /** Path to the directory containing the auxiliary files. */
    var auxilPath: VirtualFile? = null

    var compileTwice = false
    var outputFormat: Format = Format.PDF

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig = false
    var isFirstRunConfig = true

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun = false

    /** Whether the pdf viewer is allowed to claim focus after compilation. */
    var allowFocusChange = true

    private var bibRunConfigId = ""
    var bibRunConfig: RunnerAndConfigurationSettings?
        get() = RunManagerImpl.getInstanceImpl(project)
                .getConfigurationById(bibRunConfigId)
        set(bibRunConfig) {
            this.bibRunConfigId = bibRunConfig?.uniqueID ?: ""
        }

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

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (compiler == null) {
            throw RuntimeConfigurationError(
                    "Run configuration is invalid: no compiler selected")
        }
        if (mainFile == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: no valid main LaTeX file selected")
        }
        // It is not possible to create the directory when it doesn't exist, because we do not know when the user is done editing (LatexSettingsEditor.applyEditorTo is called a lot of times)
        if (outputPath == null) {
            throw RuntimeConfigurationError("Run configuration is invalid: output path cannot be found")
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

        // Read main file.
        val fileSystem = LocalFileSystem.getInstance()
        val filePath = parent.getChildText(MAIN_FILE)
        val mainFile = fileSystem.findFileByPath(filePath)
        if (mainFile?.extension == "tex") {
            this.mainFile = mainFile
        }
        else {
            this.mainFile = null
        }

        // Read output path
        val outputPathString = parent.getChildText(OUTPUT_PATH)
        if (outputPathString != null) {
            this.outputPath = fileSystem.findFileByPath(outputPathString)
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
        val format = Format
                .byNameIgnoreCase(parent.getChildText(OUTPUT_FORMAT))
        this.outputFormat = format

        // Read whether the run config has been run
        val hasBeenRunString = parent.getChildText(HAS_BEEN_RUN)
        this.hasBeenRun = hasBeenRunString?.toBoolean() ?: false

        // Read bibliography run configuration
        val bibRunConfigElt = parent.getChildText(BIB_RUN_CONFIG)
        this.bibRunConfigId = bibRunConfigElt ?: ""

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

        // Write compiler.
        val compilerElt = Element(COMPILER)
        compilerElt.text = compiler?.name ?: ""
        parent.addContent(compilerElt)

        // Write compiler path.
        val compilerPathElt = Element(COMPILER_PATH)
        compilerPathElt.text = compilerPath ?: ""
        parent.addContent(compilerPathElt)

        // Write SumatraPDF path
        val sumatraPathElt = Element(SUMATRA_PATH)
        sumatraPathElt.text = sumatraPath ?: ""
        parent.addContent(sumatraPathElt)

        // Write pdf viewer command
        val viewerCommandElt = Element(VIEWER_COMMAND)
        viewerCommandElt.text = viewerCommand ?: ""
        parent.addContent(viewerCommandElt)

        // Write compiler arguments
        val compilerArgsElt = Element(COMPILER_ARGUMENTS)
        compilerArgsElt.text = this.compilerArguments ?: ""
        parent.addContent(compilerArgsElt)

        // Write main file.
        val mainFileElt = Element(MAIN_FILE)
        mainFileElt.text = mainFile?.path ?: ""
        parent.addContent(mainFileElt)

        // Write output path
        val outputPathElt = Element(OUTPUT_PATH)
        outputPathElt.text = outputPath?.path ?: ""
        parent.addContent(outputPathElt)

        // Write auxiliary path
        val auxilPathElt = Element(AUXIL_PATH)
        auxilPathElt.text = auxilPath?.path ?: ""
        parent.addContent(auxilPathElt)

        // Write whether to compile twice
        val compileTwiceElt = Element(COMPILE_TWICE)
        compileTwiceElt.text = compileTwice.toString()
        parent.addContent(compileTwiceElt)

        // Write output format.
        val outputFormatElt = Element(OUTPUT_FORMAT)
        outputFormatElt.text = outputFormat.name
        parent.addContent(outputFormatElt)

        // Write whether the run config has been run
        val hasBeenRunElt = Element(HAS_BEEN_RUN)
        hasBeenRunElt.text = hasBeenRun.toString()
        parent.addContent(hasBeenRunElt)

        // Write bibliography run configuration
        val bibRunConfigElt = Element(BIB_RUN_CONFIG)
        bibRunConfigElt.text = bibRunConfigId
        parent.addContent(bibRunConfigElt)

        // Write makeindex run configuration
        val makeindexRunConfigElt = Element(MAKEINDEX_RUN_CONFIG)
        makeindexRunConfigElt.text = makeindexRunConfigId
        parent.addContent(makeindexRunConfigElt)
    }

    /**
     * Generate a Bibtex run configuration, after trying to guess whether the user wants to use bibtex or biber as compiler.
     */
    internal fun generateBibRunConfig() {

        val defaultCompiler = when {
            psiFile?.hasBibliography() == true -> BibliographyCompiler.BIBTEX
            psiFile?.usesBiber() == true -> BibliographyCompiler.BIBER
            else -> return // Do not auto-generate a bib run config when we can't detect bibtex
        }

        // On non-MiKTeX systems, override outputPath to disable the out/ directory by default for bibtex to work
        if (!LatexDistribution.isMiktex) {
            // Only if default, because the user could have changed it after creating the run config but before running
            if (isDefaultOutputPath() && mainFile != null) {
                outputPath = mainFile!!.parent
            }
        }

        val runManager = RunManagerImpl.getInstanceImpl(project)

        val bibSettings = runManager.createConfiguration(
                "",
                LatexConfigurationFactory(BibtexRunConfigurationType())
        )

        val bibtexRunConfiguration = bibSettings.configuration as BibtexRunConfiguration

        bibtexRunConfiguration.compiler = defaultCompiler
        bibtexRunConfiguration.mainFile = mainFile
        bibtexRunConfiguration.setSuggestedName()

        runManager.addConfiguration(bibSettings)

        bibRunConfig = bibSettings
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
        }
        else {
            this.mainFile = null
        }
    }

    fun setDefaultCompiler() {
        compiler = LatexCompiler.PDFLATEX
    }

    fun setDefaultOutputFormat() {
        outputFormat = Format.PDF
    }

    /**
     * Find the directory where auxiliary files will be placed, depending on the run config settings.
     *
     * @return The auxil folder when MiKTeX used, or else the out folder when used, or else the folder where the main file is, or null if there is no main file.
     */
    fun getAuxilDirectory(): VirtualFile? {
        return when {
            auxilPath != null && LatexDistribution.isMiktex -> auxilPath
            outputPath != null -> outputPath
            mainFile != null -> mainFile?.parent
            else -> null
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
        this.outputPath = LocalFileSystem.getInstance().findFileByPath(fileOutputPath)
    }

    /**
     * Assuming the main file is known, set a default output path if not already set.
     */
    fun setDefaultOutputPath() {
        if (outputPath != null || mainFile == null) return
        this.outputPath = getDefaultOutputPath()
    }

    private fun getDefaultOutputPath(): VirtualFile? {
        val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
        return LocalFileSystem.getInstance().findFileByPath(moduleRoot?.path + "/out")
    }

    /**
     * Whether the current output path is the default.
     */
    fun isDefaultOutputPath() = getDefaultOutputPath() == outputPath

    /**
     * Assuming the main file is known, set a default auxil path if not already set.
     * Will be set to null if not used.
     */
    fun setDefaultAuxilPath() {
        // -aux-directory pdflatex flag only exists on MiKTeX, so disable auxil otherwise
        if (auxilPath != null || mainFile == null || !LatexDistribution.isMiktex) return
        val moduleRoot = ProjectRootManager.getInstance(project).fileIndex.getContentRootForFile(mainFile!!)
        this.auxilPath = LocalFileSystem.getInstance().findFileByPath(moduleRoot?.path + "/auxil")
    }

    /**
     * Set [auxilPath]
     */
    fun setFileAuxilPath(fileAuxilPath: String) {
        this.auxilPath = LocalFileSystem.getInstance().findFileByPath(fileAuxilPath)
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
