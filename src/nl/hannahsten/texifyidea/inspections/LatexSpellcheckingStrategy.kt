package nl.hannahsten.texifyidea.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.Argument.Type
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.hasParent

/**
 * @author Hannah Schellekens
 */
class LatexSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun isMyContext(psiElement: PsiElement): Boolean {
        return psiElement.language == LatexLanguage.INSTANCE
    }

    override fun getTokenizer(psiElement: PsiElement): Tokenizer<*> {
        if (psiElement !is LeafPsiElement) {
            return EMPTY_TOKENIZER
        }

        if (psiElement.elementType == LatexTypes.COMMAND_TOKEN ||
            psiElement.elementType == LatexTypes.COMMAND_IFNEXTCHAR ||
            psiElement.elementType == LatexTypes.COMMENT_TOKEN ||
            isBeginEnd(psiElement) ||
            psiElement.hasParent(LatexOptionalParam::class)
        ) {
            return EMPTY_TOKENIZER
        }

        val argument = getArgument(psiElement)
        if (argument == null && psiElement.elementType == LatexTypes.NORMAL_TEXT) {
            return TEXT_TOKENIZER
        }

        if (argument == null || argument.type == Type.TEXT) {
            return TEXT_TOKENIZER
        }

        return EMPTY_TOKENIZER
    }

    private fun isBeginEnd(element: PsiElement): Boolean {
        var elt: PsiElement? = PsiTreeUtil.getParentOfType(element, LatexBeginCommand::class.java)
        if (elt != null) {
            return true
        }

        elt = PsiTreeUtil.getParentOfType(element, LatexEndCommand::class.java)
        return elt != null
    }

    private fun getArgument(leaf: LeafPsiElement): Argument? {
        val parent = PsiTreeUtil.getParentOfType(leaf, LatexCommands::class.java) ?: return null

        val arguments = getArguments(parent.commandToken.text.substring(1)) ?: return null

        val realParams = parent.requiredParameters
        val parameterIndex = realParams.indexOf(leaf.text)
        return if (parameterIndex < 0 || parameterIndex >= arguments.size) {
            null
        }
        else arguments[parameterIndex]
    }

    private fun getArguments(commandName: String): Array<out Argument>? {
        val cmdHuh = LatexRegularCommand[commandName]
        if (cmdHuh != null) {
            return cmdHuh.first().arguments
        }

        val mathCmdHuh = LatexMathCommand[commandName] ?: return null

        return mathCmdHuh.first().arguments
    }
}
