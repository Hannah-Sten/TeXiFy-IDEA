package nl.rubensten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 * @author Sten Wessel
 */
class LatexDocumentationProvider : DocumentationProvider {

    override fun getQuickNavigateInfo(psiElement: PsiElement, originalElement: PsiElement) = when (psiElement) {
        is LatexCommands -> LabelDeclarationLabel(psiElement).makeLabel()
        is BibtexId -> IdDeclarationLabel(psiElement).makeLabel()
        else -> null
    }

    override fun getUrlFor(psiElement: PsiElement, originalElement: PsiElement) = null

    override fun generateDoc(psiElement: PsiElement, originalElement: PsiElement?) = null

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager, o: Any, psiElement: PsiElement) = null

    override fun getDocumentationElementForLink(psiManager: PsiManager, s: String, psiElement: PsiElement) = null
}
