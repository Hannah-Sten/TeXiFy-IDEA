package nl.hannahsten.texifyidea.lang

import nl.hannahsten.texifyidea.lang.LatexContextIntro.*

/**
 * The base interface for a context so that we can provide context-specific sematic features such as autocompletion or validation.
 *
 * A context applies to a "scope" in a LaTeX document, such as the math environment `\(...\)`,
 * a parameter `{...}`, or an environment `\begin{env}...\end{env}`.
 * For example, the part `...` in `\label{...}` should be a label, while the part `...` in `\usepackage{...}` should be a package name.
 *
 * Due to the nature of LaTeX, multiple contexts can be active at the same time.
 * Also, contexts can be inherited from the outer scope.
 *
 * @author Ezrnest
 */
interface LatexContext {

    /**
     * The display name of the context.
     */
    val display: String
}

typealias LContextSet = Set<LatexContext>

/**
 * Describes how contexts are introduced.
 *
 * @author Ezrnest
 */
sealed interface LatexContextIntro {
    fun applyTo(outerCtx: LContextSet): LContextSet

    /**
     * Computes the outer context required to satisfy the inner context [innerCtx], given that this intro is applied.
     */
    fun revoke(innerCtx: LContextSet): LContextSet?

    fun introduces(context: LatexContext): Boolean

    fun displayString(): String {
        val intro = this
        return when (intro) {
            Inherit -> ""
            is Clear -> "<>"
            is Assign -> "<${intro.contexts.joinToString(",")}>"
            is Modify -> buildString {
                append("<")
                if (intro.toAdd.isNotEmpty()) {
                    append("+${intro.toAdd.joinToString(",")}")
                }
                if (intro.toRemove.isNotEmpty()) {
                    if (intro.toAdd.isNotEmpty()) append(";")
                    append("-${intro.toRemove.joinToString(",")}")
                }
                append(">")
            }
        }
    }

    /**
     * Inherits the context from the outer scope.
     * This is the default behavior, so it can be used to reset the context to the outer scope.
     */
    object Inherit : LatexContextIntro {
        override fun applyTo(outerCtx: LContextSet): LContextSet {
            return outerCtx
        }

        override fun revoke(innerCtx: LContextSet): LContextSet? {
            return innerCtx
        }

        override fun toString(): String {
            return ""
        }

        override fun introduces(context: LatexContext): Boolean {
            return false
        }
    }

    object Clear : LatexContextIntro {
        override fun applyTo(outerCtx: LContextSet): LContextSet {
            return emptySet()
        }

        override fun revoke(innerCtx: LContextSet): LContextSet? {
            return if (innerCtx.isEmpty()) emptySet() else null
        }

        override fun toString(): String {
            return "Clear"
        }

        override fun introduces(context: LatexContext): Boolean {
            return false
        }
    }

    /**
     * Sets the context to the given [LatexContext], discarding any previous context.
     */
    data class Assign(val contexts: Set<LatexContext>) : LatexContextIntro {

        constructor(contexts: LatexContext) : this(setOf(contexts))
        constructor(vararg contexts: LatexContext) : this(contexts.toSet())

        override fun applyTo(outerCtx: LContextSet): LContextSet {
            return contexts
        }

        override fun revoke(innerCtx: LContextSet): LContextSet? {
            if (contexts.containsAll(innerCtx)) return emptySet()
            return null
        }

        override fun introduces(context: LatexContext): Boolean {
            return contexts.contains(context)
        }

        override fun toString(): String {
            return "Assign(${contexts.joinToString(",") { it.display }})"
        }
    }

    /**
     * Adds [toAdd] and removes [toRemove] contexts from the current context.
     */
    class Modify(val toAdd: Set<LatexContext>, val toRemove: Set<LatexContext>) : LatexContextIntro {
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

        override fun introduces(context: LatexContext): Boolean {
            return context in toAdd
        }

        override fun toString(): String {
            val parts = mutableListOf<String>()
            if (toAdd.isNotEmpty()) {
                parts += "+(${toAdd.joinToString(",") { it.display }})"
            }
            if (toRemove.isNotEmpty()) {
                parts += "-(${toRemove.joinToString(",") { it.display }})"
            }
            return parts.joinToString("", prefix = "Modify(", postfix = ")")
        }
    }

    companion object {

        fun inherit(): LatexContextIntro {
            return Inherit
        }

        fun add(ctx: LatexContext): LatexContextIntro {
            return Modify(
                toAdd = setOf(ctx), toRemove = emptySet()
            )
        }

        fun remove(ctx: LatexContext): LatexContextIntro {
            return Modify(
                toAdd = emptySet(), toRemove = setOf(ctx)
            )
        }

        fun assign(ctx: LatexContext): LatexContextIntro {
            return Assign(setOf(ctx))
        }

        fun assign(ctx: LContextSet): LatexContextIntro {
            return Assign(ctx)
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
                is Clear, is Assign -> b
                is Inherit -> a
                is Modify -> {
                    when (a) {
                        Inherit -> b
                        is Clear -> Assign(b.toAdd)
                        is Assign -> Assign(b.applyTo(a.contexts))
                        is Modify -> Modify(
                            toAdd = a.toAdd - a.toRemove + b.toAdd - b.toRemove,
                            toRemove = a.toRemove - b.toAdd + b.toRemove
                        )
                    }
                }
            }
        }

        fun composeList(introList: List<LatexContextIntro>): LatexContextIntro {
            return introList.fold(Inherit, ::compose)
        }

        fun union(a: LatexContextIntro, b: LatexContextIntro): LatexContextIntro {
            if (b is Clear || b is Inherit) return a
            return when (a) {
                is Inherit, is Clear -> b
                is Assign -> when (b) {
                    is Assign -> Assign(a.contexts + b.contexts)
                    is Modify -> Assign(b.applyTo(a.contexts))
                    else -> a // already handled above
                }

                is Modify -> when (b) {
                    is Assign -> Assign(a.applyTo(b.contexts))
                    is Modify -> Modify(
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

        val ASSIGN_MATH = assign(LatexContexts.Math)
    }
}