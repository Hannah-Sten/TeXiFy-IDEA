package nl.hannahsten.texifyidea.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.Argument.Type
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.psi.firstParentOfType
import nl.hannahsten.texifyidea.util.psi.hasParent

/**
 * @author Hannah Schellekens
 */
class LatexSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun isMyContext(psiElement: PsiElement): Boolean {
        return psiElement.language == LatexLanguage
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
        if (
            argument == null &&
            psiElement.elementType == LatexTypes.NORMAL_TEXT_WORD &&
            // Exclude text in parameters by default, unless we know it contains text (e.g. \section)
            !psiElement.hasParent(LatexParameterText::class)
        ) {
            return TEXT_TOKENIZER
        }

        if (argument?.type == Type.TEXT) {
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

    /**
     * Get the argument (with type) from TeXiFy knowledge that corresponds with the current psi element.
     */
    private fun getArgument(leaf: LeafPsiElement): Argument? {
        val parent = PsiTreeUtil.getParentOfType(leaf, LatexCommands::class.java) ?: return null

        val arguments = getArguments(parent.commandToken.text.substring(1)) ?: return null

        val realParams = parent.getRequiredParameters()
        // Note that a leaf may be only part of a parameter
        val parameterIndex = realParams.indexOf(leaf.firstParentOfType(LatexParameterText::class)?.text)
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
