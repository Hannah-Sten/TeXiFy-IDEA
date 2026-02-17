package nl.hannahsten.texifyidea.run.compiler

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.DockerSdk
import nl.hannahsten.texifyidea.settings.sdk.DockerSdkAdditionalData
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.files.hasTectonicTomlFile
import nl.hannahsten.texifyidea.util.runCommand
import java.util.*

/**
 * @author Hannah Schellekens, Sten Wessel
 */
@Suppress("DuplicatedCode")
enum class LatexCompiler(private val displayName: String, val executableName: String) {

    PDFLATEX("pdfLaTeX", "pdflatex") {

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            // For now only support custom executable for TeX Live
            // At least avoids prepending a full path to a supposed TeX Live executable when in fact it will be prepended by a docker command
            val executable = LatexSdkUtil.getExecutableName(executableName, runConfig.project, runConfig.getLatexSdk(), runConfig.getLatexDistributionType())
            val command = mutableListOf(runConfig.compilerPath ?: executable)

            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.lowercase(Locale.getDefault())}")

            command.add("-output-directory=$outputPath")

            // -aux-directory only exists on MiKTeX
            if (auxilPath != null && runConfig.getLatexDistributionType().isMiktex(project = runConfig.project)) {
                command.add("-aux-directory=$auxilPath")
            }

            // Prepend root paths to the input search path
            if (runConfig.getLatexDistributionType().isMiktex(runConfig.project)) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    LUALATEX("LuaLaTeX", "lualatex") {

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(
                runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                    executableName,
                    runConfig.project,
                    runConfig.getLatexSdk(),
                    runConfig.getLatexDistributionType()
                )
            )

            // Some commands are the same as for pdflatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.lowercase(Locale.getDefault())}")

            command.add("-output-directory=$outputPath")

            // Note that lualatex has no -aux-directory
            return command
        }
    },

    LATEXMK("Latexmk", "latexmk") {

        override val includesBibtex = true

        override val includesMakeindex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.DEFAULT, Format.PDF, Format.DVI)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(
                runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                    executableName,
                    runConfig.project,
                    runConfig.getLatexSdk(),
                    runConfig.getLatexDistributionType()
                )
            )

            val isLatexmkRcFilePresent = LatexmkRcFileFinder.isLatexmkRcFilePresent(runConfig)

            // If it is present, assume that it will handle everything (command line options would overwrite latexmkrc options)
            if (!isLatexmkRcFilePresent) {
                // Adding the -pdf flag makes latexmk run with pdflatex, which is definitely preferred over running with just latex
                command.add("-pdf")
                command.add("-file-line-error")
                command.add("-interaction=nonstopmode")
                command.add("-synctex=1")
            }

            if (runConfig.outputFormat != Format.DEFAULT) {
                command.add("-output-format=${runConfig.outputFormat.name.lowercase(Locale.getDefault())}")
            }

            command.add("-output-directory=$outputPath")

            if (auxilPath != null && runConfig.getLatexDistributionType().isMiktex(runConfig.project)) {
                command.add("-aux-directory=$auxilPath")
            }

            // -include-directory does not work with latexmk
            return command
        }
    },

    XELATEX("XeLaTeX", "xelatex") {

        override val outputFormats = arrayOf(Format.PDF, Format.XDV)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(
                runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                    executableName,
                    runConfig.project,
                    runConfig.getLatexSdk(),
                    runConfig.getLatexDistributionType()
                )
            )

            // As usual, available command line options can be viewed with xelatex --help
            // On TeX Live, installing collection-xetex should be sufficient to get xelatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")

            if (runConfig.outputFormat == Format.XDV) {
                command.add("-no-pdf")
            }

            command.add("-output-directory=$outputPath")

            if (auxilPath != null && runConfig.getLatexDistributionType().isMiktex(runConfig.project)) {
                command.add("-aux-directory=$auxilPath")
            }

            // Prepend root paths to the input search path
            if (runConfig.getLatexDistributionType().isMiktex(runConfig.project)) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    TEXLIVEONFLY("Texliveonfly", "texliveonfly") {

        override val outputFormats = arrayOf(Format.PDF)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(
                runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                    executableName,
                    runConfig.project,
                    runConfig.getLatexSdk(),
                    runConfig.getLatexDistributionType()
                )
            )

            // texliveonfly is a Python script which calls other compilers (by default pdflatex), main feature is downloading packages automatically
            // commands can be passed to those compilers with the arguments flag, however apparently IntelliJ cannot handle quotes so we cannot pass multiple arguments to pdflatex.
            // Fortunately, -synctex=1 and -interaction=nonstopmode are on by default in texliveonfly
            // Since adding one will work without any quotes, we choose the output directory.
            command.add("--arguments=--output-directory=$outputPath")

            return command
        }
    },

    TECTONIC("Tectonic", "tectonic") {

        override val includesBibtex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.PDF, Format.HTML, Format.XDV, Format.AUX)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: executableName)

            // The available command line arguments can be found at https://github.com/tectonic-typesetting/tectonic/blob/d7a8497c90deb08b5e5792a11d6e8b082f53bbb7/src/bin/tectonic.rs#L158
            // The V2 CLI uses a toml file and should not have arguments
            if (runConfig.mainFile?.hasTectonicTomlFile() != true) {
                command.add("--synctex")

                command.add("--outfmt=${runConfig.outputFormat.name.lowercase(Locale.getDefault())}")

                command.add("--outdir=$outputPath")
            }
            else {
                command.add("-X")
                command.add("build")
            }

            return command
        }
    },

    ARARA("Arara", "arara") {

        override val includesBibtex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.PDF)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            // Arara handles everything as configured by magic comments in the file.
            // We cannot use --verbose because it relies on user input
            return mutableListOf(runConfig.compilerPath ?: executableName)
        }
    },
    ;

    /**
     * Get the execution command for the latex compiler.
     *
     * @param runConfig
     *          The run configuration object to get the command for.
     * @param project
     *          The current project.
     */
    fun getCommand(runConfig: LatexRunConfiguration, project: Project): List<String>? {
        val mainFile = runConfig.mainFile ?: return null
        // Getting the content root is an expensive operation (See WorkspaceFileIndexDataImpl#ensureIsUpToDate), and since it probably won't change often we reuse a cached value
        val moduleRoot = runConfig.outputPath.getMainFileContentRoot(runConfig.mainFile)
        // For now we disable module roots with Docker
        // Could be improved by mounting them to the right directory
        val moduleRoots = if (runConfig.getLatexDistributionType().isDocker()) {
            emptyArray()
        }
        else {
            // If the project contains other non-LaTeX modules ignore them, we are only interested in source roots relevant to this file
            // Limit the number of roots, in case the user has hundreds of roots it can exceed the maximum command line length on Windows
            val allRoots = ModuleUtil.findModuleForFile(mainFile, runConfig.project)?.rootManager?.sourceRoots ?: emptyArray()
            var totalLength = 0
            val roots = mutableListOf<VirtualFile>()
            for (root in allRoots) {
                totalLength += root.toString().length + " -include-directory=".length
                // See LatexCommandLineState
                if (totalLength > 10_000) break
                roots.add(root)
            }
            roots.toTypedArray()
        }

        val outputPath = if (runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_MIKTEX) {
            // If we used /miktex/work/out, an out directory would appear in the src folder on the host system
            "/miktex/out"
        }
        else if (runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_TEXLIVE) {
            "/out"
        }
        else {
            (runConfig.outputPath.getAndCreatePath() ?: mainFile.parent).path.toWslPathIfNeeded(runConfig.getLatexDistributionType())
        }

        val auxilPath = if (runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_MIKTEX) {
            "/miktex/auxil"
        }
        else if (runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_TEXLIVE) {
            null
        }
        else {
            runConfig.auxilPath.getAndCreatePath()?.path?.toWslPathIfNeeded(runConfig.getLatexDistributionType())
        }

        val command = createCommand(
            runConfig,
            auxilPath,
            outputPath,
            moduleRoot,
            moduleRoots
        )

        if (runConfig.getLatexDistributionType() == LatexDistributionType.WSL_TEXLIVE) {
            var wslCommand = GeneralCommandLine(command).commandLineString

            // Custom compiler arguments specified by the user
            runConfig.compilerArguments?.let { arguments ->
                ParametersListUtil.parse(arguments)
                    .forEach { wslCommand += " $it" }
            }

            wslCommand += " ${mainFile.path.toWslPathIfNeeded(runConfig.getLatexDistributionType())}"

            return mutableListOf(*SystemEnvironment.wslCommand, wslCommand)
        }

        if (runConfig.getLatexDistributionType().isDocker()) {
            createDockerCommand(runConfig, auxilPath, outputPath, mainFile, command)
        }

        // Custom compiler arguments specified by the user
        runConfig.compilerArguments?.let { arguments ->
            ParametersListUtil.parse(arguments)
                .forEach { command.add(it) }
        }

        // Run some code before running the document
        if (runConfig.beforeRunCommand?.isNotBlank() == true) {
            if (runConfig.compiler == LATEXMK) {
                // latexmk has its own flag to do this
                command.add("-usepretex=" + runConfig.beforeRunCommand)
                command.add(mainFile.name)
            }
            else {
                // pdflatex, lualatex and xelatex support this by being able to run on a given string as if it was in a file
                command.add(runConfig.beforeRunCommand + " \\input{${mainFile.name}}")
            }
        }
        else if (runConfig.compiler != TECTONIC || runConfig.mainFile?.hasTectonicTomlFile() != true) {
            if (runConfig.hasDefaultWorkingDirectory()) {
                command.add(mainFile.name)
            }
            else {
                command.add(mainFile.path)
            }
        }

        return command
    }

    @Suppress("SameParameterValue")
    private fun createDockerCommand(runConfig: LatexRunConfiguration, dockerAuxilDir: String?, dockerOutputDir: String?, mainFile: VirtualFile, command: MutableList<String>) {
        val isMiktex = runConfig.getLatexDistributionType() == LatexDistributionType.DOCKER_MIKTEX

        if (isMiktex) {
            // See https://hub.docker.com/r/miktex/miktex
            "docker volume create --name miktex".runCommand()
        }

        // Find the sdk corresponding to the type the user has selected in the run config
        val sdk = LatexSdkUtil.getAllLatexSdks().firstOrNull { it.sdkType is DockerSdk }

        val parameterList = mutableListOf(
            if (sdk == null) "docker" else (sdk.sdkType as DockerSdk).getExecutableName("docker", sdk.homePath!!),
            "run",
            "--rm",
        )

        parameterList += if (isMiktex) {
            listOf(
                "-v",
                "miktex:/miktex/.miktex",
                "-v",
                "${mainFile.parent.path}:/miktex/work"
            )
        }
        else {
            listOf(
                "-v",
                "${mainFile.parent.path}:/workdir"
            )
        }

        if (dockerOutputDir != null) {
            // Avoid mounting the mainfile parent also to /miktex/work/out,
            // because there may be a good reason to make the output directory the same as the source directory
            val outPath = runConfig.outputPath.getAndCreatePath()
            if (outPath?.path != null && outPath != mainFile.parent) {
                parameterList.addAll(listOf("-v", "${outPath.path}:$dockerOutputDir"))
            }
        }

        if (dockerAuxilDir != null) {
            val auxilPath = runConfig.auxilPath.getAndCreatePath()
            if (auxilPath?.path != null && auxilPath != mainFile.parent) {
                parameterList.addAll(listOf("-v", "${auxilPath.path}:$dockerAuxilDir"))
            }
        }

        val sdkImage = (sdk?.sdkAdditionalData as? DockerSdkAdditionalData)?.imageName
        val default = if (isMiktex) "miktex/miktex:latest" else "texlive/texlive:latest"
        parameterList.add(sdkImage ?: default)

        command.addAll(0, parameterList)
    }

    /**
     * Create the command to execute to use the compiler.
     *
     * @param runConfig LaTeX run configuration which initiated the action of creating this command.
     * @param moduleRoot Module root.
     * @param moduleRoots List of source roots.
     *
     * @return The command to be executed.
     */
    protected open fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> = error("Not implemented for $this")

    /**
     * Whether the compiler includes running bibtex/biber.
     */
    open val includesBibtex = false

    /**
     * Whether the compiler includes running index programs.
     */
    open val includesMakeindex = false

    /**
     * Whether the compiler automatically determines the number of compiles needed.
     */
    open val handlesNumberOfCompiles = false

    /**
     * List of output formats supported by this compiler.
     */
    open val outputFormats: Array<Format> = arrayOf(Format.PDF, Format.DVI)

    override fun toString() = this.displayName

    companion object {

        fun byExecutableName(exe: String): LatexCompiler = entries.firstOrNull {
            it.executableName.equals(exe, true)
        } ?: PDFLATEX

        /**
         * Convert Windows paths to WSL paths.
         */
        fun String.toWslPathIfNeeded(distributionType: LatexDistributionType): String =
            if (distributionType == LatexDistributionType.WSL_TEXLIVE) {
                runCommand("wsl", "wslpath", "-a", this) ?: this
            }
            else this
    }

    /**
     * @author Hannah Schellekens
     */
    enum class Format {

        DEFAULT, // Means: don't overwite the default, e.g. a default from the latexmkrc, i.e. don't add any command line parameters
        PDF,
        DVI,
        HTML,
        XDV,
        AUX;

        companion object {

            fun byNameIgnoreCase(name: String?): Format = entries.firstOrNull {
                it.name.equals(name, ignoreCase = true)
            } ?: PDF
        }
    }
}
