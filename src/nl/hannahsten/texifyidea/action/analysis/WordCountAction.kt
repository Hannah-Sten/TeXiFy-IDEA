package nl.hannahsten.texifyidea.action.analysis

import arrow.core.flatten
import arrow.core.padZip
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.PaletteKeys
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RowsRange
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import fleet.util.capitalizeLocaleAgnostic
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.files.findRootFile
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.util.*
import java.util.regex.Pattern
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.io.path.Path

/**
 * @author Hannah Schellekens
 */
open class WordCountAction : AnAction() {

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

    private data class TextCount(
        val total: Int,
        val currentFile: Int? = null
    )

    private data class CountData(
        val wordCount: TextCount,
        val charsCount: TextCount? = null
    )

    override fun actionPerformed(event: AnActionEvent) {
        val virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return
        val psiFile = virtualFile.psiFile(project) ?: return

        // Prefer texcount, I think it is slightly more accurate
        val dialog = if (SystemEnvironment.isAvailable("texcount")) {
            val root = psiFile.findRootFile().virtualFile
            val workingDirectory = root.parent?.path

            if (root == null || workingDirectory == null) {
                defaultWordCount(psiFile)
            }
            else {
                runTexcount(root, workingDirectory, psiFile)
            }
        }
        else {
            defaultWordCount(psiFile)
        }

        dialog.show()
    }

    private fun runTexcount(
        root: VirtualFile,
        workingDirectory: @NonNls String,
        psiFile: PsiFile
    ): DialogBuilder {
        // Make sure the file is written to disk before running an external tool on it
        FileDocumentManager.getInstance().apply { saveDocument(getDocument(root) ?: return@apply) }
        val (output, exitCode) = runCommandWithExitCode("texcount", "-1", "-inc", "-sum", root.name, workingDirectory = Path(workingDirectory))
        return if (exitCode == 0 && output?.toIntOrNull() != null) {
            makeDialog(psiFile, CountData(TextCount(output.toInt())))
        }
        else {
            // If there is an error, the output will contain both word count and error message (which could indicate a problem with the document itself)
            val words = "[0-9]+".toRegex().find(output ?: "")?.value
            makeDialog(psiFile, count = words?.let { CountData(TextCount(it.toInt())) }, errorMessage = output?.drop(words?.length ?: 0))
        }
    }

    private fun defaultWordCount(psiFile: PsiFile): DialogBuilder {
        return makeDialog(psiFile, countWords(psiFile))
    }

    /**
     * Builds the dialog that must show the word count.
     */
    private fun makeDialog(baseFile: PsiFile, count: CountData?, errorMessage: String? = null): DialogBuilder = DialogBuilder().apply {
        fun Panel.renderCount(title: String, count: TextCount) {
            groupRowsRange(
                title = title.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.titlecaseChar() } },
                topGroupGap = false,
                bottomGroupGap = false
            ) {
                row("Entire document:") { label(count.total.toString()) }
                count.currentFile?.let {
                    row("Current file (${baseFile.name}):") { label(it.toString()) }
                }
            }
        }

        setTitle("Word Count")

        setCenterPanel(
            panel {
                row {
                    icon(AllIcons.General.InformationDialog)
                    panel {
                        row {
                            text("Analysis of document that includes ${baseFile.name}")
                        }
                        if (count != null) {
                            renderCount("word count", count.wordCount)
                            count.charsCount?.let { renderCount("character count", it) }
                        } else {
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

    /**
     * Counts all the words in the given base file.
     */
    private fun countWords(baseFile: PsiFile): CountData {
        val fileSet = baseFile.referencedFileSet()
            .filter { it.name.endsWith(".tex", ignoreCase = true) }
        val allNormalText = mutableMapOf<PsiFile, List<LatexNormalText>>()
        val parameterText = mutableMapOf<PsiFile, List<LatexParameterText>>()
        val bibliographies = mutableMapOf<PsiFile, List<LatexEnvironment>>()

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
                        if(e.getEnvironmentName() == "thebibliography") {
                            bib.add(e)
                        }
                    }
                }
                true
            }
            allNormalText[f] = normal.toList()
            parameterText[f] = parameter.toList()
            bibliographies[f] = bib.toList()
        }

        val bibliography = bibliographies.values.flatten().flatMap { it.collectSubtreeTyped<LatexNormalText>() }

        val (wordsTotal, charsTotal) = countWords(allNormalText.values.flatten(), parameterText.values.flatten(), bibliography)
        val (wordsCurrent, charsCurrent) = countWords(
            allNormalText[baseFile] ?: emptyList(),
            parameterText[baseFile] ?: emptyList(),
            bibliographies[baseFile]?.flatMap { it.collectSubtreeTyped<LatexNormalText>() } ?: emptyList()
        )

        return CountData(
            wordCount = TextCount(wordsTotal, wordsCurrent),
            charsCount = TextCount(charsTotal, charsCurrent)
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
        val command = word.grandparent(7) as? LatexCommands ?: return false

        return Util.IGNORE_COMMANDS.contains(command.name)
    }
}
