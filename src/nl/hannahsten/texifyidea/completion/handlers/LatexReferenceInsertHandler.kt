package nl.hannahsten.texifyidea.completion.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import nl.hannahsten.texifyidea.file.BibtexFile
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

/**
 * @author Sten Wessel
 */
class LatexReferenceInsertHandler(private val remote: Boolean = false, private val remoteBib: BibtexEntry? = null) : InsertHandler<LookupElement> {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val model = context.editor.caretModel
        model.moveToOffset(model.offset + 1)

        if (remote and TexifySettings.getState().automaticBibtexImport) {
            remoteBib ?: return
            // remoteBib may come from a file with CRLF line separators, which cannot be accepted into a psi file, so we need to fix that
            val newBibEntry = LatexPsiHelper(context.project).createBibtexFromText(remoteBib.text.replace("\r\n", "\n")).findFirstChildOfType(BibtexEntry::class) ?: return
            val file = context.file.originalFile
            val bibtexFile = file.referencedFileSet().firstOrNull { it is BibtexFile }
            // find a bibtex file and add the new bib entry to it
            bibtexFile?.add(newBibEntry)
        }
    }
}