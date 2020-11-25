package nl.hannahsten.texifyidea.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.icons.AllIcons
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFileProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexFolderProvider
import nl.hannahsten.texifyidea.completion.pathcompletion.LatexGraphicsPathProvider
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.ALL_TEXIFY_INSPECTIONS
import nl.hannahsten.texifyidea.lang.*
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.run.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.util.*
import java.util.*

/**
 * This class registers some completion contributors. For labels we currently use reference completion instead of
 * contributor-based completion, in [nl.hannahsten.texifyidea.reference.LatexLabelReference],
 * though at the moment I don't see a reason why this is the case.
 * Also see https://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/code_completion.html
 *
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
                .andNot(
                    PlatformPatterns.psiElement()
                        .inside(LatexMathEnvironment::class.java)
                )
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
            PlatformPatterns.psiElement().inside(LatexParameterText::class.java)
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
            PlatformPatterns.psiElement()
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
            PlatformPatterns.psiElement().inside(LatexParameterText::class.java)
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
            PlatformPatterns.psiElement().inside(LatexMagicComment::class.java)
                .with(object : PatternCondition<PsiElement>("Magic comment completion pattern") {
                    override fun accepts(comment: PsiElement, context: ProcessingContext?): Boolean {
                        return comment.isMagicComment() && comment.text.contains('=').not()
                    }
                })
                .withLanguage(LatexLanguage.INSTANCE),
            LatexMagicCommentKeyProvider
        )

        extendLatexCommands(LatexBibliographyReferenceProvider, Magic.Command.bibliographyReference)

        // Inspection list for magic comment suppress
        val suppressRegex = Regex("""suppress\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        val inspectionIds = InsightGroup.byFileType(LatexFileType)
            .flatMap { ALL_TEXIFY_INSPECTIONS[it] ?: emptyList() }
            .toHashSet()
        extendMagicCommentValues("suppress", suppressRegex, LatexMagicCommentValueProvider(suppressRegex, inspectionIds, AllIcons.General.InspectionsEye))

        // List containing tikz/math to autocomplete the begin/end/preamble values in magic comments
        val beginEndRegex = Regex("""(begin|end|preview) preamble\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues("preamble", beginEndRegex, LatexMagicCommentValueProvider(beginEndRegex, Magic.Comment.preambleValues))

        // List of LaTeX compilers
        val compilerRegex = Regex("""compiler\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues("compiler", compilerRegex, LatexMagicCommentValueProvider(compilerRegex, LatexCompiler.values().map { it.executableName }.toHashSet()))

        val bibtexCompilerRegex = Regex("""bibtex compiler\s*=\s*""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues("bibtex compiler", bibtexCompilerRegex, LatexMagicCommentValueProvider(bibtexCompilerRegex, BibliographyCompiler.values().map { it.executableName }.toHashSet()))

        val fakeRegex = Regex("""fake\s*[=\s*]?""", EnumSet.of(RegexOption.IGNORE_CASE))
        extendMagicCommentValues("fake", fakeRegex, LatexMagicCommentValueProvider(fakeRegex, Magic.Comment.fakeSectionValues))

        // Package names
        extendLatexCommands(LatexPackageNameProvider, "\\usepackage", "\\RequirePackage")

        // Documentclasses
        extendLatexCommands(
            LatexDocumentclassProvider,
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
            PlatformPatterns.psiElement().inside(LatexParameterText::class.java)
                .inside(LatexRequiredParam::class.java)
                .with(object : PatternCondition<PsiElement>(null) {
                    override fun accepts(psiElement: PsiElement, context: ProcessingContext): Boolean {
                        val command = psiElement.parentOfType(LatexCommands::class) ?: return false
                        if (command.commandToken.text in commandNamesWithSlash) {
                            return true
                        }

                        CommandManager.updateAliases(commandNamesWithSlash, psiElement.project)
                        return CommandManager.getAliases(command.commandToken.text).intersect(commandNamesWithSlash).isNotEmpty()
                    }
                })
                .withLanguage(LatexLanguage.INSTANCE),
            provider
        )
    }

    /**
     * Adds a completions contributor that gets activated when typing a value for a magic comment.
     */
    private fun extendMagicCommentValues(commentName: String, regex: Regex, completionProvider: CompletionProvider<CompletionParameters>) {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(LatexMagicComment::class.java)
                .with(object : PatternCondition<PsiElement>("Magic comment $commentName pattern") {
                    override fun accepts(comment: PsiElement, context: ProcessingContext?): Boolean {
                        return comment.isMagicComment() && comment.text.contains(regex)
                    }
                })
                .withLanguage(LatexLanguage.INSTANCE),
            completionProvider
        )
    }
}
