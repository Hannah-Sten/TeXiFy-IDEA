package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

fun getNameIdentifier(element: BibtexId): PsiElement {
    return element
}

fun setName(element: BibtexId, name: String): PsiElement {
    return element // todo
}

fun getName(element: BibtexId): String {
    // Drop the , separator
    return element.text.dropLast(1)
}