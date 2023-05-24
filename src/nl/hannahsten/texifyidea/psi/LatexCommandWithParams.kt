package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

/**
 * This class allows the LatexCommandsImplMixin class to 'inject' methods into LatexCommands(Impl).
 * In general, it is more straightforward to provide extension methods in LatexCommandsUtil.
 */
interface LatexCommandWithParams : PsiElement {

    val parameterList: List<LatexParameter>
    val requiredParameters: List<String>
    fun getOptionalParameterMap(): Map<LatexKeyValKey, LatexKeyValValue?>
}