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
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFileProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFolderProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
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
                                val command = LatexPsiUtil.getParentOfType(psiElement, LatexCommands::class.java)
                                        ?: return false

                                val name = command.commandToken.text
                                val cmd = LatexRegularCommand[name.substring(1)] ?: return false

                                val args = cmd.first().getArgumentsOf(RequiredFileArgument::class)
                                if (args.isNotEmpty()) processingContext.put("type", args.first())

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

                                val args = cmd.first().getArgumentsOf(RequiredFolderArgument::class)
                                if (args.isNotEmpty()) processingContext.put("type", args.first())

                                return args.isNotEmpty()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexFolderProvider()
        )

        // Graphics paths
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

                                val args = cmd.first().getArgumentsOf(RequiredPicturePathArgument::class)
                                if (args.isNotEmpty()) processingContext.put("type", args.first())

                                return args.isNotEmpty()
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexGraphicsPathProvider()
        )

        // Colors from xcolor
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(LatexRequiredParam::class.java)
                        .with(object : PatternCondition<PsiElement>("xcolor color completion patter") {
                            override fun accepts(psiElement: PsiElement, context: ProcessingContext?): Boolean {
                                val command = LatexPsiUtil.getParentOfType(psiElement, LatexCommands::class.java)
                                        ?: return false

                                val name = command.commandToken.text
                                return name.substring(1) in Magic.Colors.takeColorCommands
                            }
                        }),
                LatexXColorProvider
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

        // List containing tikz/math to autocomplete the begin/end/preamble values in magic comments.
        val beginEndRegex = Regex("""(begin|end|preview) preamble\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(PsiComment::class.java)
                        .with(object : PatternCondition<PsiElement>("Magic comment preamble pattern") {
                            override fun accepts(comment: PsiElement, context: ProcessingContext?): Boolean {
                                return comment.isMagicComment() && comment.text.contains(beginEndRegex)
                            }
                        })
                        .withLanguage(LatexLanguage.INSTANCE),
                LatexMagicCommentValueProvider(Magic.Comment.preambleValues)
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
