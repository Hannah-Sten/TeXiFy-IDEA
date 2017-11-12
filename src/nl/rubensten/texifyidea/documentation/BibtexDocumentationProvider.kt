package nl.rubensten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.lang.Described
import nl.rubensten.texifyidea.psi.BibtexKey

/**
 * @author Ruben Schellekens
 */
open class BibtexDocumentationProvider : DocumentationProvider {

    /**
     * The currently active lookup item.
     */
    private var lookup: Described? = null

    override fun getUrlFor(p0: PsiElement?, p1: PsiElement?): MutableList<String> {
        return ArrayList()
    }

    override fun getQuickNavigateInfo(element: PsiElement, originalPsiElement: PsiElement): String? {
        return when (element) {
            is BibtexKey -> StringDeclarationLabel(element).makeLabel()
            else -> null
        }
    }

    override fun getDocumentationElementForLookupItem(manager: PsiManager?, obj: Any?, element: PsiElement?): PsiElement? {
        if (obj == null || obj !is Described) {
            return null
        }

        lookup = obj
        return element
    }

    override fun generateDoc(element: PsiElement, element2: PsiElement?): String? {
        return lookup?.description
    }

    override fun getDocumentationElementForLink(manager: PsiManager, string: String, element: PsiElement): PsiElement? {
        return null
    }
}