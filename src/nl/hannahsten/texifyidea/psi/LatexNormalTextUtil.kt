package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

fun getReferences(element: LatexNormalText): Array<PsiReference> {
    return emptyArray() // todo
}

fun getNameIdentifier(element: LatexNormalText): PsiElement {
    return element
}

fun setName(element: LatexNormalText, name: String): PsiElement {
    return element
}

fun getName(element: LatexNormalText): String {
    return element.text ?: ""
}