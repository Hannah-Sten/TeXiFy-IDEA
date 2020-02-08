package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

fun getReferences(element: LatexNormalText): Array<PsiReference> {
    return emptyArray() // todo
}

/**
 * If [getReferences] returns one reference return that one, null otherwise.
 */
fun getReference(element: LatexNormalText): PsiReference? {
    val references = getReferences(element)
    return if (references.size != 1) {
        null
    } else {
        references[0]
    }
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