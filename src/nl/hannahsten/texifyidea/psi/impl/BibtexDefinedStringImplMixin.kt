package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.psi.BibtexDefinedString
import nl.hannahsten.texifyidea.reference.BibtexStringReference

abstract class BibtexDefinedStringImplMixin(node: ASTNode) : BibtexDefinedString, ASTWrapperPsiElement(node) {
    /**
     * Get a reference to the declaration of the string variable.
     */
    override fun getReference(): PsiReference {
        return BibtexStringReference(this)
    }

}