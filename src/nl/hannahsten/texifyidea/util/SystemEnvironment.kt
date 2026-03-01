package nl.hannahsten.texifyidea.util

import com.intellij.execution.RunManager
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.rd.util.ConcurrentHashMap
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfigurationStaticSupport
import nl.hannahsten.texifyidea.util.SystemEnvironment.Companion.isAvailable
import nl.hannahsten.texifyidea.util.files.allChildDirectories
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File

/**
 * Information about the system other than the LatexDistribution or the OS.
 */
class SystemEnvironment {

    companion object {

        val wslCommand by lazy {
            // PyCharm 2025.1+ does not support wsl --exec, see #4115
            if (ApplicationInfo.getInstance().fullApplicationName.contains("IntelliJ")) {
                arrayOf("wsl", "--exec", "bash", "-ic")
            }
            else {
                arrayOf("bash", "-ic")
            }
        }

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

        // Map a system command to the full location, as raw output (so may be multiple paths on Windows) from the where/which command
        // This is cached because the value will not change between restarts
        var executableLocationCache = ConcurrentHashMap<String, String?>()

        // Assumes version will be given in the format GNOME Document Viewer 3.34.2
        val evinceVersion: DefaultArtifactVersion by lazy {
            DefaultArtifactVersion("evince --version".runCommand()?.split(" ")?.lastOrNull() ?: "")
        }

        val texinputs by lazy {
            runCommand("kpsewhich", "--expand-var", $$"'$TEXINPUTS'")
        }

        val texmfhome by lazy {
            runCommand("kpsewhich", "--expand-var", $$"'$TEXMFHOME'")
        }
    }
}

/**
 * Collect search paths from TEXINPUTS and TEXMFHOME from run configs
 *
 * @param rootFiles If provided, filter run configurations
 * @param expandPaths Expand subdirectories
 */
fun getTexinputsPaths(
    project: Project,
    rootFiles: Collection<VirtualFile>,
    expandPaths: Boolean = true,
    latexmkSearchDirectory: VirtualFile? = null
): Set<String> {
    val searchPaths = mutableSetOf<String>()
    val runManager = RunManagerImpl.getInstanceImpl(project) as RunManager
    val allConfigurations = runManager.allConfigurationsList
        .filterIsInstance<LatexRunConfiguration>()
    val selectedConfiguratios = if (rootFiles.isEmpty()) allConfigurations
    else allConfigurations.filter { LatexRunConfigurationStaticSupport.resolveMainFile(it) in rootFiles }
    val runConfigVariables = selectedConfiguratios.map { it.environmentVariables.envs }

    val configurationTexinputsVariables = runConfigVariables.mapNotNull { it.getOrDefault("TEXINPUTS", null) }
    val configurationTexmfhomeVariables = runConfigVariables.mapNotNull { it.getOrDefault("TEXMFHOME", null) }

    val latexmkTexinputs = selectedConfiguratios.map { LatexmkRcFileFinder.getTexinputsVariable(latexmkSearchDirectory ?: project.guessProjectDir() ?: return@map null, it, project) }

    val systemTexinputs = listOf(if (expandPaths) SystemEnvironment.texinputs else System.getenv("TEXINPUTS"))
    val systemTexmfhome = listOf(if (expandPaths) SystemEnvironment.texmfhome else System.getenv("TEXMFHOME"))

    // Not sure which of these takes precedence, or if they are joined together
    val texinputsVariables = configurationTexinputsVariables + latexmkTexinputs + systemTexinputs

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

    // Most files are searched for in subdirectories of tex/generic or tex/latex, see kpsewhich -help-format | grep -1 sty
    (configurationTexmfhomeVariables + systemTexmfhome)
        .filterNotNull()
        .flatMap { it.split(",") }
        .map { it.trim('\'').trimEnd('/').replaceFirst("~", System.getProperty("user.home")) }
        .mapNotNull { LocalFileSystem.getInstance().findFileByPath("$it/tex") }
        .forEach { parent ->
            searchPaths.addAll(
                parent.allChildDirectories()
                    .filter { it.isDirectory }
                    .map { it.path }
            )
        }

    return searchPaths
}
