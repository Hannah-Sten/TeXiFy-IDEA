package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.SupportedCompiler
import nl.hannahsten.texifyidea.run.step.LatexCompileStep
import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import nl.hannahsten.texifyidea.util.magic.CompilerMagic
import nl.hannahsten.texifyidea.util.runCommand


/**
 *
 * When adding compilers as a subclass, be sure to add it to [CompilerMagic.latexCompilerByExecutableName].
 */
abstract class SupportedLatexCompiler(
    override val displayName: String,
    override val executableName: String
) : LatexCompiler(), SupportedCompiler<LatexCompileStep> {

    /**
     * Convert Windows paths to WSL paths.
     */
    private fun String.toPath(runConfig: LatexRunConfiguration): String =
        if (runConfig.options.getLatexDistribution(runConfig.project) == LatexDistributionType.WSL_TEXLIVE) {
            "wsl wslpath -a '$this'".runCommand() ?: this
        }
        else this

    /**
     * Get the execution command for the latex compiler.
     */
    override fun getCommand(step: LatexCompileStep): List<String>? {
        val runConfig = step.configuration
        val project = runConfig.project

        val rootManager = ProjectRootManager.getInstance(project)
        val fileIndex = rootManager.fileIndex
        val mainFile = runConfig.options.mainFile.resolve() ?: return null
        val moduleRoot = fileIndex.getContentRootForFile(mainFile)
        // For now we disable module roots with Docker
        // Could be improved by mounting them to the right directory
        val moduleRoots = if (runConfig.options.getLatexDistribution(runConfig.project) != LatexDistributionType.DOCKER_MIKTEX) {
            rootManager.contentSourceRoots
        }
        else {
            emptyArray()
        }

        // If we used /miktex/work/out, an out directory would appear in the src folder on the host system
        val dockerOutputDir = "/miktex/out"
        val dockerAuxilDir = "/miktex/auxil"
        val outputPath = if (runConfig.options.getLatexDistribution(runConfig.project) != LatexDistributionType.DOCKER_MIKTEX) {
            runConfig.options.outputPath.getOrCreateOutputPath(runConfig.options.mainFile.resolve(), project)?.path?.toPath(runConfig)
        }
        else {
            dockerOutputDir
        }

        // Make sure the output path is valid
        if (!runConfig.options.getLatexDistribution(runConfig.project).isMiktex()) {
            runConfig.options.outputPath.updateOutputSubDirs(mainFile, project)
        }

        val auxilPath = if (runConfig.options.getLatexDistribution(runConfig.project) != LatexDistributionType.DOCKER_MIKTEX) {
            runConfig.options.auxilPath.getOrCreateOutputPath(runConfig.options.mainFile.resolve(), project)?.path?.toPath(runConfig)
        }
        else {
            dockerAuxilDir
        }

        var command = createCommand(
            runConfig,
            auxilPath,
            outputPath,
            moduleRoot,
            moduleRoots
        )

        if (runConfig.options.getLatexDistribution(runConfig.project) == LatexDistributionType.WSL_TEXLIVE) {
            command = mutableListOf("bash", "-ic", GeneralCommandLine(command).commandLineString)
        }

        if (runConfig.options.getLatexDistribution(runConfig.project) == LatexDistributionType.DOCKER_MIKTEX) {
            createDockerCommand(runConfig, dockerAuxilDir, dockerOutputDir, mainFile, command)
        }

        // Custom compiler arguments specified by the user
        runConfig.options.compilerArguments?.let { arguments ->
            ParametersListUtil.parse(arguments)
                .forEach { command.add(it) }
        }

        if (runConfig.options.getLatexDistribution(runConfig.project) == LatexDistributionType.WSL_TEXLIVE) {
            command[command.size - 1] = command.last() + " ${mainFile.path.toPath(runConfig)}"
        }
        else {
            command.add(mainFile.name)
        }

        return command
    }

    @Suppress("SameParameterValue")
    private fun createDockerCommand(runConfig: LatexRunConfiguration, dockerAuxilDir: String, dockerOutputDir: String, mainFile: VirtualFile, command: MutableList<String>) {
        // See https://hub.docker.com/r/miktex/miktex
        "docker volume create --name miktex".runCommand()

        val parameterList = mutableListOf(
            "docker", // Could be improved by getting executable name based on SDK
            "run",
            "--rm",
            "-v",
            "miktex:/miktex/.miktex",
            "-v",
            "${mainFile.parent.path}:/miktex/work"
        )

        // Avoid mounting the mainfile parent also to /miktex/work/out,
        // because there may be a good reason to make the output directory the same as the source directory
        if (!runConfig.options.outputPath.isMainFileParent(runConfig.options.mainFile.resolve(), runConfig.project)) {
            val outputPath = runConfig.options.outputPath.getOrCreateOutputPath(runConfig.options.mainFile.resolve(), runConfig.project)
            parameterList.addAll(listOf("-v", "${outputPath?.path}:$dockerOutputDir"))
        }

        val auxilPath = runConfig.options.auxilPath.getOrCreateOutputPath(runConfig.options.mainFile.resolve(), runConfig.project)
        if (auxilPath != mainFile.parent) {
            parameterList.addAll(listOf("-v", "${auxilPath?.path}:$dockerAuxilDir"))
        }

        parameterList.add("docker.pkg.github.com/hannah-sten/texify-idea/miktex:latest")

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
    abstract fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String>

    override fun toString() = this.displayName

    companion object {

        fun byExecutableName(exe: String) = CompilerMagic.latexCompilerByExecutableName[exe]
    }
}
