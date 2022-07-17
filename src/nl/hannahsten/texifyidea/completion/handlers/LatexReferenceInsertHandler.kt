package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.file.BibtexFile
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.files.bibtexIdsInFileSet
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.files.referencedFiles

/**
 * @author Sten Wessel
 */
class LatexReferenceInsertHandler(private val remote: Boolean = false, private val remoteBib: BibtexEntry? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val model = context.editor.caretModel
        model.moveToOffset(model.offset + 1)

        if (remote) {
            remoteBib ?: return
            val bibsInFile = context.file.originalFile.bibtexIdsInFileSet()
            // Add the bib item after the last item we found in the file set, and hope that that makes sense...
            bibsInFile.lastOrNull()?.let {
                it.parent.addAfter(remoteBib, it)
            }

            // If there are no bib items in the fileset yet, see if there is a(n empty) bib file we can add the bib entry to.
            if (bibsInFile.isEmpty()) {
                context.file.originalFile
                    .referencedFileSet()
                    .firstOrNull { it is BibtexFile }
                    ?.add(remoteBib)
            }
        }
    }
}