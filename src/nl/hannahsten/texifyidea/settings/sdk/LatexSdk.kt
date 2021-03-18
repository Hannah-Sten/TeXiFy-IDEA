package nl.hannahsten.texifyidea.settings.sdk

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

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String) = name

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
     * Construct a valid path to the executable, given the homepath.
     * This is different per SDK type, so therefore it is a method here.
     * Use [LatexSdkUtil.getExecutableName] if you don't know yet which SDK type should be used.
     */
    internal abstract fun getExecutableName(executable: String, homePath: String): String

    /**
     * Because we cannot add a Sources Path to the SDK programmatically, even though we know (given the SDK type)
     * where the sources path is, we implement a default on the SDK type (which will be invisible for the user unfortunately).
     */
    open fun getDefaultSourcesPath(homePath: String): VirtualFile? = null
}
