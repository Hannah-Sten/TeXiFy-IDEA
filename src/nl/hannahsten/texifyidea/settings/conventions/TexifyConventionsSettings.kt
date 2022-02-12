package nl.hannahsten.texifyidea.settings.conventions

/**
 * Model for the Texify convention settings.
 *
 * A model consists of a project state and a global state and presents a unified view on both. For example, modifying
 * the currently active scheme modifies either the project state or the global state, depending on which scheme is
 * selected.
 */
data class TexifyConventionsSettings internal constructor(
    private var projectState: TexifyConventionsProjectState = TexifyConventionsProjectState(
        TexifyConventionsScheme(myName = TexifyConventionsScheme.PROJECT_SCHEME_NAME)
    ),
    private var globalState: TexifyConventionsGlobalState = TexifyConventionsGlobalState()
) {

    internal fun copyToDefaultScheme(scheme: TexifyConventionsScheme) {
        globalState.schemes =
            globalState.schemes.map { if (it.name == TexifyConventionsScheme.DEFAULT_SCHEME_NAME) scheme.deepCopy() else it }
    }

    internal fun copyToProjectScheme(scheme: TexifyConventionsScheme) {
        projectState.scheme = scheme.deepCopy()
    }

    internal fun copyFrom(newState: TexifyConventionsSettings) {
        projectState = newState.projectState.deepCopy()
        globalState = newState.globalState.deepCopy()
    }

    internal fun getStateCopy() = Pair(projectState.deepCopy(), globalState.deepCopy())

    var currentScheme: TexifyConventionsScheme
        get() {
            return if (projectState.useProjectScheme) projectState.scheme
            else schemes.firstOrNull { it.name == globalState.selectedScheme }
                ?: throw IllegalStateException("No scheme named ${globalState.selectedScheme} exists")
        }
        internal set(scheme) {
            if (scheme.isProjectScheme) {
                projectState.scheme = scheme.deepCopy()
                projectState.useProjectScheme = true
            }
            else {
                if (!globalState.schemes.any { it.name == scheme.name }) {
                    throw IllegalArgumentException("Scheme ${scheme.name} is neither a project scheme nor a known global scheme")
                }
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
}