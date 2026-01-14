package nl.hannahsten.texifyidea.action.analysis

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.jetbrains.annotations.NonNls
import java.util.*
import java.util.regex.Pattern
import kotlin.io.path.Path

internal data class CountData(
    val scope: CountScope,
    val wordCount: Int? = null,
    val charsCount: Int? = null,
) {
    fun name() = scope.name()
}

internal sealed class CountScope(val renderOrder: Int) {
    internal class FileCountScope(val psiFile: PsiFile) : CountScope(0) {
        override fun name(): String = "Current file (${psiFile.name})"
    }

    internal class DocumentCountScope : CountScope(1) {
        override fun name(): String = "Entire document"
    }

    abstract fun name(): String

    companion object {
        val DOCUMENT_SCOPE = DocumentCountScope()
    }
}

typealias ErrorMessage = String

internal sealed class CountMethod {
    internal class TexCount : CountMethod() {
        override fun renderString(): String = "<a href='https://app.uio.no/ifi/texcount/intro.html'>texcount</a> (default if available)"

        fun count(root: VirtualFile, workingDirectory: @NonNls String, psiFile: PsiFile): DialogBuilder {
            // Make sure the file is written to disk before running an external tool on it
            FileDocumentManager.getInstance().apply {
                saveDocument(
                    getDocument(root) ?: return@apply
                )
            }
            val (output, exitCode) = runCommandWithExitCode("texcount", "-brief", "-inc", "-sum", root.name, workingDirectory = Path(workingDirectory))
            return if (exitCode != 0 || output == null) {
                makeDialog(psiFile, emptyList(), errorMessage = "texcount failed")
            }
            else {
                val (countData, error) = parseOutput(output, workingDirectory, psiFile)
                makeDialog(psiFile, countData, error)
            }
        }

        private fun parseOutput(output: String, workingDirectory: String, psiFile: PsiFile): Pair<List<CountData>, ErrorMessage?> {
            // texcount output is formatted as follows:
            //
            // <error message>?
            // <digits>: File: <root.tex>
            // (<digits>: Included file: <included file.tex>)*
            // Sum of files: <root.tex>
            // <digits>: File(s) total: <root.tex><error message>?

            val mainFileRegex = """(\d+): File: (.*)""".toRegex()
            val includedFileRegex = """(\d+): Included file: (.*)""".toRegex()
            val totalRegex = """(\d+): File\(s\) total: """.toRegex()

            val mainMatch = mainFileRegex.find(output)
            val rootFileName = mainMatch?.groupValues[2]
            val totalMatch = totalRegex.find(output)

            val errorMessage = mainMatch?.range?.first?.let {
                if (it > 0) {
                    output.substring(0, it)
                }
                else {
                    totalMatch?.range?.last?.let { errorStart ->
                        val s = errorStart + (rootFileName?.length ?: 0)
                        if (s < output.length) {
                            output.substring(s + 1, output.length)
                        }
                        else null
                    }
                }
            }

            if (mainMatch?.groupValues[2] == psiFile.name) {
                val totalCount = totalRegex.find(output, mainMatch.range.last)?.groupValues[1]?.toInt()
                return listOf(
                    CountData(CountScope.FileCountScope(psiFile), mainMatch.groupValues[1].toInt()),
                    CountData(CountScope.DOCUMENT_SCOPE, totalCount)
                ) to errorMessage
            }
            else {
                val includedFileMatches = includedFileRegex.findAll(output).toList()
                val counts = mutableListOf(
                    CountData(
                        CountScope.DOCUMENT_SCOPE,
                        (mainMatch?.groupValues[1]?.toInt() ?: 0) + includedFileMatches.sumOf { it.groupValues[1].toInt() }
                    )
                )
                includedFileMatches.firstOrNull { workingDirectory + it.groupValues[2].removePrefix(".") == psiFile.virtualFile.path }
                    ?.groupValues[1]?.toInt()?.let {
                    counts.add(0, CountData(CountScope.FileCountScope(psiFile), it))
                }
                return counts to errorMessage
            }
        }
    }

    internal class DefaultCount : CountMethod() {
        object Util {
            /**
             * Commands that should be ignored by the word counter.
             */
            val IGNORE_COMMANDS = setOf(
                "\\usepackage", "\\documentclass", "\\label", "\\linespread", "\\ref", "\\cite", "\\eqref", "\\nameref",
                "\\autoref", "\\fullref", "\\pageref", "\\newcounter", "\\newcommand", "\\renewcommand",
                "\\setcounter", "\\resizebox", "\\includegraphics", "\\include", "\\input", "\\refstepcounter",
                "\\counterwithins", "\\RequirePackage", "\\bibliography", "\\bibliographystyle"
            )

            /**
             * List of all environments that must be ignored.
             */
            val IGNORE_ENVIRONMENTS = setOf(
                "tikzpicture", "thebibliography"
            )

            /**
             * Words that are contractions when `'s` is appended.
             */
            val CONTRACTION_S = listOf(
                "that", "it", "there", "she", "he"
            )

            /**
             * Characters that serve as delimiters for contractions.
             */
            val CONTRACTION_CHARACTERS: Pattern = Pattern.compile("['’]")

            /**
             * Words containing solely of punctuation must be ignored.
             */
            val PUNCTUATION: Pattern = Pattern.compile("[.,\\-_–:;?!'\"~=+*/\\\\&|]+")
        }

        override fun renderString() = "TeXiFy word count"

        fun count(psiFile: PsiFile): DialogBuilder = makeDialog(psiFile, countWords(psiFile))

        /**
         * Counts all the words in the given base file.
         */
        private fun countWords(baseFile: PsiFile): List<CountData> {
            val fileSet = baseFile.referencedFileSet()
                .filter { it.name.endsWith(".tex", ignoreCase = true) }

            val words = mutableMapOf<PsiFile, Int>()
            val chars = mutableMapOf<PsiFile, Int>()

            for (f in fileSet) {
                val normal = mutableListOf<LatexNormalText>()
                val parameter = mutableListOf<LatexParameterText>()
                val bib = mutableListOf<LatexEnvironment>()
                f.traverse { e ->
                    when (e) {
                        is LatexNormalText -> {
                            normal.add(e)
                        }

                        is LatexParameterText -> {
                            if (e.command?.text !in Util.IGNORE_COMMANDS) {
                                parameter.add(e)
                            }
                        }

                        is LatexEnvironment -> {
                            if (e.getEnvironmentName() == "thebibliography") {
                                bib.add(e)
                            }
                        }
                    }
                    true
                }

                val (w, c) = countWords(normal.toList(), parameter.toList(), bib.flatMap { it.collectSubtreeTyped<LatexNormalText>() })
                words[f] = w
                chars[f] = c
            }

            return listOf(
                CountData(CountScope.FileCountScope(baseFile), words[baseFile], chars[baseFile]),
                CountData(CountScope.DOCUMENT_SCOPE, words.values.sum(), chars.values.sum())
            )
        }

        /**
         * Counts all the words in the text elements.
         *
         * @return A pair of the total amount of words, and the amount of characters that make up the words.
         */
        private fun countWords(latexNormalText: List<PsiElement>): Pair<Int, Int> {
            // separate all latex words.
            val latexWords: MutableSet<PsiElement> = HashSet()
            var characters = 0
            for (text in latexNormalText) {
                var child = text.firstChild
                while (child != null) {
                    latexWords.add(child)
                    child = child.nextSibling

                    if (child is PsiWhiteSpace) {
                        characters += child.textLength
                        child = child.nextSibling
                        continue
                    }
                }
            }

            // Count words.
            val filteredWords = filterWords(latexWords)

            var wordCount = 0
            for (word in filteredWords) {
                wordCount += contractionCount(word.text)
                characters += word.textLength
            }

            return Pair(wordCount, characters)
        }

        private fun countWords(normal: List<LatexNormalText>, parameter: List<LatexParameterText>, bib: List<LatexNormalText>): Pair<Int, Int> {
            val (wordsNormal, charsNormal) = countWords(normal)
            val (wordsParameter, charsParameter) = countWords(parameter)
            val (wordsBib, charsBib) = countWords(bib)

            return wordsNormal + wordsParameter - wordsBib to charsNormal + charsParameter - charsBib
        }

        /**
         * Filters out all the words that should not be counted.
         */
        private fun filterWords(words: MutableSet<PsiElement>): Set<PsiElement> {
            val set: MutableSet<PsiElement> = HashSet()

            for (word in words) {
                if (isWrongCommand(word) ||
                    isOptionalParameter(word) ||
                    isEnvironmentMarker(word) ||
                    isPunctuation(word) ||
                    isInWrongEnvironment(word) ||
                    isInMath(word)
                ) {
                    continue
                }

                set.add(word)
            }

            return set
        }

        /**
         * Checks if the word is in inline math mode or not.
         */
        private fun isInMath(word: PsiElement): Boolean = word.inMathContext()

        /**
         * Checks if the given word is in an environment that must be ignored.
         */
        private fun isInWrongEnvironment(word: PsiElement): Boolean = word.inDirectEnvironment(Util.IGNORE_ENVIRONMENTS)

        /**
         * `I've` counts for two words: `I` and `have`. This function gives you the number of original words.
         */
        private fun contractionCount(word: String): Int {
            val split = Util.CONTRACTION_CHARACTERS.split(word)
            var count = 0
            for (i in split.indices) {
                val string = split[i]

                // Only count contractions: so do not count start or end single quotes :)
                if (string.isEmpty()) continue

                if (string.lowercase(Locale.getDefault()) == "s") {
                    if (split.size == 1) return 1

                    if (i > 0 && Util.CONTRACTION_S.contains(split[i - 1].lowercase(Locale.getDefault()))) {
                        count++
                    }
                }
                else count++
            }

            return count
        }

        private fun isPunctuation(word: PsiElement): Boolean = Util.PUNCTUATION.matcher(word.text).matches()

        private fun isEnvironmentMarker(word: PsiElement): Boolean {
            val grandparent = word.grandparent(7)
            return grandparent is LatexBeginCommand || grandparent is LatexEndCommand
        }

        private fun isOptionalParameter(word: PsiElement): Boolean = word.grandparent(5) is LatexOptionalParam

        private fun isWrongCommand(word: PsiElement): Boolean {
            val command = word.grandparent(7) as? LatexCommands
                ?: return false

            return Util.IGNORE_COMMANDS.contains(command.name)
        }
    }

    abstract fun renderString(): String

    /**
     * Builds the dialog that must show the word count.
     */
    protected fun makeDialog(baseFile: PsiFile, count: List<CountData>, errorMessage: String? = null): DialogBuilder = DialogBuilder().apply {
        fun Panel.renderCount(data: CountData) {
            groupRowsRange(
                title = data.name().lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.titlecaseChar() } },
            ) {
                data.wordCount?.let {
                    row("Word Count:") { text(it.toString()) }
                }
                data.charsCount?.let {
                    row("Character count:") { text(it.toString()) }
                }
            }
        }

        setTitle("Word Count")

        setCenterPanel(
            panel {
                row {
                    panel {
                        row {
                            text("Analysis of document that includes ${baseFile.name}")
                                .comment("Counted with ${renderString()}")
                        }
                        if (count.isNotEmpty()) {
                            count.sortedBy { it.scope.renderOrder }.forEach {
                                renderCount(it)
                            }
                        }
                        else {
                            row {
                                text("Word count failed")
                            }
                        }
                    }
                }

                errorMessage?.let {
                    row {
                        icon(AllIcons.General.ErrorDialog)
                        text(it)
                    }
                }
            }
        )

        addOkAction()
        setOkOperation {
            dialogWrapper.close(0)
        }
    }

    companion object {
        val DEFAULT_COUNT = DefaultCount()
        val TEX_COUNT = TexCount()
    }
}

/**
 * @author Hannah Schellekens
 */
open class WordCountAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return
        val psiFile = virtualFile.psiFile(project) ?: return

        // Prefer texcount, I think it is slightly more accurate
        val dialog = if (SystemEnvironment.isAvailable("texcount")) {
            val root = psiFile.findRootFile().virtualFile
            val workingDirectory = root.parent?.path

            if (root == null || workingDirectory == null) {
                CountMethod.DEFAULT_COUNT.count(psiFile)
            }
            else {
                CountMethod.TEX_COUNT.count(root, workingDirectory, psiFile)
            }
        }
        else {
            CountMethod.DEFAULT_COUNT.count(psiFile)
        }

        dialog.show()
    }
}
