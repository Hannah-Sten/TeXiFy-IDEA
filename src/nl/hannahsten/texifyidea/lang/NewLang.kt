package nl.hannahsten.texifyidea.lang

/**
 * The base interface for a context so that we can provide context-specific sematic features such as autocompletion or validation.
 *
 * A context applies to a "scope" in a LaTeX document, such as the math environment `\(...\)`,
 * a parameter `{...}`, or an environment `\begin{env}...\end{env}`.
 * For example, the part `...` in `\label{...}` should be a label, while the part `...` in `\usepackage{...}` should be a package name.
 *
 * Due to the nature of LaTeX, multiple contexts can be active at the same time.
 * Also, contexts can be inherited from the outer scope.
 */
interface LatexContext {

    /**
     * The name of the context.
     */
    val name: String
}

open class LatexContextBase(
    final override val name: String
) : LatexContext


object LBibtexKeyContext : LatexContextBase("bibtex.key")

object LMathContext : LatexContextBase("math")

/**
 * A context that describes text content, for example in `\text{...}`.
 */
object LTextContext : LatexContextBase("text")

/**
 * A marker interface for contexts that are related to file input.
 */
interface ILFileInputContext : LatexContext

/**
 * Describes the context of package names, for example in `\usepackage{...}`.
 *
 * The names can be comma-separated, for example in `\usepackage{package1,package2}`.
 */
object LPackageNamesContext : LatexContextBase("packages"), ILFileInputContext

/**
 * Describes the context of class names, for example in `\documentclass{...}`.
 */
object LClassNameContext : LatexContextBase("class"), ILFileInputContext

class LFileInputContext(
    name: String,
    val isCommaSeparated: Boolean = false,
    val supportedExtensions: Set<String> = emptySet(),
) : LatexContextBase(name), ILFileInputContext

class LFolderInputContext(
    name: String,
) : LatexContextBase(name), ILFileInputContext

/**
 * A context that describes a literal, for example in `\text{...}`.
 * This is used to provide autocompletion for text content.
 */
interface LLiteralContext : LatexContext

object LatexContexts {
    val PACKAGE_NAMES get() = LPackageNamesContext
    val CLASS_NAME get() = LClassNameContext

    val PREAMBLE = LatexContextBase("preamble")


    val LABEL_DEF = LatexContextBase("label.def")
    val LABEL_REF = LatexContextBase("label.ref")

    /**
     * A command and only a command. Used in `\newcommand{...}`.
     */
    val COMMAND = LatexContextBase("command")

    /**
     * An identifier, such as a command name without slash or environment name.
     *
     * Used in `\newenvironment{...}`.
     */
    val IDENTIFIER = LatexContextBase("identifier")

    /**
     * Some string literal that may be meaningful, such as `cc` in `\begin{tabular}{cc}`.
     */
    val LITERAL = LatexContextBase("literal")


    /**
     * Plain text content, such as in `\text{...}`.
     */
    val TEXT = LTextContext

    /**
     * A number is expected, for example in `\setcounter{...}{...}`.
     */
    val NUMERIC = LatexContextBase("numeric")

    val LIST_TYPE = LatexContextBase("list.type")

    val SINGLE_FILE = LFileInputContext(
        "file.general", isCommaSeparated = false, supportedExtensions = emptySet(),
    )
    val MULTIPLE_FILES = LFileInputContext(
        "files.general", isCommaSeparated = true, supportedExtensions = emptySet(),
    )
    val SINGLE_TEX_FILE = LFileInputContext(
        "file.tex", isCommaSeparated = false, supportedExtensions = setOf("tex"),
    )
    val MULTIPLE_TEX_FILES = LFileInputContext(
        "files.tex", isCommaSeparated = true, supportedExtensions = setOf("tex"),
    )

    val SINGLE_BIB_FILE = LFileInputContext(
        "file.bib", isCommaSeparated = false, supportedExtensions = setOf("bib"),
    )

    val MULTIPLE_BIB_FILES = LFileInputContext(
        "files.bib", isCommaSeparated = true, supportedExtensions = setOf("bib"),
    )

    val FOLDER = LFolderInputContext("folder")

    val BIBTEX_KEY = LBibtexKeyContext
    val BIB_STYLE = LatexContextBase("style")

    val URL = LFileInputContext("url")

    val ALGORITHMICX = LatexContextBase("algorithmicx")

    val MINTED_FUNTIME_LAND = LatexContextBase("minted.funtime.land")



}

typealias LContextSet = Set<LatexContext>

/**
 * Describes how contexts are introduced.
 */
sealed interface LContextIntro {
    fun applyTo(outerCtx: LContextSet): LContextSet

    companion object {

    }
}

/**
 * Inherits the context from the outer scope.
 * This is the default behavior, so it can be used to reset the context to the outer scope.
 */
object LContextInherit : LContextIntro {
    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return outerCtx
    }
}

object LClearContext : LContextIntro {
    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return emptySet()
    }
}

/**
 * Sets the context to the given [LatexContext], discarding any previous context.
 */
class LAssignContext(val contexts: Set<LatexContext>) : LContextIntro {

    constructor(contexts: LatexContext) : this(setOf(contexts))
    constructor(vararg contexts: LatexContext) : this(contexts.toSet())

    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return contexts
    }
}

/**
 * Adds [toAdd] and removes [toRemove] contexts from the current context.
 */
class LModifyContext(val toAdd: Set<LatexContext>, val toRemove: Set<LatexContext>) : LContextIntro {
    override fun applyTo(outerCtx: LContextSet): LContextSet {
        val res = outerCtx.toMutableSet()
        if (toAdd.isNotEmpty()) {
            res += toAdd
        }
        if (toRemove.isNotEmpty()) {
            res -= toRemove
        }
        return res
    }
}

object LContextIntros {
    val MATH = LAssignContext(LMathContext)
    val TEXT = LAssignContext(LTextContext)
    val INHERIT = LContextInherit
    val LABEL_REF = LAssignContext(LatexContexts.LABEL_REF)
    val BIBTEX_KEY = LAssignContext(LBibtexKeyContext)
    val PACKAGE = LAssignContext(LPackageNamesContext)
}

enum class LArgumentType {
    REQUIRED,
    OPTIONAL
}

class LArgument(
    /**
     * The name of the argument, used mainly for documentation (rather than named arguments).
     *
     */
    val name: String,

    /**
     * The type of the argument.
     */
    val type: LArgumentType,

    /**
     * The context that this argument introduces.
     *
     * For example, a file name argument might introduce a file input context.
     */
    val contextSignature: LContextIntro = LContextInherit,

    val description: String = "",
) {

    companion object {
        fun required(
            name: String, ctx: LContextIntro = LContextInherit, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, ctx, description)
        }

        fun required(
            name: String, ctx: LatexContext, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, LAssignContext(ctx), description)
        }

        fun optional(
            name: String, ctx: LContextIntro = LContextInherit, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.OPTIONAL, ctx, description)
        }

        fun optional(
            name: String, ctx: LatexContext, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.OPTIONAL, LAssignContext(ctx), description)
        }
    }
}

abstract class LEntity(
    /**
     * The name of the entity, such as a command or environment.
     *
     * This is the name without the leading backslash for commands.
     */
    val name: String,
    /**
     * The namespace of the entity, i.e., the package or class it belongs to.
     */
    val namespace: String = "",
    val requiredContext: LContextSet = emptySet(),
    var description: String = ""
) {
    val fqName: String = if (namespace.isEmpty()) name else "$namespace.$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LEntity) return false

        if (name != other.name) return false
        if (namespace != other.namespace) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + namespace.hashCode()
        return result
    }
}

class NewLatexCommand(
    /**
     * The name of the command without the leading backslash.
     */
    name: String,
    namespace: String,
    requiredContext: LContextSet = emptySet(),
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument>,
    /**
     * The description of the command, used for documentation.
     */
    description: String = "",

    val display: String? = null,
    val nameWithSlash: String = "\\$name",
) : LEntity(name, namespace, requiredContext, description)

class NewLatexEnvironment(
    name: String,
    namespace: String,
    requiredContext: LContextSet = emptySet(),
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument>,
    /**
     * The context signature that this environment introduces.
     */
    val contextSignature: LContextIntro,
    /**
     * The description of the environment, used for documentation.
     */
    description: String = "",
) : LEntity(name, namespace, requiredContext, description)