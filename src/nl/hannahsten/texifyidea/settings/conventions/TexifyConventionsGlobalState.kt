package nl.hannahsten.texifyidea.settings.conventions

/**
 * Global convention settings.
 *
 * The global settings store a list of schemes, one of which is the global default scheme. In addition, the global
 * settings remember which scheme is currently selected. Instances of this class must be serializable, since they are
 * persisted by a [TexifyConventionsGlobalSettingsManager]. Changing the properties to "val" silently fails
 * serialization.
 *
 * The class is internal because clients should only use the [TexifyConventionsSettings] abstraction.
 */
internal data class TexifyConventionsGlobalState(
    var selectedScheme: String = TexifyConventionsScheme.DEFAULT_SCHEME_NAME,
    var schemes: List<TexifyConventionsScheme> = listOf(TexifyConventionsScheme())
) {

    fun deepCopy() = copy(schemes = schemes.map { it.copy() })
}