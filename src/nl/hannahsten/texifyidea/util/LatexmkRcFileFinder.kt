package nl.hannahsten.texifyidea.util

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import java.io.File

/**
 * Try to find a latexmkrc file (see the latexmk man page).
 */
object LatexmkRcFileFinder {

    private val isSystemLatexmkRcFilePresent: Boolean by lazy {
        // 1
        if (SystemInfo.isLinux) {
            listOf(
                "/opt/local/share/latexmk/LatexMk",
                "/usr/local/share/latexmk/LatexMk",
                "/usr/local/lib/latexmk/LatexMk",
                "/cygdrive/local/share/latexmk/LatexMk"
            ).forEach {
                if (LocalFileSystem.getInstance().findFileByPath(it) != null) return@lazy true
                if (LocalFileSystem.getInstance().findFileByPath(it.replace("LatexMk", "latexmkrc")) != null) return@lazy true
            }
        }
        else if (SystemInfo.isWindows) {
            listOf(
                "C:\\latexmk\\LatexMk",
                "C:\\latexmk\\latexmkrc"
            ).forEach {
                if (LocalFileSystem.getInstance().findFileByPath(it) != null) return@lazy true
            }
        }
        System.getenv("LATEXMKRCSYS")?.let {
            if (LocalFileSystem.getInstance().findFileByPath(it) != null) return@lazy true
        }

        // 2
        listOf(
            "${System.getenv("XDG_CONFIG_HOME")}/latexmk/latexmkrc",
            "${System.getenv("HOME")}/.latexmkrc",
            "${System.getenv("USERPROFILE")}/.latexmkrc",
            "${System.getenv("HOME")}/.config/.latexmkrc"
        ).forEach {
            if (LocalFileSystem.getInstance().findFileByPath(it) != null) return@lazy true
        }

        false
    }

    private fun isLocalLatexmkRcFilePresent(compilerArguments: String?, workingDir: String?): Boolean {
        // 3
        if (workingDir != null) {
            listOf(
                workingDir + File.separator + "latexmkrc",
                workingDir + File.separator + ".latexmkrc"
            ).forEach {
                if (LocalFileSystem.getInstance().findFileByPath(it) != null) return true
            }
        }

        // 4
        if (compilerArguments != null) {
            val arguments = compilerArguments.splitWhitespace().dropWhile { it.isBlank() }
            if (arguments.contains("-r") && arguments.last() != "-r") {
                val path = arguments[arguments.indexOf("-r") + 1]
                if (LocalFileSystem.getInstance().findFileByPath(path) != null) return true
            }
        }

        return false
    }

    fun isLatexmkRcFilePresent(runConfig: LatexRunConfiguration): Boolean {
        val isPresent = isSystemLatexmkRcFilePresent || isLocalLatexmkRcFilePresent(runConfig.compilerArguments, runConfig.mainFile?.parent?.path)

        // The first time, by default don't override what's in the latexmkrc (but avoid resetting the user chosen output format)
        if (isPresent && !runConfig.hasBeenRun) {
            runConfig.outputFormat = LatexCompiler.Format.DEFAULT
        }
        return isPresent
    }
}