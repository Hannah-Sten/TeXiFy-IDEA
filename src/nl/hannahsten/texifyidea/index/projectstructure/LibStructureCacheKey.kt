package nl.hannahsten.texifyidea.index.projectstructure

import nl.hannahsten.texifyidea.settings.sdk.SdkPath

/**
 * Cache key for library structure information, combining SDK path and package file name.
 * This ensures different LaTeX distributions in different modules get separate caches.
 *
 * @property sdkPath The SDK home path or resolved kpsewhich path (see [nl.hannahsten.texifyidea.settings.sdk.SdkPath])
 * @property nameWithExt The package file name with extension (e.g., "amsmath.sty")
 */
data class LibStructureCacheKey(val sdkPath: SdkPath, val nameWithExt: String)