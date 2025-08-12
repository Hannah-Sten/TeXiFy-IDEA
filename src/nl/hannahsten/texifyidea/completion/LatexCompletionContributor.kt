package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFileProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFolderProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.inspections.ALL_TEXIFY_INSPECTIONS
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.util.magic.CommentMagic
import nl.hannahsten.texifyidea.util.parser.*
import java.util.*

/**
 * This class registers some completion contributors.
 *
 *
 * Also see https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/code_completion.html
 *
 * @author Sten Wessel, Hannah Schellekens
 */
class LatexCompletionContributor : CompletionContributor() {

    init {
        registerContextBasedCommandCompletion()
        registerContextAwareParameterCompletion()
        registerFileNameCompletion()
        registerFolderNameCompletion()
        registerGraphicPathCompletion()
        registerMagicCommentCompletion()
        registerDefaultEnvironmentCompletion()
    }

    /**
     * Adds the commands outside math context to the autocomplete.
     */
    private fun registerContextBasedCommandCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
            .withLanguage(LatexLanguage),
        ContextAwareCommandCompletionProvider
    )

    /**
     * Contains lots of context-aware completion providers that are activated for particular contexts by the dispatcher.
     *
     * The context is computed once and passed to all providers, making it efficient.
     */
    private fun registerContextAwareParameterCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexParameterText::class.java)
            .withLanguage(LatexLanguage),
        ContextAwareCompletionProviderDispatcher
    )

    /**
     * Adds file name support to the autocomplete.
     */
    private fun registerFileNameCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexParameterText::class.java)
            .inside(LatexRequiredParam::class.java)
            .withPattern("File name completion pattern") { psiElement, processingContext ->
                val command = getParentOfType(psiElement, LatexCommands::class.java) ?: return@withPattern false
                val name = command.name ?: return@withPattern false
                val cmd = LatexRegularCommand[name.substring(1)] ?: return@withPattern false

                val args = cmd.first().getArgumentsOf(RequiredFileArgument::class)
                if (args.isNotEmpty()) processingContext?.put("type", args.first())
                args.isNotEmpty()
            }
            .withLanguage(LatexLanguage),
        LatexFileProvider
    )

    /**
     * Adds folder name support to the autocomplete.
     */
    private fun registerFolderNameCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexRequiredParam::class.java)
            .withPattern("Folder name completion pattern") { psiElement, processingContext ->
                val command = getParentOfType(psiElement, LatexCommands::class.java) ?: return@withPattern false
                val name = command.name ?: return@withPattern false
                val cmd = LatexRegularCommand[name.substring(1)] ?: return@withPattern false

                val args = cmd.first().getArgumentsOf(RequiredFolderArgument::class)
                if (args.isNotEmpty()) processingContext?.put("type", args.first())
                args.isNotEmpty()
            }
            .withLanguage(LatexLanguage),
        LatexFolderProvider()
    )

    /**
     * Adds graphics path support to the autocomplete.
     */
    private fun registerGraphicPathCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexParameterText::class.java)
            .inside(LatexRequiredParam::class.java)
            .withPattern("Folder name completion pattern") { psiElement, processingContext ->
                val command = getParentOfType(psiElement, LatexCommands::class.java) ?: return@withPattern false
                val name = command.commandToken.text
                val cmd = LatexRegularCommand[name.substring(1)] ?: return@withPattern false

                val args = cmd.first().getArgumentsOf(RequiredPicturePathArgument::class)
                if (args.isNotEmpty()) processingContext?.put("type", args.first())
                args.isNotEmpty()
            }
            .withLanguage(LatexLanguage),
        LatexGraphicsPathProvider
    )

    /**
     * Adds autocomplete functionality for magic comments.
     */
    private fun registerMagicCommentCompletion() {
        registerMagicCommentKeyCompletion()
        registerMagicCommentValueCompletion()
    }

    /**
     * Adds all available magic comment keys to the autocomple.
     */
    private fun registerMagicCommentKeyCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexMagicComment::class.java)
            .withPattern("Magic comment completion pattern") { comment, _ ->
                comment.containsMagicComment() && comment.text.contains('=').not()
            }
            .withLanguage(LatexLanguage),
        LatexMagicCommentKeyProvider
    )

    /**
     * Adds all possible magic comment values to the autocomplete.
     */
    private fun registerMagicCommentValueCompletion() {
        registerMagicCommentInspectionCompletion()
        registerMagicCommentTikzMathCompletion()
        registerMagicCommentLatexCompilerCompletion()
        registerMagicCommentBibtexCompilerCompletion()
        registerMagicCommentFakeCompletion()
    }

    /**
     * Adds the inspection list to the autocomplete for magic comment suppress values.
     */
    private fun registerMagicCommentInspectionCompletion() {
        val suppressRegex = Regex("""suppress\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        val inspectionIds = InsightGroup.byFileType(LatexFileType)
            .flatMap { ALL_TEXIFY_INSPECTIONS[it] ?: emptyList() }
            .toHashSet()
        extendMagicCommentValues(
            "suppress",
            suppressRegex,
            LatexMagicCommentValueProvider(suppressRegex, inspectionIds, TexifyIcons.INSPECTION)
        )
    }

    /**
     * Adds the list containing tikz/math to the autocomplete for begin/end/preamble values in magic comments.
     */
    private fun registerMagicCommentTikzMathCompletion() {
        val beginEndRegex = Regex("""(begin|end|preview) preamble\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues(
            "preamble",
            beginEndRegex,
            LatexMagicCommentValueProvider(beginEndRegex, CommentMagic.preambleValues)
        )
    }

    /**
     * Adds the list of LaTeX compilers to the autocomplete for magic comment values.
     */
    private fun registerMagicCommentLatexCompilerCompletion() {
        val compilerRegex = Regex("""compiler\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues(
            "compiler",
            compilerRegex,
            LatexMagicCommentValueProvider(compilerRegex, LatexCompiler.entries.map { it.executableName }.toHashSet())
        )
    }

    /**
     * Adds the list of Bibtex compilers to the autocomplete for magic comment values.
     */
    private fun registerMagicCommentBibtexCompilerCompletion() {
        val bibtexCompilerRegex = Regex("""bibtex compiler\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues(
            "bibtex compiler",
            bibtexCompilerRegex,
            LatexMagicCommentValueProvider(
                bibtexCompilerRegex,
                BibliographyCompiler.entries.map { it.executableName }.toHashSet()
            )
        )
    }

    /**
     * Adds the fake magic comment values to the autocomplete.
     */
    private fun registerMagicCommentFakeCompletion() {
        val fakeRegex = Regex("""fake\s*(=\s*)?""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues(
            "fake",
            fakeRegex,
            LatexMagicCommentValueProvider(fakeRegex, CommentMagic.fakeSectionValues)
        )
    }

    /**
     * Adds a completions contributor that gets activated when typing a value for a magic comment.
     */
    private fun extendMagicCommentValues(
        commentName: String,
        regex: Regex,
        completionProvider: CompletionProvider<CompletionParameters>
    ) = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexMagicComment::class.java)
            .withPattern("Magic comment $commentName pattern") { comment, _ ->
                comment.containsMagicComment() && comment.text.contains(regex)
            }
            .withLanguage(LatexLanguage),
        completionProvider
    )

    /**
     * Adds default environment names to the autocompletion.
     */
    private fun registerDefaultEnvironmentCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexEnvIdentifier::class.java)
            .inside(PlatformPatterns.or(PlatformPatterns.psiElement(LatexBeginCommand::class.java), PlatformPatterns.psiElement(LatexEndCommand::class.java)))
            .withLanguage(LatexLanguage),
        ContextAwareEnvironmentCompletionProvider
    )

    /**
     * Adds a completion contributor that gets activated within the first required parameter of a given set of commands.
     */
    private fun extendLatexCommands(
        provider: CompletionProvider<CompletionParameters>,
        vararg commandNamesWithSlash: String
    ) = extendLatexCommands(provider, setOf(*commandNamesWithSlash))

    /**
     * Adds a completion contributor that gets activated within the first required parameter of a given set of commands.
     */
    private fun extendLatexCommands(
        provider: CompletionProvider<CompletionParameters>,
        commandNamesWithSlash: Set<String>
    ) = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexParameterText::class.java)
            .inside(LatexRequiredParam::class.java)
            .withPattern { psiElement, _ ->
                val command = psiElement.parentOfType(LatexCommands::class) ?: return@withPattern false
                command.name in commandNamesWithSlash
            }
            .withLanguage(LatexLanguage),
        provider
    )
}