package nl.hannahsten.texifyidea.lang

enum class LArgumentType {
    REQUIRED,
    OPTIONAL
}

data class LArgument(
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

    override fun toString(): String {
        val introDisplay = contextSignature.displayString()
        return when (type) {
            LArgumentType.REQUIRED -> "{$name$introDisplay}"
            LArgumentType.OPTIONAL -> "[$name$introDisplay]"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LArgument

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
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