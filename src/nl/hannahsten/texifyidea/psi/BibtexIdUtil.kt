package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.util.firstParentOfType

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
    // Drop the , separator
//    return element.text.dropLast(1)
    return element.text
}