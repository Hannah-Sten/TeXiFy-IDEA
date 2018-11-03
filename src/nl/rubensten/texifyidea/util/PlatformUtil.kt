package nl.rubensten.texifyidea.util

enum class PlatformType {
    WINDOWS,
    LINUX,
    MACOS,
    OTHER;
}

/**
 * Some platforms have a distinctive part in their name, for example "Windows XP", "Windows 7" etc.
 * Some platforms have multiple different parts.
 * This is just a setup which works often, it doesn't cover all cases.
 */
fun getPlatformType(): PlatformType {
    val platformString = System.getProperty("os.name").toLowerCase()
    return when {
        platformString.contains("mac") || platformString.contains("darwin") -> PlatformType.MACOS
        platformString.contains("windows") -> PlatformType.WINDOWS
        platformString.contains("linux") -> PlatformType.LINUX
        else -> PlatformType.OTHER
    }
}