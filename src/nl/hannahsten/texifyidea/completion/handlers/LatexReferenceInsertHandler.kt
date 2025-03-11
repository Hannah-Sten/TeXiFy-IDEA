package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.file.BibtexFile
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.bibtexIdsInFileSet
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.parser.firstChildOfType

/**
 * @author Sten Wessel
 */
class LatexReferenceInsertHandler(private val remote: Boolean = false, private val remoteBib: BibtexEntry? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val model = context.editor.caretModel
        model.moveToOffset(model.offset + 1)

        if (remote and TexifySettings.getInstance().automaticBibtexImport) {
            remoteBib ?: return
            // remoteBib may come from a file with CRLF line separators, which cannot be accepted into a psi file, so we need to fix that
            val newBibEntry = LatexPsiHelper(context.project).createBibtexFromText(remoteBib.text.replace("\r\n", "\n")).firstChildOfType(BibtexEntry::class) ?: return

            val bibsInFile = context.file.originalFile.bibtexIdsInFileSet()
            // Add the bib item after the last item we found in the file set, and hope that that makes sense...
            bibsInFile.lastOrNull()?.let {
                it.parent?.addAfter(newBibEntry, it)
            }

            // If there are no bib items in the fileset yet, see if there is a(n empty) bib file we can add the bib entry to.
            if (bibsInFile.isEmpty()) {
                context.file.originalFile
                    .referencedFileSet()
                    .firstOrNull { it is BibtexFile }
                    ?.add(newBibEntry)
            }
        }
    }
}