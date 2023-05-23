package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.util.psi.firstParentOfType
import nl.hannahsten.texifyidea.util.psi.remove

fun getNameIdentifier(element: BibtexId): PsiElement {
    return element
}

fun setName(element: BibtexId, name: String): PsiElement {
    // Replace the complete bibtex entry to automatically update the index (which is on entries, not ids)
    val entry = element.firstParentOfType(BibtexEntry::class)
    val oldName = element.name ?: return element
    val newText = entry?.text?.replaceFirst(oldName, name) ?: return element
    val newElement = PsiFileFactory.getInstance(element.project).createFileFromText("DUMMY.tex", BibtexLanguage, newText, false, true).firstChild
    entry.parent.node.replaceChild(entry.node, newElement.node)
    return element
}

fun getName(element: BibtexId): String {
    return element.text
}

fun delete(element: BibtexId) {
    val text = element.text ?: return

    val searchScope = GlobalSearchScope.fileScope(element.containingFile)
    BibtexEntryIndex().getEntryByName(text, element.project, searchScope).forEach {
        it.remove()
    }
}