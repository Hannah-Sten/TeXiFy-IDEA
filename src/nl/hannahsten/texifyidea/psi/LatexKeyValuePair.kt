package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

interface LatexKeyValuePair : PsiElement {

    val keyValKey: LatexKeyValKey
    val keyValValue: LatexKeyValValue?
}