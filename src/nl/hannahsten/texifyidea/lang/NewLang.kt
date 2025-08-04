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

typealias LContextSet = Set<LatexContext>

/**
 * Describes how contexts are introduced.
 */
sealed interface LContextIntro {
    fun applyTo(outerCtx: LContextSet): LContextSet

    companion object {

        fun add(ctx: LatexContext): LContextIntro {
            return LModifyContext(
                toAdd = setOf(ctx), toRemove = emptySet()
            )
        }

        fun remove(ctx: LatexContext): LContextIntro {
            return LModifyContext(
                toAdd = emptySet(), toRemove = setOf(ctx)
            )
        }
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
     * The namespace of the entity, i.e., the package or class it belongs to, including the suffix `.sty` or `.cls`.
     */
    val dependency: String = "",
    val requiredContext: LContextSet = emptySet(),
    var description: String = ""
) {
    val fqName: String
        get() = if (dependency.isEmpty()) name else "$dependency.$name"

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
    namespace: String,
    requiredContext: LContextSet = emptySet(),
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument> = emptyList(),
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