package nl.hannahsten.texifyidea.util

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File

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
