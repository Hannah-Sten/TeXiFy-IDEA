package nl.hannahsten.texifyidea.run.latex

import arrow.atomic.AtomicBoolean
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.util.remove
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

/**
 * Automatically get available command line options for all the LaTeX compilers.
 */
object LatexCommandLineOptionsCache {
    // Map compiler name to list of (name, description) pairs where name is without the - or -- prefix
    val cache = mutableMapOf<String, Options>()

    private val isCacheFillInProgress = AtomicBoolean(false)

    /**
     * Get the options for the given compiler, or fill the cache if it is empty.
     * Cache fill is done in the background because it requires system calls so may take significant time.
     */
    fun getOptionsOrFillCache(givenCompiler: String, project: Project): Options {
        if (cache.isNotEmpty()) {
            return cache[givenCompiler] ?: Options()
        }

        if (isCacheFillInProgress.compareAndSet(expected = true, new = true)) {
            return Options()
        }
        isCacheFillInProgress.getAndSet(true)

        fillCache(project)
        return Options()
    }

    private fun getOptions(optionsList: List<Pair<String, String>>): Options {
        return Options().apply {
            for ((option, description) in optionsList) {
                // option is with one - and .longOpt is with two --, but both are possible it seems with pdflatex
                addOption(Option.builder().longOpt(option).desc(description).build())
            }
        }
    }

    private fun fillCache(project: Project): List<Pair<String, String>> {
        ProgressManager.getInstance().run(object : Backgroundable(project, "Retrieving available command line options for LaTeX compilers...") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    for (compiler in LatexCompiler.values()) {
                        val (output, _) = runCommandWithExitCode(compiler.executableName, "--help")
                        if (output != null) {
                            val optionsList = parseHelpOutput(compiler.executableName, output)
                            cache[compiler.executableName] = getOptions(optionsList)
                        }
                        else {
                            cache[compiler.executableName] = Options()
                        }
                    }
                }
                finally {
                    isCacheFillInProgress.getAndSet(false)
                }
            }
        })

        return emptyList()
    }

    /**
     * Parse the output of pdflatex --help to get available compiler options.
     * These are slightly different per compiler.
     * Tested with pdflatex, lualatex, xelatex and latexmk
     */
    fun parseHelpOutput(compiler: String, text: String): List<Pair<String, String>> {
        return text.split("\n")
            .asSequence()
            .map { it.trim(' ').split(if (compiler == "latexmk") " - " else " ") }
            .filter { it.size >= 2 }
            .map { Pair(it.first(), it.drop(1).joinToString(" ").trim(' ')) }
            .flatMap { (option, description) ->
                // [-no] for pdflatex, --[no-] for lualatex
                if (option.contains("[-no]") || option.contains("[no-]")) {
                    val cleanedOption = option.remove("[-no]").remove("[no-]").trim('-')
                    listOf(
                        Pair(cleanedOption, description),
                        Pair("no-$cleanedOption", description)
                    )
                }
                // latexmk
                else if (option.contains(" or ")) {
                    option.split(" or ").map { singleOption -> Pair(singleOption, description) }
                }
                else if (option.startsWith("-")) {
                    listOf(Pair(option, description))
                }
                else {
                    emptyList()
                }
            }
            .map { Pair(it.first.trim(' ').trimStart('-'), it.second) }
            .toList()
    }
}