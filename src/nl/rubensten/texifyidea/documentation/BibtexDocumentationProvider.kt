package nl.rubensten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.lang.BibtexEntryType
import nl.rubensten.texifyidea.psi.BibtexDefinedString
import nl.rubensten.texifyidea.psi.BibtexKey

/**
 * @author Ruben Schellekens
 */
open class BibtexDocumentationProvider : DocumentationProvider {

    private var entryType: BibtexEntryType? = null

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
        if (obj is BibtexEntryType) {
            entryType = obj
        }

        val file = element?.containingFile
        if (file != null) {
            BibtexIdIndex.getIndexedIds(file).map(PsiElement::getText).forEach(::println)
        }

        return element
    }

    override fun generateDoc(element: PsiElement, element2: PsiElement?): String? {
        return entryType?.description
    }

    override fun getDocumentationElementForLink(manager: PsiManager, string: String, element: PsiElement): PsiElement? {
        return null
    }
}