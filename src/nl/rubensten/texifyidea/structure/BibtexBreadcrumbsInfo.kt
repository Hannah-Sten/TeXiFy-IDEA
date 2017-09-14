package nl.rubensten.texifyidea.structure

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.rubensten.texifyidea.BibtexLanguage
import nl.rubensten.texifyidea.psi.BibtexEntry
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.BibtexKey
import nl.rubensten.texifyidea.psi.BibtexTag
import nl.rubensten.texifyidea.util.firstChildOfType
import nl.rubensten.texifyidea.util.substringEnd
import nl.rubensten.texifyidea.util.tokenType

/**
 * @author Ruben Schellekens
 */
open class BibtexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(BibtexLanguage)

    override fun getElementInfo(element: PsiElement) = when (element) {
        is BibtexEntry -> {
            val token = element.tokenType()
            val identifier = when (token?.toLowerCase()) {
                "@preamble" -> ""
                "@string" -> element.firstChildOfType(BibtexKey::class)?.text
                else -> element.firstChildOfType(BibtexId::class)?.text?.substringEnd(1)
            } ?: ""

            if (identifier.isEmpty()) {
                "$token"
            }
            else {
                "$token($identifier)"
            }
        }
        is BibtexTag -> {
            element.text
        }
        else -> ""
    } ?: ""

    override fun acceptElement(element: PsiElement) = when (element) {
        is BibtexEntry -> true
        is BibtexTag -> true
        else -> false
    }
}