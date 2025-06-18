package nl.hannahsten.texifyidea.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiReference
import com.intellij.util.IncorrectOperationException
import nl.hannahsten.texifyidea.reference.LatexEnvironmentReference
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

abstract class LatexEnvIdentifierImplMixin(node: ASTNode) : LatexEnvIdentifier, ASTWrapperPsiElement(node)  {

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun setName(name: String): PsiElement? {
        if(!VALID_IDENTIFIERS_REGEX.matches(name)){
            throw IncorrectOperationException("Invalid identifier: $name.")
        }
        val newElement = LatexPsiHelper(this.project).createFromText(name).firstChild
        val oldNode = this.node
        val newNode = newElement.node
        this.parent.node.replaceChild(oldNode, newNode)

        return newNode.psi
    }

    override fun getName(): String? {
        return text
    }

    override fun getReference(): PsiReference? {
        if(this.firstParentOfType<LatexEndCommand>(3) !=null){
            return LatexEnvironmentReference(this)
        }
        return null
    }


    companion object{
        val VALID_IDENTIFIERS_REGEX = Regex("[a-zA-Z]+")

    }
}