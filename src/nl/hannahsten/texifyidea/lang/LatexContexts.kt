package nl.hannahsten.texifyidea.lang

open class LatexContextBase(
    final override val name: String
) : LatexContext

/**
 * A marker interface for contexts that are related to file input.
 */
interface ILFileInputContext : LatexContext

class LFileInputContext(
    name: String,
    val isCommaSeparated: Boolean = false,
    val supportedExtensions: Set<String> = emptySet(),
) : LatexContextBase(name), ILFileInputContext

/**
 * A context that describes a literal, for example `cc` in `\begin{tabular}{cc}`.
 * This is used to provide autocompletion for text content.
 */
interface LLiteralContext : LatexContext

object LatexContexts {

    object Math : LatexContextBase("math")

    /**
     * Describes the context of package names, for example in `\usepackage{...}`.
     *
     * The names can be comma-separated, for example in `\usepackage{package1,package2}`.
     */
    object PackageNames : LatexContextBase("packages"), ILFileInputContext

    /**
     * Describes the context of class names, for example in `\documentclass{...}`.
     */
    object ClassName : LatexContextBase("class"), ILFileInputContext

    val Preamble = LatexContextBase("preamble")

    /**
     * The definition of a command, such as `...` in `\newcommand{\mycmd}{...}`.
     */
    val InsideDefinition = LatexContextBase("definition")

    val LabelDefinition = LatexContextBase("label.def")
    val LabelReference = LatexContextBase("label.ref")

    /**
     * A command and only a command. Used in `\newcommand{...}`.
     */
    val PlainCommand = LatexContextBase("command")

    /**
     * An identifier, such as a command name without slash or environment name.
     *
     * Used in `\newenvironment{...}`.
     */
    val Identifier = LatexContextBase("identifier")

    /**
     * Some string literal that may be meaningful, such as `cc` in `\begin{tabular}{cc}`.
     */
    val Literal = LatexContextBase("literal")

    /**
     * Plain text content, such as in `\text{...}`.
     */
    val Text = LatexContextBase("text")

    /**
     * A number is expected, for example in `\setcounter{...}{...}`.
     */
    val Numeric = LatexContextBase("numeric")

    val ListType = LatexContextBase("list.type")

    val SingleFile = LFileInputContext(
        "file.general", isCommaSeparated = false, supportedExtensions = emptySet(),
    )
    val MultipleFiles = LFileInputContext(
        "files.general", isCommaSeparated = true, supportedExtensions = emptySet(),
    )
    val SingleTexFile = LFileInputContext(
        "file.tex", isCommaSeparated = false, supportedExtensions = setOf("tex"),
    )
    val MultipleTexFiles = LFileInputContext(
        "files.tex", isCommaSeparated = true, supportedExtensions = setOf("tex"),
    )

    val SingleBibFile = LFileInputContext(
        "file.bib", isCommaSeparated = false, supportedExtensions = setOf("bib"),
    )

    val MultipleBibFiles = LFileInputContext(
        "files.bib", isCommaSeparated = true, supportedExtensions = setOf("bib"),
    )

    object Folder : LatexContextBase("folder"), ILFileInputContext

    object BibtexKey : LatexContextBase("bibtex.key"), ILFileInputContext

    val BibStyle = LatexContextBase("style")

    val URL = LFileInputContext("url")

    val Algorithmicx = LatexContextBase("algorithmicx")

    val MintedFuntimeLand = LatexContextBase("minted.funtime.land")
}