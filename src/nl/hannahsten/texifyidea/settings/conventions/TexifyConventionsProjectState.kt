package nl.hannahsten.texifyidea.settings.conventions

/**
 * Per project convention settings.
 *
 * The per project settings store a single project scheme and a flag that indicates whether the project scheme or the
 * global scheme should be used. Instances of this class must be serializable, since they are persisted by a
 * [TexifyConventionsProjectSettingsManager]. Changing the properties to "val" silently fails serialization.
 *
 * The class is internal because clients should only use the [TexifyConventionsSettings] abstraction.
 */
internal data class TexifyConventionsProjectState(
    var scheme: TexifyConventionsScheme = TexifyConventionsScheme(
        myName = TexifyConventionsScheme.PROJECT_SCHEME_NAME,
    ),
    var useProjectScheme: Boolean = false
) {

    fun deepCopy() = copy(scheme = scheme.deepCopy())
}