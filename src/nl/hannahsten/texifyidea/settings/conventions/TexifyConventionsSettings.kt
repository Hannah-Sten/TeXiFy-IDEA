package nl.hannahsten.texifyidea.settings.conventions

/**
 * Model for the Texify convention settings.
 *
 * A model consists of a project state and a global state and presents a unified view on both. For example, modifying
 * the currently active scheme modifies either the project state or the global state, depending on which scheme is
 * selected.
 */
@ConsistentCopyVisibility
data class TexifyConventionsSettings internal constructor(
    private var projectState: TexifyConventionsProjectState = TexifyConventionsProjectState(
        TexifyConventionsScheme(myName = TexifyConventionsScheme.PROJECT_SCHEME_NAME)
    ),
    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()
) {

    /**
     * Copies the settings of the supplied scheme to the default (global) scheme
     *
     * The method is internal because it should only be called by the convention settings GUI components
     */
    internal fun copyToDefaultScheme(scheme: TexifyConventionsScheme) {
        globalState.schemes.single { it.name == TexifyConventionsScheme.DEFAULT_SCHEME_NAME }.copyFrom(scheme)
    }

    /**
     * Copies the settings of the supplied scheme to the project scheme
     *
     * The method is internal because it should only be called by the convention settings GUI components
     */
    internal fun copyToProjectScheme(scheme: TexifyConventionsScheme) {
        projectState.scheme.copyFrom(scheme)
    }

    /**
     * Copies the settings from another settings instance
     *
     * The method is internal because it should only be called by the convention settings GUI components
     */
    internal fun copyFrom(newState: TexifyConventionsSettings) {
        projectState = newState.projectState.deepCopy()
        globalState = newState.globalState.deepCopy()
    }

    /**
     * Returns the substates constituting this settings instance
     *
     * The method is internal because it should only be called by the [TexifyConventionsSettingsManager] for persistence
     */
    internal fun getStateCopy() = Pair(projectState.deepCopy(), globalState.deepCopy())

    /**
     * The currently active scheme.
     *
     * When changing the scheme make sure that the new scheme is either a project scheme or a known global scheme.
     */
    var currentScheme: TexifyConventionsScheme
        get() {
            return if (projectState.useProjectScheme) projectState.scheme
            else schemes.firstOrNull { it.name == globalState.selectedScheme }
                ?: throw IllegalStateException("No scheme named ${globalState.selectedScheme} exists")
        }
        set(scheme) {
            if (scheme.isProjectScheme) {
                projectState.scheme.copyFrom(scheme)
                projectState.useProjectScheme = true
            }
            else {
                val globalScheme = globalState.schemes.singleOrNull { it.name == scheme.name }
                    ?: throw IllegalArgumentException("Scheme ${scheme.name} is neither a project scheme nor a known global scheme")
                globalScheme.copyFrom(scheme)
                globalState.selectedScheme = scheme.name
                projectState.useProjectScheme = false
            }
        }

    val schemes: List<TexifyConventionsScheme>
        get() = listOfNotNull(*globalState.schemes.toTypedArray(), projectState.scheme)

    /**
     * Convenience method to retrieve the currently active label convention
     *
     * @param name the name of the command or environment of the label convention
     */
    fun getLabelConvention(name: String?, type: LabelConventionType): LabelConvention? {
        val conventionName = if (type == LabelConventionType.COMMAND) {
            name?.replace("\\", "")
        }
        else {
            name
        }
        return currentScheme.labelConventions.singleOrNull { c -> c.name == conventionName && c.type == type }
    }

    @Suppress("unused")
    fun deleteProjectLabel(index: Int) {
        projectState.scheme.labelConventions.removeAt(index)
    }

    @Suppress("unused")
    fun deleteProjectLabel(item: LabelConvention) {
        projectState.scheme.labelConventions.remove(item)
    }
}