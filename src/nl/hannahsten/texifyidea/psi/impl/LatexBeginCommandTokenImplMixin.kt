package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.psi.LatexBeginTokenPsi
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.reference.LatexEnvironmentDefinitionReference
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

//abstract class LatexBeginCommandTokenImplMixin(node: ASTNode) : LatexBeginTokenPsi, ASTWrapperPsiElement(node) {
//
//    override fun getReference(): PsiReference? {
//        return LatexEnvironmentDefinitionReference(this, firstParentOfType<LatexEnvironment>() ?: return null)
//    }
//}