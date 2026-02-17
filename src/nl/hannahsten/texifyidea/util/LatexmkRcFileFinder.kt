package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.latex.LatexCompilationRunConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path as KtPath

/**
 * Try to find a latexmkrc file (see the latexmk man page).
 */
object LatexmkRcFileFinder {

    private val isSystemLatexmkRcFilePresent: Boolean by lazy { getSystemLatexmkRcPath() != null }

    private var usesLatexmkrc: Boolean? = null

    private var systemLatexmkRcFile: VirtualFile? = null

    private fun systemLatexmkRcCandidates(): List<String> {
        val paths = mutableListOf<String?>()

        // 1
        if (SystemInfo.isLinux) {
            paths += listOf(
                "/opt/local/share/latexmk/",
                "/usr/local/share/latexmk/",
                "/usr/local/lib/latexmk/",
                "/cygdrive/local/share/latexmk/"
            ).flatMap { listOf(it + "latexMk", it + "latexmkrc") }
        }
        else if (SystemInfo.isWindows) {
            paths += listOf(
                "C:\\latexmk\\LatexMk",
                "C:\\latexmk\\latexmkrc"
            )
        }

        System.getenv("LATEXMKRCSYS")?.let { paths.add(it) }

        // 2
        paths += listOf(
            System.getenv("XDG_CONFIG_HOME")?.let { KtPath(it, "latexmk", "latexmkrc").toString() },
            System.getenv("HOME")?.let { KtPath(it, ".latexmkrc").toString() },
            System.getenv("USERPROFILE")?.let { KtPath(it, ".latexmkrc").toString() },
            System.getenv("HOME")?.let { KtPath(it, ".config", ".latexmkrc").toString() },
        )

        return paths.filterNotNull()
    }

    private fun getSystemLatexmkRcPath(): Path? =
        systemLatexmkRcCandidates()
            .asSequence()
            .mapNotNull { runCatching { Path.of(it) }.getOrNull() }
            .firstOrNull { Files.exists(it) }

    // Note: this cannot be a lazy val because then if there is any exception, the stacktrace will not be shown but be hidden by a NoClassDefFoundError. This way, we will have an ExceptionInInitializerError which will have the original stacktrace as well.
    private fun getSystemLatexmkRcFile(): VirtualFile? {
        val path = getSystemLatexmkRcPath() ?: return null
        LocalFileSystem.getInstance().findFileByIoFile(File(path.toString()))?.let { file ->
            return file
        }

        return null
    }

    private fun getLocalLatexmkRcPath(compilerArguments: String?, workingDir: Path?): Path? {
        // 3
        if (workingDir != null) {
            listOf(
                workingDir.resolve("latexmkrc"),
                workingDir.resolve(".latexmkrc")
            ).forEach {
                if (Files.exists(it)) return it
            }
        }

        // 4
        if (compilerArguments != null) {
            val arguments = compilerArguments.splitWhitespace().dropWhile { it.isBlank() }
            if (arguments.contains("-r") && arguments.last() != "-r") {
                val path = runCatching { Path.of(arguments[arguments.indexOf("-r") + 1]) }.getOrNull() ?: return null
                if (Files.exists(path)) return path
            }
        }

        return null
    }

    private fun isLocalLatexmkRcFilePresent(compilerArguments: String?, workingDir: Path?) = getLocalLatexmkRcPath(compilerArguments, workingDir) != null

    /**
     * Get the first latexmkrc file we can find locally (in the project).
     */
    private fun getLocalLatexmkRcFile(compilerArguments: String?, workingDir: Path?): VirtualFile? {
        val path = getLocalLatexmkRcPath(compilerArguments, workingDir) ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path.toString())
    }

    fun isLatexmkRcFilePresent(runConfig: LatexCompilationRunConfiguration): Boolean {
        val isPresent = isSystemLatexmkRcFilePresent || isLocalLatexmkRcFilePresent(runConfig.compilerArguments, runConfig.getResolvedWorkingDirectory())

        // The first time, by default don't override what's in the latexmkrc (but avoid resetting the user chosen output format)
        if (isPresent && !runConfig.hasBeenRun) {
            runConfig.outputFormat = LatexCompiler.Format.DEFAULT
        }
        return isPresent
    }

    /**
     * Get TEXINPUTS from latexmkrc.
     */
    private fun getTexinputs(file: VirtualFile): String? = """ensure_path\(\s*'TEXINPUTS',\s*'(?<path>[^']+)'\s*\)""".toRegex().find(file.inputStream.reader().readText())?.groups?.get("path")?.value

    /**
     * Get the first TEXINPUTS we can find in latexmkrc files.
     * Cached because searching involves quite some system calls, and it's a rarely used feature.
     */
    fun getTexinputsVariable(directory: VirtualFile, runConfig: LatexCompilationRunConfiguration?, project: Project): String? = if (usesLatexmkrc == false) {
        null
    }
    else {
        val texinputs = getTexinputsVariableNoCache(directory, runConfig, project)
        if (usesLatexmkrc == null) {
            usesLatexmkrc = texinputs != null
        }
        texinputs
    }

    /**
     * Check the (first) latexmkrc file for any additions to TEXINPUTS and return that if present.
     *
     * @param runConfig Run configuration to check for working directory and arguments.
     */
    private fun getTexinputsVariableNoCache(directory: VirtualFile, runConfig: LatexCompilationRunConfiguration?, project: Project): String? {
        // Cache system file
        (systemLatexmkRcFile ?: getSystemLatexmkRcFile().also { systemLatexmkRcFile = it })?.let {
            return getTexinputs(it)
        }

        if (runConfig != null) {
            getLocalLatexmkRcFile(runConfig.compilerArguments, runConfig.mainFile?.parent?.path?.let { Path.of(it) })?.let { return getTexinputs(it) }
        }
        if (!directory.isValid) return null
        // File could be anywhere if run configurations are not used, but searching the whole project could be too expensive
        directory.findChild(".latexmkrc")?.let { return getTexinputs(it) }
        directory.findChild("latexmkrc")?.let { return getTexinputs(it) }

        val projectDir = project.guessProjectDir()
        if (projectDir?.isValid == false) return null
        projectDir?.findChild(".latexmkrc")?.let { return getTexinputs(it) }
        projectDir?.findChild("latexmkrc")?.let { return getTexinputs(it) }
        projectDir?.children?.forEach { childDir ->
            childDir?.findChild(".latexmkrc")?.let { return getTexinputs(it) }
            childDir?.findChild("latexmkrc")?.let { return getTexinputs(it) }
        }
        return null
    }
}
