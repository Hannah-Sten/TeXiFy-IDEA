package nl.hannahsten.texifyidea.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.*
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
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
        private const val AUX_DIR = "aux-dir"
        private const val OUT_DIR = "out-dir"
        private const val COMPILE_TWICE = "compile-twice"
        private const val OUTPUT_FORMAT = "output-format"
        private const val HAS_BEEN_RUN = "has-been-run"
        private const val BIB_RUN_CONFIG = "bib-run-config"
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

    // Enable auxiliary directories by default on MiKTeX only
    var hasAuxiliaryDirectories = LatexDistribution.isMiktex
    var hasOutputDirectories = true
    var compileTwice = false
    var outputFormat: Format = Format.PDF
    private var bibRunConfigId = ""
    var isSkipBibtex = false

    /** Whether this run configuration is the last one in the chain of run configurations (e.g. latex, bibtex, latex, latex). */
    var isLastRunConfig = false

    // Whether the run configuration has already been run or not, since it has been created
    var hasBeenRun = false

    var bibRunConfig: RunnerAndConfigurationSettings?
        get() = RunManagerImpl.getInstanceImpl(project)
                .getConfigurationById(bibRunConfigId)
        set(bibRunConfig) {
            this.bibRunConfigId = bibRunConfig?.uniqueID ?: ""
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return LatexSettingsEditor(project)
    }

    @Throws(RuntimeConfigurationException::class)
    override fun checkConfiguration() {
        if (compiler == null || mainFile == null) {
            throw RuntimeConfigurationError(
                    "Run configuration is invalid.")
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
        this.mainFile = fileSystem.findFileByPath(filePath)

        // Read auxiliary directories.
        val auxDirBoolean = parent.getChildText(AUX_DIR)
        this.hasAuxiliaryDirectories = java.lang.Boolean.parseBoolean(auxDirBoolean)

        // Read output directories.
        val outDirBoolean = parent.getChildText(OUT_DIR)
        // This is null if the original run configuration did not contain the
        // option to disable the out directory, which should be enabled by default.
        if (outDirBoolean == null) {
            this.hasOutputDirectories = true
        }
        else {
            this.hasOutputDirectories = java.lang.Boolean.parseBoolean(outDirBoolean)
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

        // Write auxiliary directories.
        val auxDirElt = Element(AUX_DIR)
        auxDirElt.text = java.lang.Boolean.toString(hasAuxiliaryDirectories)
        parent.addContent(auxDirElt)

        // Write output directories.
        val outDirElt = Element(OUT_DIR)
        outDirElt.text = java.lang.Boolean.toString(hasOutputDirectories)
        parent.addContent(outDirElt)

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

        // On non-MiKTeX systems, disable the out/ directory by default for bibtex to work
        if (!LatexDistribution.isMiktex) {
            this.hasOutputDirectories = false
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
        this.mainFile = fileSystem.findFileByPath(mainFilePath)
    }

    fun setDefaultCompiler() {
        compiler = LatexCompiler.PDFLATEX
    }

    /**
     * Only enabled by default on MiKTeX, because -aux-directory is MikTeX only.
     */
    fun setDefaultAuxiliaryDirectories() {
        this.hasAuxiliaryDirectories = LatexDistribution.isMiktex
    }

    fun setDefaultOutputFormat() {
        outputFormat = Format.PDF
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

    override fun getOutputFilePath(): String {
        val folder: String = if (hasOutputDirectories) {
            ProjectRootManager.getInstance(project).fileIndex
                    .getContentRootForFile(mainFile!!)!!.path + "/out/"
        }
        else {
            mainFile!!.parent.path + "/"
        }

        return folder + mainFile!!
                .nameWithoutExtension + "." + outputFormat.toString()
                .toLowerCase()
    }

    override fun setFileOutputPath(fileOutputPath: String) {}

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
                ", bibWorkingDir=" + hasAuxiliaryDirectories +
                ", outputFormat=" + outputFormat +
                '}'.toString()
    }
}
