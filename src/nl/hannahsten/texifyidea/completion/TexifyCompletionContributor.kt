package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.lang.LatexMode
import nl.hannahsten.texifyidea.lang.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.RequiredFileArgument
import nl.hannahsten.texifyidea.lang.RequiredFolderArgument
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import java.util.*

/**
 * @author Sten Wessel, Hannah Schellekens
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
                        .with(object : PatternCondition<PsiElement>("File name completion pattern") {
                            override fun accepts(psiElement: PsiElement, processingContext: ProcessingContext): Boolean {
                                val command = LatexPsiUtil.getParentOfType(psiElement, LatexCommands::class.java) ?: return false

                                val name = command.commandToken.text
                                val cmd = LatexRegularCommand[name.substring(1)] ?: return false

                                val args = cmd.getArgumentsOf(RequiredFileArgument::class)
                                return args.isNotEmpty()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexFileProvider()
        )

        // Folder names
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(LatexNormalText::class.java)
                        .inside(LatexRequiredParam::class.java)
                        .with(object : PatternCondition<PsiElement>("Folder name completion pattern") {
                            override fun accepts(psiElement: PsiElement, processingContext: ProcessingContext): Boolean {
                                val command = LatexPsiUtil.getParentOfType(psiElement, LatexCommands::class.java)
                                        ?: return false

                                val name = command.commandToken.text
                                val cmd = LatexRegularCommand[name.substring(1)] ?: return false

                                val args = cmd.getArgumentsOf(RequiredFolderArgument::class)
                                return args.isNotEmpty()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexFolderProvider()
        )

        // Magic comments keys.
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(PsiComment::class.java)
                        .with(object : PatternCondition<PsiElement>("Magic comment completion pattern") {
                            override fun accepts(comment: PsiElement, context: ProcessingContext?): Boolean {
                                return comment.isMagicComment() && comment.text.contains('=').not()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexMagicCommentKeyProvider
        )

        extendLatexCommands(LatexBibliographyReferenceProvider, Magic.Command.bibliographyReference)

        // Inspection list for magic comment suppress.
        val suppressRegex = Regex("""suppress\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(PsiComment::class.java)
                        .with(object : PatternCondition<PsiElement>("Magic comment suppress pattern") {
                            override fun accepts(comment: PsiElement, context: ProcessingContext?): Boolean {
                                return comment.isMagicComment() && comment.text.contains(suppressRegex)
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexInspectionIdProvider
        )

        // Package names
        extendLatexCommands(LatexPackageNameProvider, "\\usepackage", "\\RequirePackage")

        // Documentclasses
        extendLatexCommands(LatexDocumentclassProvider,
                "\\documentclass", "\\LoadClass", "\\LoadClassWithOptions"
        )

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
