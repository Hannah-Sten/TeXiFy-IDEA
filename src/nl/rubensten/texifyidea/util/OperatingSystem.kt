package nl.rubensten.texifyidea.util

/**
 * @author Ruben Schellekens, Thomas Schouten
 */
object OperatingSystem {

    /**
     * Some platforms have a distinctive part in their name, for example "Windows XP", "Windows 7" etc.
     * Some platforms have multiple different parts.
     * This is just a setup which works often, it doesn't cover all cases.
     */
    @JvmStatic
    val type: Type by lazy {
        val platformString = System.getProperty("os.name").toLowerCase()
        when {
            platformString.contains("mac") || platformString.contains("darwin") -> Type.MACOS
            platformString.contains("windows") -> Type.WINDOWS
            platformString.contains("linux") -> Type.LINUX
            else -> Type.OTHER
        }
    }

    /**
     * @author Thomas Schouten
     */
    enum class Type {

        WINDOWS,
        LINUX,
        MACOS,
        OTHER;
    }
}