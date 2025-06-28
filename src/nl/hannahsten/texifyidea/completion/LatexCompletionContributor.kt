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
import nl.hannahsten.texifyidea.lang.LatexMode
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.lang.commands.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.ColorMagic
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.CommentMagic
import nl.hannahsten.texifyidea.util.parser.*
import java.util.*

/**
 * This class registers some completion contributors. For labels we currently use reference completion instead of
 * contributor-based completion, in [nl.hannahsten.texifyidea.reference.LatexLabelReference],
 * though at the moment I don't see a reason why this is the case.
 * Also see https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/code_completion.html
 *
 * @author Sten Wessel, Hannah Schellekens
 */
open class LatexCompletionContributor : CompletionContributor() {

    init {
        registerRegularCommandCompletion()
        registerMathModeCompletion()
        registerFileNameCompletion()
        registerFolderNameCompletion()
        registerGraphicPathCompletion()
        registerColorCompletion()
        registerMagicCommentCompletion()
        registerBibliographyReferenceCompletion()
        registerPackageNameCompletion()
        registerGlossariesCompletion()
        registerDocumentClassCompletion()
        registerLatexArgumentTypeCompletion()
        registerDefaultEnvironmentCompletion()
    }

    /**
     * Adds the commands outside math context to the autocomplete.
     */
    private fun registerRegularCommandCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
            .andNot(PlatformPatterns.psiElement().inside(LatexMathEnvironment::class.java))
            .withPattern { psiElement, _ -> psiElement.inMathContext().not() }
            .withLanguage(LatexLanguage),
        LatexCommandsAndEnvironmentsCompletionProvider(LatexMode.NORMAL)
    )

    /**
     * Adds the commands inside math context to the autocomplete.
     */
    private fun registerMathModeCompletion() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
                .withPattern { psiElement, _ -> psiElement.inMathContext() }
                .withLanguage(LatexLanguage),
            LatexCommandsAndEnvironmentsCompletionProvider(LatexMode.MATH)
        )

        registerMathModeInsideEnvironmentCompletion()
    }

    private fun registerMathModeInsideEnvironmentCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement(LatexTypes.COMMAND_TOKEN)
            .inside(LatexMathEnvironment::class.java)
            .withLanguage(LatexLanguage),
        LatexCommandsAndEnvironmentsCompletionProvider(LatexMode.MATH)
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
        LatexFileProvider()
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
        LatexGraphicsPathProvider()
    )

    /**
     * Adds color support to the autocomplete.
     */
    private fun registerColorCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexRequiredParam::class.java)
            .withPattern("xcolor color completion pattern") { psiElement, _ ->
                val command = getParentOfType(psiElement, LatexCommands::class.java) ?: return@withPattern false
                val name = command.commandToken.text
                name.substring(1) in ColorMagic.takeColorCommands
            },
        LatexXColorProvider
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
     * Adds support for bibliography references to the autocomplete.
     */
    private fun registerBibliographyReferenceCompletion() {
        extendLatexCommands(LatexBibliographyReferenceProvider, CommandMagic.bibliographyReference)
    }

    /**
     * Adds support for package names to the autocomplete.
     */
    private fun registerPackageNameCompletion() {
        extendLatexCommands(LatexPackageNameProvider, "\\usepackage", "\\RequirePackage")
    }

    private fun registerGlossariesCompletion() {
        extendLatexCommands(LatexGlossariesCompletionProvider, CommandMagic.glossaryReference)
    }

    /**
     * Adds support for document classes to the autocomplete.
     */
    private fun registerDocumentClassCompletion() = extendLatexCommands(
        LatexDocumentclassProvider,
        "\\documentclass", "\\LoadClass", "\\LoadClassWithOptions"
    )

    /**
     * Adds autocompletion for parameters that have been given a default [Argument.Type].
     * When the type contains a completion contributor, it will be registered to the correct6 argument.
     */
    private fun registerLatexArgumentTypeCompletion() = Argument.Type.entries.forEach { type ->
        val completionProvider = type.completionProvider ?: return@forEach
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(LatexParameterText::class.java)
                .withPattern { psiElement, _ ->
                    val parameter = psiElement.parentOfType(LatexParameter::class) ?: return@withPattern false
                    val psiCommand = parameter.parentOfType(LatexCommands::class) ?: return@withPattern false
                    val command = psiCommand.defaultCommand() ?: return@withPattern false
                    val index = parameter.indexOf() ?: return@withPattern false
                    val argument = command.arguments.getOrNull(index) ?: return@withPattern false
                    argument.type == type
                }
                .withLanguage(LatexLanguage),
            completionProvider
        )
    }

    /**
     * Adds default environment names to the autocompletion.
     */
    private fun registerDefaultEnvironmentCompletion() = extend(
        CompletionType.BASIC,
        PlatformPatterns.psiElement()
            .inside(LatexEnvIdentifier::class.java)
            .inside(PlatformPatterns.or(PlatformPatterns.psiElement(LatexBeginCommand::class.java), PlatformPatterns.psiElement(LatexEndCommand::class.java)))
            .withLanguage(LatexLanguage),
        LatexCommandsAndEnvironmentsCompletionProvider(LatexMode.ENVIRONMENT_NAME)
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
                if (command.commandToken.text in commandNamesWithSlash) return@withPattern true

                CommandManager.updateAliases(commandNamesWithSlash, psiElement.project)
                CommandManager.getAliases(command.commandToken.text).intersect(commandNamesWithSlash).isNotEmpty()
            }
            .withLanguage(LatexLanguage),
        provider
    )
}