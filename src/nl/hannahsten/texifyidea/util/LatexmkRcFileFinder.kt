package nl.hannahsten.texifyidea.util

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import java.io.File
import kotlin.io.path.Path

/**
 * Try to find a latexmkrc file (see the latexmk man page).
 */
object LatexmkRcFileFinder {

    // Note: this cannot be a lazy val because then if there is any exception, the stacktrace will not be shown but be hidden by a NoClassDefFoundError. This way, we will have an ExceptionInInitializerError with will have the original stacktrace as well.
    private var systemLatexmkRcFile: VirtualFile? = null
        get() {
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
                System.getenv("XDG_CONFIG_HOME")?.let { Path(it, "latexmk", "latexmkrc").toString() },
                System.getenv("HOME")?.let { Path(it, ".latexmkrc").toString() },
                System.getenv("USERPROFILE")?.let { Path(it, ".latexmkrc").toString() },
                System.getenv("HOME")?.let { Path(it, ".config", ".latexmkrc").toString() },
            )

            paths.filterNotNull()
                .forEach {
                    LocalFileSystem.getInstance().findFileByIoFile(File(it))?.let { file ->
                        field = file
                        return file
                    }
                }

            return null
        }

    private val isSystemLatexmkRcFilePresent: Boolean = systemLatexmkRcFile != null

    private fun isLocalLatexmkRcFilePresent(compilerArguments: String?, workingDir: String?) = getLocalLatexmkRcFile(compilerArguments, workingDir) != null

    /**
     * Get the first latexmkrc file we can find locally (in the project).
     */
    private fun getLocalLatexmkRcFile(compilerArguments: String?, workingDir: String?): VirtualFile? {
        // 3
        if (workingDir != null) {
            listOf(
                workingDir + File.separator + "latexmkrc",
                workingDir + File.separator + ".latexmkrc"
            ).forEach {
                LocalFileSystem.getInstance().findFileByPath(it)?.let { file -> return file }
            }
        }

        // 4
        if (compilerArguments != null) {
            val arguments = compilerArguments.splitWhitespace().dropWhile { it.isBlank() }
            if (arguments.contains("-r") && arguments.last() != "-r") {
                val path = arguments[arguments.indexOf("-r") + 1]
                LocalFileSystem.getInstance().findFileByPath(path)?.let { file -> return file }
            }
        }

        return null
    }

    fun isLatexmkRcFilePresent(runConfig: LatexRunConfiguration): Boolean {
        val isPresent = isSystemLatexmkRcFilePresent || isLocalLatexmkRcFilePresent(
            runConfig.options.compilerArguments,
            runConfig.workingDirectory
        )

        // The first time, by default don't override what's in the latexmkrc (but avoid resetting the user chosen output format)
        if (isPresent && !runConfig.options.hasBeenRun) {
            runConfig.options.outputFormat = LatexCompiler.OutputFormat.DEFAULT
        }
        return isPresent
    }

    /**
     * Get TEXINPUTS from latexmkrc.
     */
    private fun getTexinputs(file: VirtualFile): String? {
        return """ensure_path\('TEXINPUTS',\s*'(?<path>[^']+)'\)""".toRegex().find(file.inputStream.reader().readText())?.groups?.get("path")?.value
    }

    private var usesLatexmkrc: Boolean? = null

    /**
     * Get the first TEXINPUTS we can find in latexmkrc files.
     * Cached because searching involves quite some system calls, and it's a rarely used feature.
     */
    fun getTexinputsVariable(someFile: PsiFile?, runConfig: LatexRunConfiguration?): String? {
        return if (usesLatexmkrc == false) {
            null
        }
        else {
            val texinputs = getTexinputsVariableNoCache(someFile, runConfig)
            if (usesLatexmkrc == null) {
                usesLatexmkrc = texinputs != null
            }
            texinputs
        }
    }

    /**
     * Check the (first) latexmkrc file for any additions to TEXINPUTS and return that if present.
     *
     * @param someFile Any file, the parent directories will be searched.
     * @param runConfig Run configuration to check for working directory and arguments.
     */
    private fun getTexinputsVariableNoCache(someFile: PsiFile?, runConfig: LatexRunConfiguration?): String? {
        systemLatexmkRcFile?.let { return getTexinputs(it) }
        if (runConfig != null) {
            getLocalLatexmkRcFile(runConfig.options.compilerArguments, runConfig.options.mainFile.resolve()?.parent?.path)?.let { return getTexinputs(it) }
        }
        // File could be anywhere if run configurations are not used, but searching the whole project could be too expensive
        someFile?.virtualFile?.parent?.let { parentDir ->
            parentDir.findChild(".latexmkrc")?.let { return getTexinputs(it) }
            parentDir.findChild("latexmkrc")?.let { return getTexinputs(it) }
        }
        val project = someFile?.project ?: return null
        val projectDir = project.guessProjectDir()
        projectDir?.findChild(".latexmkrc")?.let { return getTexinputs(it) }
        projectDir?.findChild("latexmkrc")?.let { return getTexinputs(it) }
        projectDir?.children?.forEach { directory ->
            directory?.findChild(".latexmkrc")?.let { return getTexinputs(it) }
            directory?.findChild("latexmkrc")?.let { return getTexinputs(it) }
        }
        return null
    }
}