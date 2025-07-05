package nl.hannahsten.texifyidea.inspections

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.lang.commands.Argument.Type
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.hasParent

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
            psiElement.elementType == LatexTypes.LEFT ||
            psiElement.elementType == LatexTypes.RIGHT ||
            isBeginEnd(psiElement)
        ) {
            return EMPTY_TOKENIZER
        }

        // Allow comments to be spellchecked, depending on inspection settings
        if (psiElement.elementType == LatexTypes.COMMENT_TOKEN) {
            return TEXT_TOKENIZER
        }

        return TEXT_TOKENIZER
    }

    /**
     * We need more fine-grained control than just the element types provided in LatexParserDefinition.
     *
     * Literals: normal text and arguments with a known text type, should be on by default
     * Code: anything else that could make sense to spellcheck, should be off by default
     */
    override fun elementFitsScope(psiElement: PsiElement, scope: Set<SpellCheckingInspection.SpellCheckingScope?>?): Boolean {
        if (scope == null || psiElement !is LeafPsiElement) {
            return super.elementFitsScope(psiElement, scope)
        }

        val elementType = psiElement.node.elementType
        val latexParserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(LatexLanguage)

        // Literals
        val hasLiteralsScope = scope.contains(SpellCheckingInspection.SpellCheckingScope.Literals)
        val argument = getArgument(psiElement)
        // Normal text
        if (
            argument == null &&
            psiElement.elementType == LatexTypes.NORMAL_TEXT_WORD &&
            // Exclude text in parameters by default, unless we know it contains text (e.g. \section)
            !psiElement.hasParent(LatexParameterText::class)
        ) {
            return hasLiteralsScope
        }

        if (argument?.type == Type.TEXT) {
            return hasLiteralsScope
        }

        // Comments
        if (latexParserDefinition.commentTokens.contains(elementType)) {
            return scope.contains(SpellCheckingInspection.SpellCheckingScope.Comments)
        }

        // Code
        if (scope.contains(SpellCheckingInspection.SpellCheckingScope.Code)) {
            // Override the default to disable spellchecking for code unless enabled in TeXiFy settings
            return TexifySettings.getInstance().enableSpellcheckEverywhere
        }

        return false
    }

    private fun isBeginEnd(element: PsiElement): Boolean {
        return element.parentOfType<LatexEndCommand>() != null || element.parentOfType<LatexBeginCommand>() != null
    }

    /**
     * Get the argument (with type) from TeXiFy knowledge that corresponds with the current psi element.
     */
    private fun getArgument(leaf: LeafPsiElement): Argument? {
        val parent = leaf.parentOfType<LatexCommands>() ?: return null

        val arguments = getArguments(parent.name?.substring(1) ?: return null) ?: return null
        val requiredArguments = arguments.filterIsInstance<RequiredArgument>()
        val optionalArguments = arguments.filterIsInstance<OptionalArgument>()

        val requiredParams = parent.requiredParametersText()
        // Note that a leaf may be only part of a parameter
        val parameterText = leaf.firstParentOfType(LatexParameterText::class)?.text
        val parameterIndex = requiredParams.indexOf(parameterText)
        if (parameterIndex >= 0 && parameterIndex < requiredArguments.size) {
            return arguments[parameterIndex]
        }

        // Also check optional arguments, if not key-value pairs it may contain text
        val optionalParamIndex = parent.getOptionalParameterMap().map { it.key.text }.indexOf(parameterText)
        return if (optionalParamIndex >= 0 && optionalParamIndex < optionalArguments.size) {
            optionalArguments[optionalParamIndex]
        }
        else {
            null
        }
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
