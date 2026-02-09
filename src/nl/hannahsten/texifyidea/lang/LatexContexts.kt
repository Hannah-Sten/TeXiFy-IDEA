package nl.hannahsten.texifyidea.lang

/**
 * Simple implementation of [LatexContext] that only holds a name.
 *
 * The identity of the context is based on the name, so two contexts with the same name are considered equal.
 */
open class SimpleLatexContext(
    val name: String,
    final override val display: String = name
) : LatexContext {
    override fun toString(): String = display

    private val hash = name.hashCode()

    final override fun hashCode(): Int = hash

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleLatexContext) return false
        return name == other.name
    }
}

/**
 * A marker interface for contexts that are related to file input.
 */
interface ILFileInputContext : LatexContext

class SimpleFileInputContext(
    name: String,
    val isCommaSeparated: Boolean = false,
    val isExtensionRequired: Boolean = false,
    val supportedExtensions: Set<String> = emptySet(),
    val isAbsolutePathSupported: Boolean = true
) : SimpleLatexContext(name), ILFileInputContext

/**
 * Some predefined contexts that are used in LaTeX files.
 *
 * @author Ezrnest
 */
object LatexContexts {

    val Math = SimpleLatexContext("math")

    val InlineMath = SimpleLatexContext("math.inline")

    /**
     * Describes the context of package names, for example in `\usepackage{...}`.
     *
     * The names can be comma-separated, for example in `\usepackage{package1,package2}`.
     */
    val PackageNames = SimpleLatexContext("packages")

    /**
     * Describes the context of class names, for example in `\documentclass{...}`.
     */
    val ClassName = SimpleLatexContext("class")

    val Preamble = SimpleLatexContext("preamble")

    /**
     * Definitions of labels, such as `\label{...}`.
     */
    val LabelDefinition = SimpleLatexContext("new.label")

    /**
     * References to labels, such as `\ref{...}` or `\eqref{...}`.
     */
    val LabelReference = SimpleLatexContext("label.ref")

    /**
     * An identifier that is used in a command definition, such as `\mycmd` in `\newcommand{\mycmd}{...}`.
     */
    val CommandDeclaration = SimpleLatexContext("new.cmd")

    /**
     * An identifier that is used in an environment definition, such as `myenv` in `\newenvironment{myenv}{...}{...}`.
     */
    val EnvironmentDeclaration = SimpleLatexContext("new.env")

    /**
     * The definition of a command, such as `...` in `\newcommand{\mycmd}{...}`.
     */
    val InsideDefinition = SimpleLatexContext("definition")

    /**
     * An identifier, such as a command name without slash or environment name.
     *
     * Used in `\begin{...}`.
     */
    val Identifier = SimpleLatexContext("identifier")

    /**
     * Verbatim as in \verb|...| or verbatim environments
     */
    val Verbatim = SimpleLatexContext("verbatim")

    /**
     * Some string literal that may be meaningful, such as `cc` in `\begin{tabular}{cc}`.
     *
     * Creating more specific contexts for literals is preferred and auto-completion can be further provided.
     */
    val Literal = SimpleLatexContext("literal")

    /**
     * Plain text content, such as in `\text{...}`.
     */
    val Text = SimpleLatexContext("text")

    val Comment = SimpleLatexContext("comment")

    val baseContexts = setOf(Preamble, Text)

    /**
     * A number is expected, for example in `\setcounter{...}{...}`.
     */
    val Numeric = SimpleLatexContext("numeric")

    /**
     * List names, such as `itemize` or `enumerate`.
     */
    val ListType = SimpleLatexContext("list.type")

    val SingleFile = SimpleFileInputContext(
        "file.general", isCommaSeparated = false, supportedExtensions = emptySet(),
    )

    val SingleBibFile = SimpleFileInputContext(
        "file.bib", isCommaSeparated = false, isExtensionRequired = true, supportedExtensions = setOf("bib"),
    )

    val MultipleBibFiles = SimpleFileInputContext(
        "files.bib", isCommaSeparated = true, supportedExtensions = setOf("bib"),
    )

    val SingleCSLBibFile = SimpleFileInputContext(
        "file.bib", isCommaSeparated = false, isExtensionRequired = true, supportedExtensions = setOf("bib", "json", "yaml"),
    )

    val Folder = SimpleLatexContext("folder")

    /**
     * The citation reference in `\cite{...}`.
     */
    val BibReference = SimpleLatexContext("bib.ref")

    val BibKey = SimpleLatexContext("bib.key")

    /**
     * A context for BibTeX style files, such as `plain` in `\bibliographystyle{plain}`.
     */
    val BibStyle = SimpleLatexContext("style")

    val URL = SimpleFileInputContext("url")

    val Algorithmicx = SimpleLatexContext("algorithmicx")

    val MintedFuntimeLand = SimpleLatexContext("minted.funtime.land")

    /**
     * References to glossary entries, such as in `\gls{...}` or `\Gls{...}`.
     */
    val GlossaryReference = SimpleLatexContext("glossary")

    /**
     * Definitions of glossary entries, such as in `\newglossaryentry{...}{...}`.
     */
    val GlossaryDefinition = SimpleLatexContext("new.glossary")

    /**
     * A context for color names or literal, such as in `\textcolor{red}{...}` or `\color{blue}`.
     */
    val ColorReference = SimpleLatexContext("color")
    val ColorDefinition = SimpleLatexContext("new.color")

    val PicturePath = SimpleLatexContext("picture.path")

    // environment contexts
    val Enumerate = SimpleLatexContext("env.enumerate")
    val Figure = SimpleLatexContext("env.figure")
    val Table = SimpleLatexContext("env.table")

    /**
     * A context inside tabular environment, where `&` is valid.
     */
    val Tabular = SimpleLatexContext("tabular")

    val Alignable = SimpleLatexContext("alignable")

    val TikzPicture = SimpleLatexContext("tikz.picture")

    fun asFileInputCtx(intro: LatexContextIntro): SimpleFileInputContext? {
        if (intro !is LatexContextIntro.Assign) return null
        val contexts = intro.contexts
        for (ctx in contexts) {
            if (ctx is SimpleFileInputContext) return ctx
        }
        return null
    }

    /**
     * Contexts in which an identifier is expected.
     *
     * @see nl.hannahsten.texifyidea.psi.impl.LatexParameterTextImplMixin.getNameIdentifier
     */
    val contextsAsIdentifier = setOf(
        LabelDefinition,
        LabelReference,
        BibKey,
        BibReference,
        GlossaryReference,
        GlossaryDefinition,
        CommandDeclaration,
        EnvironmentDeclaration,
        Identifier
    )
}