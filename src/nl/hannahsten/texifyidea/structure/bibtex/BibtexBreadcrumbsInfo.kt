package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.BibtexKey
import nl.hannahsten.texifyidea.psi.BibtexTag
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.substringEnd
import nl.hannahsten.texifyidea.util.tokenType
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class BibtexBreadcrumbsInfo : BreadcrumbsProvider {

    override fun getLanguages() = arrayOf(BibtexLanguage)

    override fun getElementInfo(element: PsiElement) = when (element) {
        is BibtexEntry -> {
            val token = element.tokenType()
            val identifier = when (token.lowercase(Locale.getDefault())) {
                "@preamble" -> ""
                "@string" -> element.findFirstChildOfType(BibtexKey::class)?.text
                else -> element.findFirstChildOfType(BibtexId::class)?.text?.substringEnd(1)
            } ?: ""

            if (identifier.isEmpty()) {
                token
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