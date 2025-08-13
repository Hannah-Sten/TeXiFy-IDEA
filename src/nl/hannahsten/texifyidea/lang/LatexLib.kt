package nl.hannahsten.texifyidea.lang

/**
 * Represents a LaTeX library, which can be a package or class file.
 *
 *
 */
@JvmInline
value class LatexLib(val name: String) {
    val isPackageFile: Boolean
        get() = name.endsWith(".sty")

    val isClassFile: Boolean
        get() = name.endsWith(".cls")

    val requiresImport: Boolean
        get() = isPackageFile || isClassFile

    val isDefault: Boolean
        get() = this == BASE

    val isCustom: Boolean
        get() = this == CUSTOM

    fun toLatexPackage(): LatexPackage? {
        if (!isPackageFile) return null
        return LatexPackage(name.substringBefore('.'))
    }

    fun toPackageName(): String? {
        return if (isPackageFile) name.substringBefore('.') else null
    }

    override fun toString(): String {
        return name
    }

    companion object {
        val CUSTOM = LatexLib("")

        val BASE = LatexLib("(base)") // Represents the base package

        fun fromPackageName(name: String): LatexLib {
            return if (name.isEmpty()) BASE else LatexLib("$name.sty")
        }

        fun fromFileName(fileName: String): LatexLib {
            return if (fileName.isEmpty()) BASE else LatexLib(fileName)
        }
    }
}