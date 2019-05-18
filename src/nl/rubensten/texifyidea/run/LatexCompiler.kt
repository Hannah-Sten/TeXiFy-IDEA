package nl.rubensten.texifyidea.run

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.util.LatexDistribution
import nl.rubensten.texifyidea.util.splitWhitespace

/**
 * @author Ruben Schellekens, Sten Wessel
 */
enum class LatexCompiler(private val displayName: String, val executableName: String) {

    PDFLATEX("pdfLaTeX", "pdflatex") {

        override fun createCommand(runConfig: LatexRunConfiguration, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "pdflatex")

            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // -output-directory also exists on non-Windows systems
            if (runConfig.hasOutputDirectories()) {
                command.add("-output-directory=" + moduleRoot.path + "/out")
            }

            // -aux-directory only exists on MikTeX
            if (runConfig.hasAuxiliaryDirectories() && LatexDistribution.isMiktex) {
                command.add("-aux-directory=" + moduleRoot.path + "/auxil")
            }

            // Prepend root paths to the input search path
            if (LatexDistribution.isMiktex) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    LUALATEX("LuaLaTeX", "lualatex") {

        override fun createCommand(runConfig: LatexRunConfiguration, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "lualatex")

            // Some commands are the same as for pdflatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // -output-directory also exists on non-Windows systems
            if (runConfig.hasOutputDirectories()) {
                command.add("-output-directory=${moduleRoot.path}/out")
            }

            // Note that lualatex has no -aux-directory
            return command
        }
    },

    LATEXMK("Latexmk", "latexmk") {

        override fun createCommand(runConfig: LatexRunConfiguration, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "latexmk")

            // Adding the -pdf flag makes latexmk run with pdflatex, which is definitely preferred over running with just latex
            command.add("-pdf")
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
            command.add("-output-format=${runConfig.outputFormat.name.toLowerCase()}")

            // -output-directory also exists on non-Windows systems
            if (runConfig.hasOutputDirectories()) {
                command.add("-output-directory=${moduleRoot.path}/out")
            }

            // -aux-directory only exists on MikTeX
            if (runConfig.hasAuxiliaryDirectories() && LatexDistribution.isMiktex) {
                command.add("-aux-directory=${moduleRoot.path}/auxil")
            }

            // -include-directory does not work with latexmk
            return command
        }
    },

    XELATEX("XeLaTeX", "xelatex") {

        override fun createCommand(runConfig: LatexRunConfiguration, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "xelatex")

            // As usual, available command line options can be viewed with xelatex --help
            // On TeX Live, installing collection-xetex should be sufficient to get xelatex
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")

            val outputFormatName = runConfig.outputFormat.name.toLowerCase()
            if (outputFormatName == "dvi") {
                command.add("-no-pdf") // Generates XDV output instead of PDF
            }

            if (runConfig.hasOutputDirectories()) {
                command.add("-output-directory=" + moduleRoot.path + "/out")
            }

            // -aux-directory only exists on MikTeX
            if (runConfig.hasAuxiliaryDirectories() && LatexDistribution.isMiktex) {
                command.add("-aux-directory=" + moduleRoot.path + "/auxil")
            }

            // Prepend root paths to the input search path
            if (LatexDistribution.isMiktex) {
                moduleRoots.forEach {
                    command.add("-include-directory=${it.path}")
                }
            }

            return command
        }
    },

    TEXLIVEONFLY("Texliveonfly", "texliveonfly") {

        override fun createCommand(runConfig: LatexRunConfiguration, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: "texliveonfly")

            // texliveonfly is a Python script which calls other compilers (by default pdflatex), main feature is downloading packages automatically
            // commands can be passed to those compilers with the arguments flag, however apparently IntelliJ cannot handle quotes so we cannot pass multiple arguments to pdflatex.
            // Fortunately, -synctex=1 and -interaction=nonstopmode are on by default in texliveonfly
            // Since adding one will work without any quotes, we choose the output directory.
            if (runConfig.hasOutputDirectories()) {
                command.add("--arguments=--output-directory=${moduleRoot.path}/out")
            }

            return command
        }
    };

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
        val moduleRoot = fileIndex.getContentRootForFile(runConfig.mainFile) ?: return null
        val moduleRoots = rootManager.contentSourceRoots

        val command = createCommand(runConfig, moduleRoot, moduleRoots)

        // Custom compiler arguments specified by the user
        runConfig.compilerArguments?.let { arguments ->
            arguments.splitWhitespace()
                    .dropLastWhile { it.isEmpty() }
                    .forEach { command.add(it) }
        }

        command.add(mainFile.name)

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
            moduleRoot: VirtualFile,
            moduleRoots: Array<VirtualFile>
    ): MutableList<String> = error("Not implemented for $this")

    override fun toString() = this.displayName

    /**
     * @author Ruben Schellekens
     */
    enum class Format {

        PDF,
        DVI;

        companion object {

            fun byNameIgnoreCase(name: String?): Format? {
                for (format in values()) {
                    if (format.name.equals(name!!, ignoreCase = true)) {
                        return format
                    }
                }

                return null
            }
        }
    }
}
