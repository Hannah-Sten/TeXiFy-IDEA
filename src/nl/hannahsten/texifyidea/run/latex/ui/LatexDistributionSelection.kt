package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * UI helper for the LaTeX distribution dropdown in run configurations.
 *
 * This wraps [LatexDistributionType] to provide display information for the dropdown,
 * including resolving SDK names for MODULE_SDK and PROJECT_SDK options.
 *
 * @see nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration.latexDistribution for design documentation
 */
data class LatexDistributionSelection(val distributionType: LatexDistributionType) {

    /**
     * Returns the display name for this selection.
     * For MODULE_SDK and PROJECT_SDK, shows the resolved SDK name if available.
     */
    fun getDisplayName(mainFile: VirtualFile?, project: Project): String {
        return when (distributionType) {
            LatexDistributionType.MODULE_SDK -> {
                val sdk = if (mainFile != null) {
                    LatexSdkUtil.getLatexSdkForFile(mainFile, project)
                }
                else {
                    LatexSdkUtil.getLatexProjectSdk(project)
                }
                sdk?.name ?: "<no SDK configured>"
            }
            LatexDistributionType.PROJECT_SDK -> {
                LatexSdkUtil.getLatexProjectSdk(project)?.name ?: "<no SDK configured>"
            }
            else -> distributionType.displayName
        }
    }

    /**
     * Returns a secondary label shown in gray, or null if none.
     * Used for MODULE_SDK and PROJECT_SDK to distinguish them from concrete distributions.
     */
    val secondaryLabel: String?
        get() = when (distributionType) {
            LatexDistributionType.MODULE_SDK -> "Module SDK"
            LatexDistributionType.PROJECT_SDK -> "Project SDK"
            else -> null
        }

    companion object {

        /**
         * Get all available selections for the dropdown.
         * Shows SDK-based options when SDKs are configured, plus all available distribution types.
         */
        fun getAvailableSelections(project: Project): List<LatexDistributionSelection> {
            val selections = mutableListOf<LatexDistributionSelection>()
            val hasSdks = LatexSdkUtil.getAllLatexSdks().isNotEmpty()

            // Add SDK-based options if any SDKs are configured
            if (hasSdks) {
                selections.add(LatexDistributionSelection(LatexDistributionType.MODULE_SDK))

                if (LatexSdkUtil.getLatexProjectSdk(project) != null) {
                    selections.add(LatexDistributionSelection(LatexDistributionType.PROJECT_SDK))
                }
            }

            // Add concrete distribution types that are available
            val concreteTypes = listOf(
                LatexDistributionType.TEXLIVE,
                LatexDistributionType.MIKTEX,
                LatexDistributionType.WSL_TEXLIVE,
                LatexDistributionType.DOCKER_MIKTEX,
                LatexDistributionType.DOCKER_TEXLIVE
            )

            concreteTypes
                .filter { it.isAvailable(project) }
                .forEach { selections.add(LatexDistributionSelection(it)) }

            // If nothing is available, at least show TeX Live as an option
            if (selections.isEmpty()) {
                selections.add(LatexDistributionSelection(LatexDistributionType.TEXLIVE))
            }

            return selections
        }

        /**
         * Create a selection from a persisted distribution type.
         */
        fun fromDistributionType(distributionType: LatexDistributionType): LatexDistributionSelection {
            return LatexDistributionSelection(distributionType)
        }
    }
}
