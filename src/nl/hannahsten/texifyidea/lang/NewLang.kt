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

abstract class LatexContextBase(
    final override val name: String
) : LatexContext

/**
 * The label
 */
object LLabelContext : LatexContextBase("label")

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


open class LFileInputContextBase(
    name: String,
    val isCommaSeparated: Boolean = false,
    val supportedExtensions: Set<String> = emptySet(),
) : LatexContextBase(name), ILFileInputContext

/**
 * A context that describes a literal, for example in `\text{...}`.
 * This is used to provide autocompletion for text content.
 */
interface LLiteralContext : LatexContext

object LContexts {
    val PACKAGE_NAMES get() = LPackageNamesContext
    val CLASS_NAME get() = LClassNameContext
    val LABEL get() = LLabelContext

    val SINGLE_FILE = LFileInputContextBase(
        "file.general",
        isCommaSeparated = false,
        supportedExtensions = emptySet(),
    )
    val MULTIPLE_FILES = LFileInputContextBase(
        "files.general",
        isCommaSeparated = true,
        supportedExtensions = emptySet(),
    )

    val SINGLE_TEX_FILE = LFileInputContextBase(
        "file.tex",
        isCommaSeparated = false,
        supportedExtensions = setOf("tex"),
    )

    val SINGLE_BIB_FILE = LFileInputContextBase(
        "file.bib",
        isCommaSeparated = false,
        supportedExtensions = setOf("bib"),
    )

}

typealias LContextInfo = Set<LatexContext>

/**
 * Describes how contexts are introduced.
 */
sealed interface LContextSignature {
    fun applyTo(outerCtx: LContextInfo): LContextInfo
}

/**
 * Inherits the context from the outer scope.
 * This is the default behavior, so it can be used to reset the context to the outer scope.
 */
object LContextInherit : LContextSignature {
    override fun applyTo(outerCtx: LContextInfo): LContextInfo {
        return outerCtx
    }
}

object LClearContext : LContextSignature {
    override fun applyTo(outerCtx: LContextInfo): LContextInfo {
        return emptySet()
    }
}

/**
 * Sets the context to the given [LatexContext], discarding any previous context.
 */
class LContextAssign(val contexts: Set<LatexContext>) : LContextSignature {

    constructor(contexts: LatexContext) : this(setOf(contexts))
    constructor(vararg contexts: LatexContext) : this(contexts.toSet())

    override fun applyTo(outerCtx: LContextInfo): LContextInfo {
        return contexts
    }
}

/**
 * Adds [toAdd] and removes [toRemove] contexts from the current context.
 */
class LContextModify(val toAdd: Set<LatexContext>, val toRemove: Set<LatexContext>) : LContextSignature {
    override fun applyTo(outerCtx: LContextInfo): LContextInfo {
        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            return outerCtx
        }
        if (toRemove.isEmpty()) {
            // If no contexts are removed, we just add the specified contexts.
            return outerCtx + toAdd
        }
        if (toAdd.isEmpty()) {
            // If no contexts are added, we just remove the specified contexts.
            return outerCtx - toRemove
        }
        return (outerCtx + toAdd) - toRemove
    }
}

object LContextInLContextSignatures {
    val MATH = LContextAssign(LMathContext)
    val TEXT = LContextAssign(LTextContext)
    val INHERIT = LContextInherit
    val LABEL = LContextAssign(LLabelContext)
    val PACKAGE = LContextAssign(LPackageNamesContext)
}


enum class LArgumentType {
    REQUIRED, OPTIONAL
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
    val contextSignature: LContextSignature = LContextInherit,

    val description: String = "",
)



abstract class LEntity(
    /**
     * The name of the entity, such as a command or environment.
     *
     * This is the name without the leading backslash for commands.
     */
    val name: String, val dependency: LatexPackage,
    val requiredContext : LContextInfo = emptySet(),
    var description: String = ""
) {
    val fqName: String = "${dependency.name}.$name"


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LEntity) return false

        if (name != other.name) return false
        if (dependency != other.dependency) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + dependency.hashCode()
        return result
    }

}

class NewLatexCommand(
    /**
     * The name of the command without the leading backslash.
     */
    name: String,
    dependency: LatexPackage,
    requiredContext : LContextInfo = emptySet(),
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
) : LEntity(name, dependency, requiredContext, description) {

}

class NewLatexEnvironment(
    name: String,
    dependency: LatexPackage,
    requiredContext : LContextInfo = emptySet(),
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument>,

    val contextSignature: LContextSignature,
    /**
     * The description of the environment, used for documentation.
     */
    description: String = "",
) : LEntity(name, dependency,requiredContext, description) {

}