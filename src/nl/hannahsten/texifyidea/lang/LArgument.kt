package nl.hannahsten.texifyidea.lang

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
    val contextSignature: LatexContextIntro = LatexContextIntro.Inherit,

    val description: String = "",
) {

    val isRequired: Boolean
        get() = type == LArgumentType.REQUIRED
    val isOptional: Boolean
        get() = type == LArgumentType.OPTIONAL

    private fun contextIntroDisplay(): String {
        return when (contextSignature) {
            LatexContextIntro.Inherit -> ""
            is LatexContextIntro.Clear -> "<>"
            is LatexContextIntro.Assign -> "<${contextSignature.contexts.joinToString(",")}>"
            is LatexContextIntro.Modify -> buildString {
                append("<")
                if (contextSignature.toAdd.isNotEmpty()) {
                    append("+${contextSignature.toAdd.joinToString(",")}")
                }
                if (contextSignature.toRemove.isNotEmpty()) {
                    if (contextSignature.toAdd.isNotEmpty()) append(";")
                    append("-${contextSignature.toRemove.joinToString(",")}")
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
            name: String, ctx: LatexContextIntro = LatexContextIntro.Inherit, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, ctx, description)
        }

        fun required(
            name: String, ctx: LContextSet, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, LatexContextIntro.assign(ctx), description)
        }

        fun required(
            name: String, ctx: LatexContext, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.REQUIRED, LatexContextIntro.assign(ctx), description)
        }

        fun optional(
            name: String, ctx: LatexContextIntro = LatexContextIntro.Inherit, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.OPTIONAL, ctx, description)
        }

        fun optional(
            name: String, ctx: LatexContext, description: String = ""
        ): LArgument {
            return LArgument(name, LArgumentType.OPTIONAL, LatexContextIntro.Assign(ctx), description)
        }
    }
}