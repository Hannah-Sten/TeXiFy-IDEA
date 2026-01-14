package nl.hannahsten.texifyidea.lang

/**
 * @author Sten Wessel
 */
interface Diacritic {

    companion object {

        fun buildChain(base: String, diacritics: List<Diacritic?>): String? {
            if (diacritics.contains(null) || diacritics.isEmpty()) {
                return null
            }
            return diacritics.fold(base) { s, d -> d!!.buildCommand(s) }
        }

        fun allValues(): List<Diacritic> {
            val list: MutableList<Diacritic> = ArrayList()
            list.addAll(Normal.entries.toTypedArray())
            list.addAll(Math.entries.toTypedArray())
            return list
        }
    }

    val unicode: String
    val command: String
    val needsSpace: Boolean
    val isTypeable: Boolean

    fun buildCommand(param: String) = command + if (param.length > 1) {
        "{$param}"
    }
    else if (needsSpace) {
        " $param"
    }
    else param

    enum class Normal(
        override val unicode: String,
        override val command: String,
        override val needsSpace: Boolean,
        override val isTypeable: Boolean = false
    ) : Diacritic {

        GRAVE("\u0300", "\\`", false, isTypeable = true),
        ACUTE("\u0301", "\\'", false, isTypeable = true),
        CIRCUMFLEX("\u0302", "\\^", false, isTypeable = true),
        DIERESIS("\u0308", "\\\"", false, isTypeable = true),
        DOUBLE_ACUTE("\u030B", "\\H", true),
        TILDE("\u0303", "\\~", false, isTypeable = true),
        CEDILLA("\u0327", "\\c", true, isTypeable = true),
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

            fun fromUnicode(unicode: String): Normal? = entries
                .find { it.unicode == unicode }

            fun fromCommand(command: String): Normal? = entries
                .find { it.command == command }
        }

        override fun buildCommand(param: String): String {
            if (!param.startsWith("\\")) {
                return super.buildCommand(param.replace("i", "\\i").replace("j", "\\j"))
            }

            return super.buildCommand(param)
        }
    }

    enum class Math(
        override val unicode: String, override val command: String, override val needsSpace: Boolean,
        override val isTypeable: Boolean = false
    ) : Diacritic {

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

            fun fromUnicode(unicode: String): Math? = entries
                .find { it.unicode == unicode }
        }

        override fun buildCommand(param: String): String {
            if (!param.startsWith("\\")) {
                return super.buildCommand(param.replace("i", "\\imath").replace("j", "\\jmath"))
            }

            return super.buildCommand(param)
        }
    }
}