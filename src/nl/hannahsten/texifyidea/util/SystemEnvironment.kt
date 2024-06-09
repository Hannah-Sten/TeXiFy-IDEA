package nl.hannahsten.texifyidea.util

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.process.ProcessNotCreatedException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.files.*
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

/**
 * Information about the system other than the LatexDistribution or the OS.
 */
class SystemEnvironment {

    companion object {

        val inkscapeMajorVersion: Int by lazy {
            "inkscape --version".runCommand()
                ?.split(" ")?.getOrNull(1)
                ?.split(".")?.firstOrNull()
                ?.toInt() ?: 0
        }

        val isInkscapeInstalledAsSnap: Boolean by lazy {
            "snap list".runCommand()?.contains("inkscape") == true
        }

        /** Cache for [isAvailable]. */
        private var availabilityCache = mutableMapOf<String, Boolean>()

        /**
         * Check if [command] is available as a system command.
         */
        fun isAvailable(command: String): Boolean {
            // Not thread-safe, but don't think that's a problem here
            availabilityCache.getOrDefault(command, null)?.let { return it }

            // Has to be run with bash because command is a shell command
            val isAvailable = if (SystemInfo.isUnix) {
                val (_, exitCode) = runCommandWithExitCode("bash", "-c", "command -v $command")
                exitCode == 0
            }
            else {
                "where $command".runCommandWithExitCode().second == 0
            }
            availabilityCache[command] = isAvailable
            return isAvailable
        }

        // Assumes version will be given in the format GNOME Document Viewer 3.34.2
        val evinceVersion: DefaultArtifactVersion by lazy {
            DefaultArtifactVersion("evince --version".runCommand()?.split(" ")?.lastOrNull() ?: "")
        }

        val texinputs by lazy {
            runCommand("kpsewhich", "--expand-var", "'\$TEXINPUTS'")
        }
    }
}

/**
 * Run a command in the terminal.
 *
 * @return The output of the command or null if an exception was thrown.
 */
fun runCommand(vararg commands: String, workingDirectory: File? = null): String? {
    return runCommandWithExitCode(*commands, workingDirectory = workingDirectory).first
}

/**
 * See [runCommand], but also returns exit code.
 *
 * @param returnExceptionMessage Whether to return exception messages if exceptions are thrown.
 * @param nonBlocking If true, the function will not block waiting for output
 * @param inputString If provided, this will be written to the process outputStream before starting the process.
 */
fun runCommandWithExitCode(vararg commands: String, workingDirectory: File? = null, timeout: Long = 3, returnExceptionMessage: Boolean = false, nonBlocking: Boolean = false, inputString: String = ""): Pair<String?, Int> {
    Log.debug("isEDT=${SwingUtilities.isEventDispatchThread()} Executing in ${workingDirectory ?: "current working directory"} ${GeneralCommandLine(*commands).commandLineString}")
    return try {
        val proc = GeneralCommandLine(*commands)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .createProcess()

        if (inputString.isNotBlank()) {
            proc.outputStream.bufferedWriter().apply {
                write(inputString)
                close()
            }
        }

        if (proc.waitFor(timeout, TimeUnit.SECONDS)) {
            val output = readInputStream(nonBlocking, proc)
            Log.debug("${commands.firstOrNull()} exited with ${proc.exitValue()} ${output.take(100)}")
            return Pair(output, proc.exitValue())
        }
        else {
            var output = ""
            // If the program has timed out, something is stuck so we are not going to wait until it prints its stdout/stderr, we just check if ready and otherwise are out of luck
            if (proc.inputStream.bufferedReader().ready()) {
                output += proc.inputStream.bufferedReader().readText().trim()
            }
            if (proc.errorStream.bufferedReader().ready()) {
                output += proc.errorStream.bufferedReader().readText().trim()
            }
            proc.destroy()
            proc.waitFor()
            Log.debug("${commands.firstOrNull()} exited ${proc.exitValue()} with timeout")
            Pair(output, proc.exitValue())
        }
    }
    catch (e: IOException) {
        Log.debug(e.message ?: "Unknown IOException occurred")
        if (!returnExceptionMessage) {
            Pair(null, -1) // Don't print the stacktrace because that is confusing.
        }
        else {
            Pair(e.message, -1)
        }
    }
    catch (e: ProcessNotCreatedException) {
        Log.debug(e.message ?: "Unknown ProcessNotCreatedException occurred")
        // e.g. if the command is just trying if a program can be run or not, and it's not the case
        if (!returnExceptionMessage) {
            Pair(null, -1)
        }
        else {
            Pair(e.message, -1)
        }
    }
}

/**
 * Read input and error streams. If non-blocking, we will skip reading the streams if they are not ready.
 */
private fun readInputStream(nonBlocking: Boolean, proc: Process): String {
    var output = ""
    if (nonBlocking) {
        if (proc.inputStream.bufferedReader().ready()) {
            output += proc.inputStream.bufferedReader().readText().trim()
        }
        if (proc.errorStream.bufferedReader().ready()) {
            output += proc.errorStream.bufferedReader().readText().trim()
        }
    }
    else {
        output = proc.inputStream.bufferedReader().readText().trim() + proc.errorStream.bufferedReader().readText().trim()
    }
    return output
}

/**
 * Collect texinputs from various places
 *
 * @param rootFiles If provided, filter run configurations
 * @param expandPaths Expand subdirectories
 */
fun getTexinputsPaths(
    project: Project,
    rootFiles: Collection<VirtualFile>,
    expandPaths: Boolean = true,
    latexmkSearchDirectory: VirtualFile? = null
): List<String> {
    val searchPaths = mutableListOf<String>()
    val runManager = RunManagerImpl.getInstanceImpl(project) as RunManager
    val allConfigurations = runManager.allConfigurationsList
        .filterIsInstance<LatexRunConfiguration>()
    val selectedConfiguratios = if (rootFiles.isEmpty()) allConfigurations else allConfigurations.filter { it.mainFile in rootFiles }
    val configurationTexinputsVariables = selectedConfiguratios.map { it.environmentVariables.envs }.mapNotNull { it.getOrDefault("TEXINPUTS", null) }
    // Not sure which of these takes precedence, or if they are joined together
    val texinputsVariables = configurationTexinputsVariables +
        selectedConfiguratios.map { LatexmkRcFileFinder.getTexinputsVariable(latexmkSearchDirectory ?: project.guessProjectDir() ?: return@map null, it, project) } +
        listOf(if (expandPaths) SystemEnvironment.texinputs else System.getenv("TEXINPUTS"))

    for (texinputsVariable in texinputsVariables.filterNotNull()) {
        for (texInputPath in texinputsVariable.trim('\'').split(File.pathSeparator).filter { it.isNotBlank() }) {
            val path = texInputPath.trimEnd(File.pathSeparatorChar)
            searchPaths.add(path.trimEnd('/'))
            // See the kpathsea manual, // expands to subdirs
            if (path.endsWith("//")) {
                LocalFileSystem.getInstance().findFileByPath(path.trimEnd('/'))?.let { parent ->
                    if (expandPaths) {
                        searchPaths.addAll(
                            parent.allChildDirectories()
                                .filter { it.isDirectory }
                                .map { it.path }
                        )
                    }
                    else {
                        searchPaths.add(parent.path)
                    }
                }
            }
        }
    }
    return searchPaths
}
