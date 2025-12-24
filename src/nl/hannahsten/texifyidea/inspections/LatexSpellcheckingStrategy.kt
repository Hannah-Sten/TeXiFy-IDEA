package nl.hannahsten.texifyidea.inspections

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.lang.LArgument
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.hasParent
import nl.hannahsten.texifyidea.util.parser.lookupCommandPsi

/**
 * @author Hannah Schellekens
 */
class LatexSpellcheckingStrategy : SpellcheckingStrategy() {

    override fun isMyContext(psiElement: PsiElement): Boolean = psiElement.language == LatexLanguage

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

        if (argument?.contextSignature?.introduces(LatexContexts.Text) == true) {
            return hasLiteralsScope
        }

        // Comments
        if (latexParserDefinition.commentTokens.contains(elementType)) {
            return scope.contains(SpellCheckingInspection.SpellCheckingScope.Comments)
        }

        // Code
        if (scope.contains(SpellCheckingInspection.SpellCheckingScope.Code)) {
            // Override the default to disable spellchecking for code unless enabled in TeXiFy settings
            return TexifySettings.getState().enableSpellcheckEverywhere
        }

        return false
    }

    private fun isBeginEnd(element: PsiElement): Boolean = element.parentOfType<LatexEndCommand>() != null || element.parentOfType<LatexBeginCommand>() != null

    /**
     * Get the argument (with type) from TeXiFy knowledge that corresponds with the current psi element.
     */
    private fun getArgument(leaf: LeafPsiElement): LArgument? {
        val parameter = leaf.parentOfType<LatexParameter>() ?: return null
        val parent = parameter.parentOfType<LatexCommands>() ?: return null
        val arguments = AllPredefined.lookupCommandPsi(parent)?.arguments ?: return null

        parameter.requiredParam?.let { requiredParam ->
            val idx = parent.indexOfRequiredParameter { it === requiredParam }
            LArgument.getRequiredByIdx(arguments, idx)?.let {
                return it
            }
        }

        // Note that a leaf may be only part of a parameter
        val parameterText = leaf.firstParentOfType(LatexParameterText::class)?.text
        val optionalArguments = arguments.filter { it.isOptional }
        // Also check optional arguments, if not key-value pairs it may contain text
        val optionalParamIndex = parent.getOptionalParameterMap().map { it.key.text }.indexOf(parameterText)
        return if (optionalParamIndex >= 0 && optionalParamIndex < optionalArguments.size) {
            optionalArguments[optionalParamIndex]
        }
        else {
            null
        }
    }
}
