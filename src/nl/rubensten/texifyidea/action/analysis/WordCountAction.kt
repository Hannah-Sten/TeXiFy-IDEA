package nl.rubensten.texifyidea.action.analysis

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.*
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.grandparent
import nl.rubensten.texifyidea.util.psiFile
import nl.rubensten.texifyidea.util.referencedFiles
import java.util.regex.Pattern
import javax.swing.JLabel
import javax.swing.SwingConstants

/**
 * @author Ruben Schellekens
 */
open class WordCountAction : AnAction(
        "Word Count",
        "Estimate the word count of the currently active .tex file and inclusions.",
        TexifyIcons.WORD_COUNT
) {

    companion object {

        /**
         * Commands that should be ignored by the word counter.
         */
        private val IGNORE_COMMANDS = listOf(
                "\\usepackage", "\\documentclass", "\\label", "\\linespread", "\\ref", "\\cite", "\\eqref", "\\nameref",
                "\\autoref", "\\fullref", "\\pageref", "\\newcounter", "\\newcommand", "\\renewcommand",
                "\\setcounter", "\\resizebox", "\\includegraphics", "\\include", "\\input", "\\refstepcounter",
                "\\counterwithins", "\\RequirePackage"
        )

        /**
         * Words that are contractions when `'s` is appended.
         */
        private val CONTRACTION_S = listOf(
                "that", "it", "there", "she", "he"
        )

        /**
         * Characters that serve as delimiters for contractions.
         */
        private val CONTRACTION_CHARACTERS = Pattern.compile("['’]")

        /**
         * Words containing solely of punctuation must be ignored.
         */
        private val PUNCTUATION = Pattern.compile("[.,\\-_–:;?!'\"~=+*/\\\\&|]+")
    }

    override fun actionPerformed(event: AnActionEvent?) {
        val virtualFile = event?.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return
        val psiFile = virtualFile.psiFile(project) ?: return

        val (words, chars) = countWords(psiFile)
        val dialog = makeDialog(psiFile, words, chars)

        dialog.show()
    }

    /**
     * Builds the dialog that must show the word count.
     */
    private fun makeDialog(baseFile: PsiFile, wordCount: Int, characters: Int): DialogBuilder {
        return DialogBuilder().apply {
            setTitle("Word count")

            setCenterPanel(JLabel(
                    """|<html>
                        |<p>Analysis of <i>${baseFile.name}</i> (and inclusions):</p>
                        |<table cellpadding=1 style='margin-top:4px'>
                        |   <tr><td style='text-align:right'>Word count:</td><td><b>$wordCount</b></td></tr>
                        |   <tr><td style='text-align:right'>Character count:</td><td><b>$characters</b></td>
                        |</table>
                        |</html>""".trimMargin(),
                    AllIcons.General.InformationDialog,
                    SwingConstants.LEADING
            ))

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
        val fileSet = baseFile.referencedFiles()
        val allNormalText = fileSet.flatMap { it.childrenOfType(LatexNormalText::class) }

        val bibliographies = baseFile.childrenOfType(LatexEnvironment::class)
                .filter {
                    val children = it.childrenOfType(LatexBeginCommand::class)
                    if (children.isEmpty()) {
                        return@filter false
                    }

                    val parameters = children.first().parameterList
                    if (parameters.isEmpty()) {
                        return@filter false
                    }

                    return@filter parameters[0].text == "{thebibliography}"
                }
        val bibliography = bibliographies.flatMap { it.childrenOfType(LatexNormalText::class) }

        val (wordsNormal, charsNormal) = countWords(allNormalText)
        val (wordsBib, charsBib) = countWords(bibliography)

        return Pair(wordsNormal - wordsBib, charsNormal - charsBib)
    }

    /**
     * Counts all the words in the text elements.
     *
     * @return A pair of the total amount of words, and the amount of characters that make up the words.
     */
    private fun countWords(latexNormalText: List<LatexNormalText>): Pair<Int, Int> {
        // Seperate all latex words.
        val latexWords: MutableSet<PsiElement> = HashSet()
        var characters: Int = 0
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

        var words: Int = 0
        for (word in filteredWords) {
            words += contractionCount(word.text)
            characters += word.textLength
        }

        return Pair(words, characters)
    }

    /**
     * Filters out all the words that should not be counted.
     */
    private fun filterWords(words: MutableSet<PsiElement>): Set<PsiElement> {
        val set: MutableSet<PsiElement> = HashSet()

        for (word in words) {
            if (isWrongCommand(word) || isOptionalParameter(word) || isEnvironmentMarker(word) || isPunctuation(word)) {
                continue
            }

            set.add(word)
        }

        return set
    }

    /**
     * `I've` counts for two words: `I` and `have`. This function gives you the number of original words.
     */
    private fun contractionCount(text: String): Int {
        val split = CONTRACTION_CHARACTERS.split(text)
        var count = 0
        for (i in 0 until split.size) {
            val string = split[i]

            // Only count contractions: so do not count start or end single quotes :)
            if (string.isEmpty()) {
                continue
            }

            if (string.toLowerCase() == "s") {
                if (split.size == 1) {
                    return 1
                }

                if (CONTRACTION_S.contains(split[i - 1].toLowerCase())) {
                    count++
                }
            }
            else {
                count++
            }
        }

        return count
    }

    private fun isPunctuation(word: PsiElement): Boolean {
        return PUNCTUATION.matcher(word.text).matches()
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

        if (IGNORE_COMMANDS.contains(command.name)) {
            return true
        }

        return false
    }
}
