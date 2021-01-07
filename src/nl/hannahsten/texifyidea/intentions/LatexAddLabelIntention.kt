package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import nl.hannahsten.texifyidea.util.findLatexAndBibtexLabelStringsInFileSet
import kotlin.math.max

abstract class LatexAddLabelIntention : TexifyIntentionBase("Add label") {
    protected inline fun <reified T : PsiElement> findTarget(editor: Editor?, file: PsiFile?): T? {
        val offset = editor?.caretModel?.offset ?: return null
        val element = file?.findElementAt(offset) ?: return null
        // Also check one position back, because we want it to trigger in \section{a}<caret>
        return element as? T ?: element.parentOfType<T>()
        ?: file.findElementAt(max(0, offset - 1)) as? T
        ?: file.findElementAt(max(0, offset - 1))?.parentOfType<T>()
    }

    protected fun getUniqueLabelName(base: String, prefix: String?, file: PsiFile): String {
        val labelBase = "$prefix:$base"
        val allLabels = file.findLatexAndBibtexLabelStringsInFileSet()
        return appendCounter(labelBase, allLabels)
    }

    /**
     * Keeps adding a counter behind the label until there is no other label with that name.
     */
    private fun appendCounter(label: String, allLabels: Set<String>): String {
        var counter = 2
        var candidate = label

        while (allLabels.contains(candidate)) {
            candidate = label + (counter++)
        }

        return candidate
    }
}