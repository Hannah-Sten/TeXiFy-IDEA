package nl.hannahsten.texifyidea.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.reference.LatexEnvironmentBeginReference
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

abstract class LatexEnvIdentifierImplMixin(node: ASTNode) : LatexEnvIdentifier, ASTWrapperPsiElement(node) {

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun setName(name: String): PsiElement? {
        if(!VALID_IDENTIFIERS_REGEX.matches(name)) {
            throw IncorrectOperationException("Invalid identifier: $name.")
        }
        // file - content - no_math_content - normal_text - normal_text_word
        val rootFile = LatexPsiHelper(this.project).createFromText(name)
        val newNormalText = (rootFile as LatexFile).findFirstChildTyped<LatexNormalText>()
        val newNormalTextWord = newNormalText?.firstChild
        require(newNormalTextWord != null && newNormalTextWord.elementType == LatexTypes.NORMAL_TEXT_WORD) {
            "Expected NORMAL_TEXT_WORD, but got ${newNormalTextWord.elementType}."
        }
        val oldNode = normalTextWord!!.node
        val newNode = newNormalTextWord.node
        this.node.replaceChild(oldNode, newNode)

        return this
    }

    override fun getName(): String? {
        return this.normalTextWord?.text
    }

    override fun getReference(): PsiReference? {
        // The environment's definition is resolved by LatexBeginCommand
        // so that we can distinguish from the block or the definition
        if(this.firstParentOfType<LatexEndCommand>(3) != null) {
            return LatexEnvironmentBeginReference(this)
        }
        return null
    }

    companion object {
        val VALID_IDENTIFIERS_REGEX = Regex("[a-zA-Z]+\\*?")
    }
}