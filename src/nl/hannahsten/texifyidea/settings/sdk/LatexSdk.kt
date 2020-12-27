package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import org.jdom.Element

/**
 * Represents the location of the LaTeX installation.
 *
 * @author Thomas
 */
abstract class LatexSdk(name: String) : SdkType(name) {
    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String?) = name

    override fun getPresentableName() = name

    override fun setupSdkPaths(sdk: Sdk) {
        val modificator = sdk.sdkModificator
        modificator.versionString = getVersionString(sdk)
        modificator.commitChanges() // save
    }

    /**
     * Interface between this and [LatexDistributionType], which is used in the run configuration.
     */
    abstract fun getLatexDistributionType(): LatexDistributionType

    /**
     * If the executable (pdflatex, kpsewhich, etc.) is not in PATH, use the home path of the SDK to find it and return the full path to the executable.
     */
    abstract fun getExecutableName(executable: String, project: Project): String

    /**
     * Because we cannot add a Sources Path to the SDK programmatically, even though we know (given the SDK type)
     * where the sources path is, we implement a default on the SDK type (which will be invisible for the user unfortunately).
     */
    open fun getDefaultSourcesPath(homePath: String): VirtualFile? = null
}
