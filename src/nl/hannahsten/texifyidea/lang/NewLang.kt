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

fun LContextSet.compactDisplayString() : String = joinToString(",") { it.name }

/**
 * Describes how contexts are introduced.
 */
sealed interface LatexContextIntro {
    fun applyTo(outerCtx: LContextSet): LContextSet

    /**
     * Computes the outer context required to satisfy the inner context [innerCtx], given that this intro is applied.
     */
    fun revoke(innerCtx: LContextSet): LContextSet?

    companion object {

        fun inherit(): LatexContextIntro {
            return LContextInherit
        }

        fun add(ctx: LatexContext): LatexContextIntro {
            return LModifyContext(
                toAdd = setOf(ctx), toRemove = emptySet()
            )
        }

        fun remove(ctx: LatexContext): LatexContextIntro {
            return LModifyContext(
                toAdd = emptySet(), toRemove = setOf(ctx)
            )
        }

        fun buildContext(introList: List<LatexContextIntro>, outerCtx: LContextSet = emptySet()): LContextSet {
            return introList.fold(outerCtx) { ctx, intro ->
                intro.applyTo(ctx)
            }
        }

        private operator fun LContextSet.plus(other: LContextSet): LContextSet {
            if (this.isEmpty()) return other
            if (other.isEmpty()) return this
            return buildSet {
                addAll(this@plus)
                addAll(other)
            }
        }

        private operator fun LContextSet.minus(other: LContextSet): LContextSet {
            if (this.isEmpty()) return emptySet()
            if (other.isEmpty()) return this
            return buildSet {
                addAll(this@minus)
                removeAll(other)
            }
        }

        fun compose(a: LatexContextIntro, b: LatexContextIntro): LatexContextIntro {
            return when (b) {
                is LClearContext, is LAssignContext -> b
                is LContextInherit -> a
                is LModifyContext -> {
                    when (a) {
                        LContextInherit -> b
                        is LClearContext -> LAssignContext(b.toAdd)
                        is LAssignContext -> LAssignContext(b.applyTo(a.contexts))
                        is LModifyContext -> LModifyContext(
                            toAdd = a.toAdd - a.toRemove + b.toAdd - b.toRemove,
                            toRemove = a.toRemove - b.toAdd + b.toRemove
                        )
                    }
                }
            }
        }

        fun composeList(introList: List<LatexContextIntro>): LatexContextIntro {
            return introList.fold(LContextInherit, ::compose)
        }

        fun union(a: LatexContextIntro, b: LatexContextIntro): LatexContextIntro {
            if (b is LClearContext || b is LContextInherit) return a
            return when (a) {
                is LContextInherit, is LClearContext -> b
                is LAssignContext -> when (b) {
                    is LAssignContext -> LAssignContext(a.contexts + b.contexts)
                    is LModifyContext -> LAssignContext(b.applyTo(a.contexts))
                    else -> a // already handled above
                }

                is LModifyContext -> when (b) {
                    is LAssignContext -> LAssignContext(a.applyTo(b.contexts))
                    is LModifyContext -> LModifyContext(
                        toAdd = a.toAdd + b.toAdd,
                        toRemove = a.toRemove + b.toRemove
                    )

                    else -> a // already handled above
                }
            }
        }

        fun computeMinimalRequiredContext(
            introList: List<LatexContextIntro>,
            innerRequiredContext: LContextSet
        ): LContextSet? {
            if (innerRequiredContext.isEmpty()) return emptySet()
            return introList.asReversed().fold(innerRequiredContext) { ctx, intro ->
                intro.revoke(ctx) ?: return null
            }
        }

        val ASSIGN_MATH = LAssignContext(LatexContexts.Math)
    }
}

/**
 * Inherits the context from the outer scope.
 * This is the default behavior, so it can be used to reset the context to the outer scope.
 */
object LContextInherit : LatexContextIntro {
    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return outerCtx
    }

    override fun revoke(innerCtx: LContextSet): LContextSet? {
        return innerCtx
    }

    override fun toString(): String {
        return ""
    }
}

object LClearContext : LatexContextIntro {
    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return emptySet()
    }

    override fun revoke(innerCtx: LContextSet): LContextSet? {
        return if (innerCtx.isEmpty()) emptySet() else null
    }

    override fun toString(): String {
        return "Clear"
    }
}

/**
 * Sets the context to the given [LatexContext], discarding any previous context.
 */
data class LAssignContext(val contexts: Set<LatexContext>) : LatexContextIntro {

    constructor(contexts: LatexContext) : this(setOf(contexts))
    constructor(vararg contexts: LatexContext) : this(contexts.toSet())

    override fun applyTo(outerCtx: LContextSet): LContextSet {
        return contexts
    }

    override fun revoke(innerCtx: LContextSet): LContextSet? {
        if (contexts.containsAll(innerCtx)) return emptySet()
        return null
    }

    override fun toString(): String {
        return "Assign(${contexts.joinToString(",") { it.name }})"
    }
}

/**
 * Adds [toAdd] and removes [toRemove] contexts from the current context.
 */
class LModifyContext(val toAdd: Set<LatexContext>, val toRemove: Set<LatexContext>) : LatexContextIntro {
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

    override fun revoke(innerCtx: LContextSet): LContextSet? {
        if (innerCtx.any { it in toRemove }) return null // impossible to satisfy
        return innerCtx - toAdd
    }

    override fun toString(): String {
        val parts = mutableListOf<String>()
        if (toAdd.isNotEmpty()) {
            parts += "+(${toAdd.joinToString(",") { it.name }})"
        }
        if (toRemove.isNotEmpty()) {
            parts += "-(${toRemove.joinToString(",") { it.name }})"
        }
        return parts.joinToString("", prefix = "Modify(", postfix = ")")
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
    val contextSignature: LatexContextIntro = LContextInherit,

    val description: String = "",
) {

    val isRequired: Boolean
        get() = type == LArgumentType.REQUIRED
    val isOptional: Boolean
        get() = type == LArgumentType.OPTIONAL

    private fun contextIntroDisplay(): String {
        return when (contextSignature) {
            LContextInherit -> ""
            is LClearContext -> "<>"
            is LAssignContext -> "<${contextSignature.contexts.compactDisplayString()}>"
            is LModifyContext -> buildString {
                append("<")
                if (contextSignature.toAdd.isNotEmpty()) {
                    append("+${contextSignature.toAdd.compactDisplayString()}")
                }
                if (contextSignature.toRemove.isNotEmpty()) {
                    if (contextSignature.toAdd.isNotEmpty()) append(";")
                    append("-${contextSignature.toRemove.compactDisplayString()}")
                }
                append(">")
            }
        }
    }

    override fun toString(): String {
        val introDisplay = contextIntroDisplay()
        return when (type) {
            LArgumentType.REQUIRED -> "{$name$introDisplay}"
            LArgumentType.OPTIONAL -> "[$name$introDisplay]"
        }
    }

    companion object {
        fun required(
            name: String, ctx: LatexContextIntro = LContextInherit, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, ctx, description)
        }

        fun required(
            name: String, ctx: LatexContext, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, LAssignContext(ctx), description)
        }

        fun optional(
            name: String, ctx: LatexContextIntro = LContextInherit, description: String = ""
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

abstract class LSemanticEntity(
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
    val displayName: String
        get() = if (dependency.isEmpty()) name else "$name($dependency)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LSemanticEntity) return false

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

class LSemanticCommand(
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
) : LSemanticEntity(name, namespace, requiredContext, description) {

    override fun toString(): String {
        return "Cmd($displayName, ctx=<${requiredContext.joinToString(",")}>, arg=${arguments.joinToString("")}, description='$description')"
    }
}

class LSemanticEnv(
    name: String,
    namespace: String,
    requiredContext: LContextSet = emptySet(),
    /**
     * The list of arguments in order of appearance, including optional arguments.
     */
    val arguments: List<LArgument> = emptyList(),
    /**
     * The context signature that this environment introduces.
     */
    val contextSignature: LatexContextIntro = LContextInherit,
    /**
     * The description of the environment, used for documentation.
     */
    description: String = "",
) : LSemanticEntity(name, namespace, requiredContext, description) {
    override fun toString(): String {
        return "Env($displayName, ctx=<${requiredContext.joinToString(",")}>, arg=${arguments.joinToString("")}, scope=$contextSignature, description='$description')"
    }
}