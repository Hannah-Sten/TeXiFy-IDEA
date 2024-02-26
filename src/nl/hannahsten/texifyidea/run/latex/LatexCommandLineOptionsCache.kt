package nl.hannahsten.texifyidea.run.latex

import ai.grazie.utils.dropPrefix
import arrow.atomic.AtomicBoolean
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

/**
 * Automatically get available command line options for all the LaTeX compilers.
 * todo refactor with LatexExternalCommandsIndexCache
 */
object LatexCommandLineOptionsCache {
    // Map compiler name to list of (name, description) pairs where name is without the - or -- prefix
    val cache = mutableMapOf<String, List<Pair<String, String>>>()

    private val isCacheFillInProgress = AtomicBoolean(false)

    /**
     * Get the options for the given compiler, or fill the cache if it is empty.
     * Cache fill is done in the background because it requires system calls so may take significant time.
     */
    fun getOptionsOrFillCache(givenCompiler: String, project: Project): List<Pair<String, String>> {
        if (cache.isNotEmpty()) {
            return cache[givenCompiler] ?: emptyList()
        }

        if (isCacheFillInProgress.compareAndSet(expected = true, new = true)) {
            return emptyList()
        }
        isCacheFillInProgress.getAndSet(true)

        ProgressManager.getInstance().run(object : Backgroundable(project, "Retrieving available command line options for LaTeX compilers...") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    for (compiler in LatexCompiler.values()) {
                        val (output, _) = runCommandWithExitCode(compiler.executableName, "--help")
                        cache[compiler.executableName] = parseHelpOutput(compiler.executableName, output ?: continue)
                    }
                }
                finally {
                    isCacheFillInProgress.getAndSet(false)
                }
            }
        })

        return emptyList()
    }

    private fun parseHelpOutput(compiler: String, text: String): List<Pair<String, String>>{
        // pdflatex, xelatex have similar (but not the same) format
        when (compiler) {
            "pdflatex", "xelatex" -> {
                return text.split("\n")
                    // This is the first option, drop everything in front of it
                    .dropWhile { !it.contains("-cnf-line") }
                    .flatMap {
                        val lineParts = it.split(" ")
                        if (lineParts.size < 2) return@flatMap emptyList()
                        val option = lineParts.first()
                        val description = lineParts.drop(1).joinToString(" ")
                        if (option.startsWith("[-no]")) {
                            listOf(
                                Pair(option.dropPrefix("[-no]"), description),
                                Pair("-no" + option.dropPrefix("[-no]"), description)
                            )
                        }
                        else if(option.startsWith("-")) {
                            listOf(Pair(option, description))
                        }
                        else {
                            emptyList()
                        }
                    }
                    .map { Pair(it.first.trim('-'), it.second) }
            }
            "lualatex" -> {
                return emptyList()
            }
            "latexmk" -> {
                return emptyList()
            }
            else -> {
                return emptyList()
            }
        }
    }
}