package nl.hannahsten.texifyidea.run.compiler

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.runCommand
import nl.hannahsten.texifyidea.util.splitWhitespace

/**
 * @author Hannah Schellekens, Sten Wessel
 */
@Suppress("unused")
enum class LatexCompiler(private val displayName: String, val executableName: String) {

    PDFLATEX("pdfLaTeX", "pdflatex") {

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "pdflatex")

            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // Output directory with Dockerized MiKTeX is not yet supported
            if (outputPath != null && runConfig.latexDistribution != LatexDistributionType.DOCKER_MIKTEX) {
                command.add("-output-directory=$outputPath")
            }

            // -aux-directory only exists on MiKTeX
            if (auxilPath != null && runConfig.latexDistribution.isMiktex()) {
                command.add("-aux-directory=$auxilPath")
            }

            // Prepend root paths to the input search path
            if (runConfig.latexDistribution.isMiktex()) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    LUALATEX("LuaLaTeX", "lualatex") {

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "lualatex")

            // Some commands are the same as for pdflatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // Output directory with Dockerized MiKTeX is not yet supported
            if (outputPath != null && runConfig.latexDistribution != LatexDistributionType.DOCKER_MIKTEX) {
                command.add("-output-directory=$outputPath")
            }

            // Note that lualatex has no -aux-directory
            return command
        }
    },

    LATEXMK("Latexmk", "latexmk") {

        override val includesBibtex = true

        override val includesMakeindex = true

        override val handlesNumberOfCompiles = true

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "latexmk")

            // Adding the -pdf flag makes latexmk run with pdflatex, which is definitely preferred over running with just latex
            command.add("-pdf")
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // Output directory with Dockerized MiKTeX is not yet supported
            if (outputPath != null && runConfig.latexDistribution != LatexDistributionType.DOCKER_MIKTEX) {
                command.add("-output-directory=$outputPath")
            }

            if (auxilPath != null && runConfig.latexDistribution.isMiktex()) {
                command.add("-aux-directory=$auxilPath")
            }

            // -include-directory does not work with latexmk
            return command
        }
    },

    XELATEX("XeLaTeX", "xelatex") {

        override val outputFormats = arrayOf(Format.PDF, Format.XDV)

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "xelatex")

            // As usual, available command line options can be viewed with xelatex --help
            // On TeX Live, installing collection-xetex should be sufficient to get xelatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")

            if (runConfig.outputFormat == Format.XDV) {
                command.add("-no-pdf")
            }

            // Output directory with Dockerized MiKTeX is not yet supported
            if (outputPath != null && runConfig.latexDistribution != LatexDistributionType.DOCKER_MIKTEX) {
                command.add("-output-directory=$outputPath")
            }

            if (auxilPath != null && runConfig.latexDistribution.isMiktex()) {
                command.add("-aux-directory=$auxilPath")
            }

            // Prepend root paths to the input search path
            if (runConfig.latexDistribution.isMiktex()) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    TEXLIVEONFLY("Texliveonfly", "texliveonfly") {

        override val outputFormats = arrayOf(Format.PDF)

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "texliveonfly")

            // texliveonfly is a Python script which calls other compilers (by default pdflatex), main feature is downloading packages automatically
            // commands can be passed to those compilers with the arguments flag, however apparently IntelliJ cannot handle quotes so we cannot pass multiple arguments to pdflatex.
            // Fortunately, -synctex=1 and -interaction=nonstopmode are on by default in texliveonfly
            // Since adding one will work without any quotes, we choose the output directory.
            if (outputPath != null) {
                command.add("--arguments=--output-directory=$outputPath")
            }

            return command
        }
    },

    TECTONIC("Tectonic", "tectonic") {

        override val includesBibtex = true

        override val handlesNumberOfCompiles = true

        override val outputFormats = arrayOf(Format.PDF, Format.HTML, Format.XDV, Format.AUX)

        override fun createCommand(runConfig: LatexRunConfiguration, auxilPath: String?, outputPath: String?, moduleRoot: VirtualFile?, moduleRoots: Array<VirtualFile>): MutableList<String> {

            // The available command line arguments can be found at https://github.com/tectonic-typesetting/tectonic/blob/d7a8497c90deb08b5e5792a11d6e8b082f53bbb7/src/bin/tectonic.rs#L158
            val command = mutableListOf(runConfig.compilerPath ?: "tectonic")

            command.add("--synctex")

            command.add("--outfmt=${runConfig.outputFormat.name.toLowerCase()}")

            if (outputPath != null) {
                command.add("--outdir=$outputPath")
            }

            return command
        }
    };

    /**
     * Convert Windows paths to WSL paths.
     */
    private fun String.toPath(runConfig: LatexRunConfiguration): String = if (runConfig.latexDistribution == LatexDistributionType.WSL_TEXLIVE) { "wsl wslpath -a '$this'".runCommand() ?: this } else this

    /**
     * Get the execution command for the latex compiler.
     *
     * @param runConfig
     *          The run configuration object to get the command for.
     * @param project
     *          The current project.
     */
    fun getCommand(runConfig: LatexRunConfiguration, project: Project): List<String>? {
        val rootManager = ProjectRootManager.getInstance(project)
        val fileIndex = rootManager.fileIndex
        val mainFile = runConfig.mainFile ?: return null
        val moduleRoot = fileIndex.getContentRootForFile(mainFile)
        val moduleRoots = rootManager.contentSourceRoots

        var command = createCommand(runConfig, runConfig.auxilPath?.path?.toPath(runConfig), runConfig.outputPath?.path?.toPath(runConfig), moduleRoot, moduleRoots)

        if (runConfig.latexDistribution == LatexDistributionType.WSL_TEXLIVE) {
            command = mutableListOf("bash", "-ic", GeneralCommandLine(command).commandLineString)
        }

        if (runConfig.latexDistribution == LatexDistributionType.DOCKER_MIKTEX) {
            // See https://hub.docker.com/r/miktex/miktex
            "docker volume create --name miktex".runCommand()

            val parameterList = mutableListOf("docker", "run", "--rm", "-v", "miktex:/miktex/.miktex", "-v", "${mainFile.parent.path}:/miktex/work")

            // Avoid mounting the mainfile parent also to /miktex/work/out,
            // because there may be a good reason to make the output directory the same as the source directory
            if(runConfig.outputPath != mainFile.parent) {
                parameterList.addAll(listOf("-v", "${runConfig.outputPath?.path}:/miktex/work/out"))
                command.add("-output-directory=/miktex/work/out")
            }

            parameterList.add("docker.pkg.github.com/hannah-sten/texify-idea/miktex:latest")

            command.addAll(0, parameterList)
        }

        // Custom compiler arguments specified by the user
        runConfig.compilerArguments?.let { arguments ->
            arguments.splitWhitespace()
                    .dropLastWhile { it.isEmpty() }
                    .forEach { command.add(it) }
        }

        if (runConfig.latexDistribution == LatexDistributionType.WSL_TEXLIVE) {
            command[command.size - 1] = command.last() + " ${mainFile.path.toPath(runConfig)}"
        }
        else {
            command.add(mainFile.name)
        }

        return command
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
            outputPath: String?,
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

    /**
     * @author Hannah Schellekens
     */
    enum class Format {

        PDF,
        DVI,
        HTML,
        XDV,
        AUX;

        companion object {

            fun byNameIgnoreCase(name: String?): Format {
                if (name == null) return PDF

                for (format in values()) {
                    if (format.name.equals(name, ignoreCase = true)) {
                        return format
                    }
                }

                return PDF
            }
        }
    }
}
