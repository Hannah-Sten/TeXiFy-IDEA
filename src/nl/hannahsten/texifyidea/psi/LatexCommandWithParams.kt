package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

/**
 * This class allows the LatexCommandsImplMixin class to 'inject' methods into LatexCommands(Impl).
 * In general, it is more straightforward to provide extension methods in LatexCommandsUtil.
 */
interface LatexCommandWithParams : PsiElement {

    /**
     * Get the name of the command, for example \newcommand, from the stub if available, otherwise default to getting the text from psi.
     */
//    fun getName(): String?

    val parameterList: List<LatexParameter>

    /**
     * Generates a list of all names of all required parameters in the command.
     */
    fun getRequiredParameters(): List<String>

    fun getOptionalParameterMap(): Map<LatexKeyValKey, LatexKeyValValue?>
}