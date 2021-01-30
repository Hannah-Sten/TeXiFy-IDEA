package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

interface LatexCommandWithParams : PsiElement {

    val parameterList: List<LatexParameter>
    val requiredParameters: List<String>
    val optionalParameterMap: Map<LatexKeyvalKey, LatexKeyvalValue>
}