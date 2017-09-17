package nl.rubensten.texifyidea.documentation

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

/**
 * @author Ruben Schellekens
 */
abstract class NavigationLabel<out Psi : PsiElement>(val element: Psi) {

    abstract fun makeLabel(): String

    fun file() = element.containingFile!!

    fun fileName() = file().name

    fun lineNumber(): Int = 1 + StringUtil.offsetToLineNumber(
            file().text,
            element.textOffset
    )  // Because line numbers do start at 1
}