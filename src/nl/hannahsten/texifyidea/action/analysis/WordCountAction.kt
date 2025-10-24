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
            makeDialog(psiFile, output.toInt())
        }
        else {
            // If there is an error, the output will contain both word count and error message (which could indicate a problem with the document itself)
            val words = "[0-9]+".toRegex().find(output ?: "")?.value
            makeDialog(psiFile, wordCount = words?.toIntOrNull(), errorMessage = output?.drop(words?.length ?: 0))
        }
    }

    private fun defaultWordCount(psiFile: PsiFile): DialogBuilder {
        val (words, chars) = countWords(psiFile)
        return makeDialog(psiFile, words, chars)
    }

    private fun formatAsHtml(type: String, message: String?): String {
        return if (message == null) {
            ""
        }
        else {
            "|   <tr><td style='text-align:right'>$type:</td><td><b>${message.take(5000)}</b></td></tr>"
        }
    }

    /**
     * Builds the dialog that must show the word count.
     */
    private fun makeDialog(baseFile: PsiFile, wordCount: Int?, characters: Int? = null, errorMessage: String? = null): DialogBuilder {
        return DialogBuilder().apply {
            setTitle("Word Count")

            setCenterPanel(
                JLabel(
                    """|<html>
                        |<p>Analysis of <i>${baseFile.name}</i> (and inclusions):</p>
                        |<table cellpadding=1 style='margin-top:4px'>
                        ${formatAsHtml("Word count", wordCount?.toString())}
                        ${formatAsHtml("Characters", characters?.toString())}
                        ${formatAsHtml("Error message", errorMessage)}
                        |</table>
                        |</html>
                    """.trimMargin(),
                    AllIcons.General.InformationDialog,
                    SwingConstants.LEADING
                )
            )

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }
        }
    }

    /**
     * Counts all the words in the given base file.
     */
    private fun countWords(baseFile: PsiFile): Pair<Int, Int> {
        val fileSet = baseFile.referencedFileSet()
            .filter { it.name.endsWith(".tex", ignoreCase = true) }
        val allNormalText = mutableListOf<LatexNormalText>()
        val parameterText = mutableListOf<LatexParameterText>()
        val bibliographies = mutableListOf<LatexEnvironment>()

        for (f in fileSet) {
            f.traverse { e ->
                when (e) {
                    is LatexNormalText -> {
                        allNormalText.add(e)
                    }
                    is LatexParameterText -> {
                        if (e.command?.text !in Util.IGNORE_COMMANDS) {
                            parameterText.add(e)
                        }
                    }
                    is LatexEnvironment -> {
                        if(e.getEnvironmentName() == "thebibliography") {
                            bibliographies.add(e)
                        }
                    }
                }
                true
            }
        }

        val bibliography = bibliographies.flatMap { it.collectSubtreeTyped<LatexNormalText>() }

        val (wordsNormal, charsNormal) = countWords(allNormalText)
        val (wordsParameter, charsParameter) = countWords(parameterText)
        val (wordsBib, charsBib) = countWords(bibliography)

        return Pair(wordsNormal + wordsParameter - wordsBib, charsNormal + charsParameter - charsBib)
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

    /**
     * Filters out all the words that should not be counted.
     */
    private fun filterWords(words: MutableSet<PsiElement>): Set<PsiElement> {
        val set: MutableSet<PsiElement> = HashSet()

        for (word in words) {
            if (isWrongCommand(word) || isOptionalParameter(word) || isEnvironmentMarker(word) || isPunctuation(word) ||
                isInWrongEnvironment(word) || isInMath(word)
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
    private fun isInMath(word: PsiElement): Boolean {
        return word.inMathContext()
    }

    /**
     * Checks if the given word is in an environment that must be ignored.
     */
    private fun isInWrongEnvironment(word: PsiElement): Boolean {
        return word.inDirectEnvironment(Util.IGNORE_ENVIRONMENTS)
    }

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

    private fun isPunctuation(word: PsiElement): Boolean {
        return Util.PUNCTUATION.matcher(word.text).matches()
    }

    private fun isEnvironmentMarker(word: PsiElement): Boolean {
        val grandparent = word.grandparent(7)
        return grandparent is LatexBeginCommand || grandparent is LatexEndCommand
    }

    private fun isOptionalParameter(word: PsiElement): Boolean {
        return word.grandparent(5) is LatexOptionalParam
    }

    private fun isWrongCommand(word: PsiElement): Boolean {
        val command = word.grandparent(7) as? LatexCommands ?: return false

        return Util.IGNORE_COMMANDS.contains(command.name)
    }
}
