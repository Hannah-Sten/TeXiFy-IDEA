package nl.rubensten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import nl.rubensten.texifyidea.BibtexLanguage
import nl.rubensten.texifyidea.LatexLanguage
import nl.rubensten.texifyidea.lang.LatexMode
import nl.rubensten.texifyidea.lang.LatexNoMathCommand
import nl.rubensten.texifyidea.lang.RequiredFileArgument
import nl.rubensten.texifyidea.psi.*
import nl.rubensten.texifyidea.util.firstChildOfType
import nl.rubensten.texifyidea.util.hasParent
import nl.rubensten.texifyidea.util.inMathContext
import nl.rubensten.texifyidea.util.parentOfType

/**
 * @author Sten Wessel, Ruben Schellekens
 */
open class TexifyCompletionContributor : CompletionContributor() {

    init {
        registerLatexCompletion()
        registerBibtexCompletion()
    }

    private fun registerLatexCompletion() {
        // Math mode
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                        .inside(LatexMathEnvironment::class.java)
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexCommandProvider(LatexMode.MATH)
        )

        // Math mode inside environments
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                        .with(object : PatternCondition<PsiElement>(null) {
                            override fun accepts(psiElement: PsiElement, processingContext: ProcessingContext): Boolean {
                                return psiElement.inMathContext()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexCommandProvider(LatexMode.MATH)
        )

        // Normal
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                        .andNot(PlatformPatterns.psiElement()
                                .inside(LatexMathEnvironment::class.java))
                        .with(object : PatternCondition<PsiElement>(null) {
                            override fun accepts(psiElement: PsiElement, processingContext: ProcessingContext): Boolean {
                                return !psiElement.inMathContext()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexCommandProvider(LatexMode.NORMAL)
        )

        // DefaultEnvironment names
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .inside(LatexRequiredParam::class.java)
                        .inside(LatexBeginCommand::class.java)
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexCommandProvider(LatexMode.ENVIRONMENT_NAME)
        )

        // File names
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(LatexNormalText::class.java)
                        .inside(LatexRequiredParam::class.java)
                        .with(object : PatternCondition<PsiElement>(null) {
                            override fun accepts(psiElement: PsiElement, processingContext: ProcessingContext): Boolean {
                                val command = LatexPsiUtil.getParentOfType(psiElement, LatexCommands::class.java) ?: return false

                                val name = command.commandToken.text
                                val cmd = LatexNoMathCommand[name.substring(1)] ?: return false

                                val args = cmd.getArgumentsOf(RequiredFileArgument::class)
                                return !args.isEmpty()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexFileProvider()
        )

        // Package names
        extendLatexCommands(LatexPackageNameProvider, "\\usepackage", "\\RequirePackage")

        // Bibliography styles
        extendLatexCommand(LatexBibliographyStyleProvider, "\\bibliographystyle")
    }

    private fun registerBibtexCompletion() {
        // Outer scope: types.
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(BibtexTypes.TYPE_TOKEN)
                        .andNot(PlatformPatterns.psiElement().inside(BibtexEntry::class.java))
                        .withLanguage(BibtexLanguage),
                BibtexTypeTokenProvider
        )

        // Keys
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(BibtexTypes.IDENTIFIER)
                        .inside(BibtexEntry::class.java)
                        .with(object : PatternCondition<PsiElement>(null) {
                            override fun accepts(psiElement: PsiElement, context: ProcessingContext): Boolean {
                                val entry = psiElement.parentOfType(BibtexEntry::class)
                                val type = entry?.firstChildOfType(BibtexType::class)
                                if (type?.text?.toLowerCase() == "@string") {
                                    return false
                                }

                                return psiElement.hasParent(BibtexEndtry::class) || psiElement.hasParent(BibtexKey::class)
                            }
                        })
                        .withLanguage(BibtexLanguage),
                BibtexKeyProvider
        )

        // Strings
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(BibtexTypes.IDENTIFIER)
                        .inside(BibtexEntry::class.java)
                        .inside(BibtexContent::class.java)
                        .withLanguage(BibtexLanguage),
                BibtexStringProvider
        )
    }

    /**
     * Adds a completion contributor that gets activated within the first required parameter of a given command.
     */
    private fun extendLatexCommand(provider: CompletionProvider<CompletionParameters>, commandNameWithSlash: String) {
        extendLatexCommands(provider, setOf(commandNameWithSlash))
    }

    /**
     * Adds a completion contributor that gets activated within the first required parameter of a given set of commands.
     */
    private fun extendLatexCommands(provider: CompletionProvider<CompletionParameters>, vararg commandNamesWithSlash: String) {
        extendLatexCommands(provider, setOf(*commandNamesWithSlash))
    }

    /**
     * Adds a completion contributor that gets activated within the first required parameter of a given set of commands.
     */
    private fun extendLatexCommands(provider: CompletionProvider<CompletionParameters>, commandNamesWithSlash: Set<String>) {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(LatexNormalText::class.java)
                        .inside(LatexRequiredParam::class.java)
                        .with(object : PatternCondition<PsiElement>(null) {
                            override fun accepts(psiElement: PsiElement, context: ProcessingContext): Boolean {
                                val command = psiElement.parentOfType(LatexCommands::class) ?: return false
                                return command.commandToken.text in commandNamesWithSlash
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                provider
        )
    }
}
