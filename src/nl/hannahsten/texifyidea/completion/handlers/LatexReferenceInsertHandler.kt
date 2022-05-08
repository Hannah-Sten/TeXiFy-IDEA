package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.files.bibtexIdsInFileSet

/**
 * @author Sten Wessel
 */
class LatexReferenceInsertHandler(val remote: Boolean = false, private val remoteBib: BibtexEntry? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val model = context.editor.caretModel
        model.moveToOffset(model.offset + 1)

        if (remote) {
            remoteBib ?: return
            // Add the bib item after the last item we found in the file set, and hope that that makes sense...
            context.file.originalFile.bibtexIdsInFileSet().lastOrNull()?.let {
                it.parent.addAfter(remoteBib, it)
            }
        }
    }
}