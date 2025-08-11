package nl.hannahsten.texifyidea.lang

open class SimpleLatexContext(
    final override val name: String
) : LatexContext {
    override fun toString(): String {
        return name
    }
}

/**
 * A marker interface for contexts that are related to file input.
 */
interface ILFileInputContext : LatexContext

class LFileInputContext(
    name: String,
    val isCommaSeparated: Boolean = false,
    val supportedExtensions: Set<String> = emptySet(),
) : SimpleLatexContext(name), ILFileInputContext

/**
 * A context that describes a literal, for example `cc` in `\begin{tabular}{cc}`.
 * This is used to provide autocompletion for text content.
 */
interface LLiteralContext : LatexContext

object LatexContexts {

    val Math = SimpleLatexContext("math")

    /**
     * Describes the context of package names, for example in `\usepackage{...}`.
     *
     * The names can be comma-separated, for example in `\usepackage{package1,package2}`.
     */
    object PackageNames : SimpleLatexContext("packages"), ILFileInputContext

    /**
     * Describes the context of class names, for example in `\documentclass{...}`.
     */
    object ClassName : SimpleLatexContext("class"), ILFileInputContext

    val Preamble = SimpleLatexContext("preamble")

    /**
     * This context should never be introduced, so command under this context will never be suggested.
     */
    val Nothing = SimpleLatexContext("nothing")

    val LabelDefinition = SimpleLatexContext("new.label")
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
     * Some string literal that may be meaningful, such as `cc` in `\begin{tabular}{cc}`.
     */
    val Literal = SimpleLatexContext("literal")

    /**
     * Plain text content, such as in `\text{...}`.
     */
    val Text = SimpleLatexContext("text")

    /**
     * A number is expected, for example in `\setcounter{...}{...}`.
     */
    val Numeric = SimpleLatexContext("numeric")

    val ListType = SimpleLatexContext("list.type")

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

    object Folder : SimpleLatexContext("folder"), ILFileInputContext

    object BibtexKey : SimpleLatexContext("bibtex.key"), ILFileInputContext

    val BibStyle = SimpleLatexContext("style")

    val URL = LFileInputContext("url")

    val Algorithmicx = SimpleLatexContext("algorithmicx")

    val MintedFuntimeLand = SimpleLatexContext("minted.funtime.land")

    // environment contexts

    val Enumerate = SimpleLatexContext("env.enumerate")
    val Figure = SimpleLatexContext("env.figure")
    val Table = SimpleLatexContext("env.table")

    /**
     * A context inside tabular environment, where `&` is valid.
     */
    val Tabular = SimpleLatexContext("tabular")

    val Alignable = SimpleLatexContext("alignable")

    val Comment = SimpleLatexContext("comment")
}