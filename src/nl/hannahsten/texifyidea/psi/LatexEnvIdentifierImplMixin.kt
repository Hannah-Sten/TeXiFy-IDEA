package nl.hannahsten.texifyidea.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.IncorrectOperationException
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.reference.LatexEnvironmentBeginEndReference
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

abstract class LatexEnvIdentifierImplMixin(node: ASTNode) : LatexEnvIdentifier, ASTWrapperPsiElement(node) {

    override fun getNameIdentifier(): PsiElement? = this

    override fun setName(name: String): PsiElement? {
        if(!VALID_IDENTIFIERS_REGEX.matches(name)) {
            throw IncorrectOperationException("Invalid identifier: $name.")
        }
        // file - content - no_math_content - normal_text - normal_text_word
        val rootFile = LatexPsiHelper(this.project).createFromText(name)
        val newNormalText = (rootFile as LatexFile).findFirstChildTyped<LatexNormalText>()
        require(newNormalText != null) {
            "Expected NORMAL_TEXT_WORD, but got null for $name."
        }
        val oldNode = envIdentifierText!!.node
        val newNode = newNormalText.node
        this.node.replaceChild(oldNode, newNode)

        return this
    }

    override fun getName(): String? = this.envIdentifierText?.text

    override fun getReference(): PsiReference? {
        // The environment's definition is resolved by LatexBeginCommand
        // so that we can distinguish from the block or the definition
        if(this.firstParentOfType<LatexEndCommand>(3) != null) {
            return LatexEnvironmentBeginEndReference(this, toBegin = true)
        }
        if(this.firstParentOfType<LatexBeginCommand>(3) != null) {
            return LatexEnvironmentBeginEndReference(this, toBegin = false)
        }

        return null
    }

    companion object {
        // See env_identifier_text in parser
        val VALID_IDENTIFIERS_REGEX = Regex("[a-zA-Z!&|(),=<>]+\\*?")
    }
}