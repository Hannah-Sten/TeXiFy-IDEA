package nl.hannahsten.texifyidea.lang

import com.intellij.psi.PsiComment

/**
 * todo replace by magic comment
 * @author Sten Wessel
 */
data class LatexAnnotation(val key: String, val value: String? = null) {
    companion object {
        @JvmStatic val KEY_INJECT_LANGUAGE = "InjectLanguage"

        private val PREFIX = "% !TeX "

        fun fromComment(prev: PsiComment): LatexAnnotation? {
            if (prev.text.startsWith(PREFIX)) {
                val list = prev.text.removePrefix(PREFIX).split(" = ".toRegex(), 2)
                return LatexAnnotation(list[0], list.getOrNull(1))
            }

            return null
        }
    }

    override fun toString()= "% !TeX $key ${if (value != null) "= $value" else ""}"

}