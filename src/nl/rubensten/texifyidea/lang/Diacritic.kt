package nl.rubensten.texifyidea.lang

/**
 *
 * @author Sten Wessel
 */
interface Diacritic {
    companion object {
        fun buildChain(base: String, diacritics: List<Diacritic?>): String? {
            if (diacritics.contains(null)) {
                return null
            }
            return diacritics.fold(base, { s, d -> d!!.buildCommand(s) })
        }
    }

    val unicode: String
    val command: String
    val needsSpace: Boolean

    fun buildCommand(param: String): String = command + if (param.length > 1) "{$param}" else if (needsSpace) " $param" else param

    enum class Normal(override val unicode: String, override val command: String, override val needsSpace: Boolean) : Diacritic {
        GRAVE("\u0300", "\\`", false),
        ACUTE("\u0301", "\\'", false),
        CIRCUMFLEX("\u0302", "\\^", false),
        DIERESIS("\u0308", "\\\"", false),
        DOUBLE_ACUTE("\u030B", "\\H", true),
        TILDE("\u0303", "\\~", false),
        CEDILLA("\u0327", "\\c", true),
        OGONEK("\u0328", "\\k", true),
        MACRON("\u0304", "\\=", false),
        MACRON_BELOW("\u0331", "\b", true),
        DOT("\u0307", "\\.", false),
        DOT_BELOW("\u0323", "\\d", true),
        RING("\u030A", "\\r", true),
        BREVE("\u0306", "\\u", true),
        CARON("\u030C", "\\v", true),
        TIE("\u0311", "\\t", true),
        DOUBLE_TIE("\u0361", "\\t", true);

        companion object {
            fun fromUnicode(unicode: String): Normal? = Normal.values().find { it.unicode == unicode }
        }

        override fun buildCommand(param: String): String {
            if (!param.startsWith("\\")) {
                return super.buildCommand(param.replace("i", "\\i").replace("j", "\\j"))
            }
            return super.buildCommand(param)
        }
    }

    enum class Math(override val unicode: String, override val command: String, override val needsSpace: Boolean) : Diacritic {
        HAT("\u0302", "\\hat", true),
        DOUBLE_HAT("\u1DCD", "\\widehat", true),
        CHECK("\u030C", "\\check", true),
        TILDE("\u0303", "\\tilde", true),
        DOUBLE_TILDE("\u0360", "\\widetilde", true),
        ACUTE("\u0301", "\\acute", true),
        GRAVE("\u0300", "\\grave", true),
        DOT("\u0307", "\\dot", true),
        DOUBLE_DOT("\u0308", "\\ddot", true),
        BREVE("\u0306", "\\breve", true),
        BAR("\u0304", "\\bar", true),
        VEC("\u20D7", "\\vec", true);

        companion object {
            fun fromUnicode(unicode: String): Math? = Math.values().find { it.unicode == unicode }
        }

        override fun buildCommand(param: String): String {
            if (!param.startsWith("\\")) {
                return super.buildCommand(param.replace("i", "\\imath").replace("j", "\\jmath"))
            }
            return super.buildCommand(param)
        }
    }
}
