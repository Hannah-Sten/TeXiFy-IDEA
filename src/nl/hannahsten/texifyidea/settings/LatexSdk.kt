package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.projectRoots.*
import nl.hannahsten.texifyidea.run.latex.LatexDistribution
import nl.hannahsten.texifyidea.util.runCommand
import org.jdom.Element

/**
 * todo customstepprojectgenerator
 * todo quickfix for setting up sdk (e.g. on package/class not found)
 *
 * @author Thomas
 */
class LatexSdk : SdkType("LaTeX SDK") {
    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun suggestHomePath(): String? {
        // Copy from javadoc:
        // This method should work fast and allow running from the EDT thread.
        return null
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // todo miktex?
        val results = mutableSetOf<String>()
        val path = "which pdflatex".runCommand()
        if (path != null) {
            // Let's just assume that there is only one /bin/ in this path
            val index = path.findLastAnyOf(setOf("/bin/"))?.first ?: path.length - 1
            results.add(path.substring(0, index))
        }
        else {
            results.add("~/texlive/")
        }
        return results
    }

    override fun isValidSdkHome(path: String?): Boolean {
        if (path == null) return false

        // We expect the location of the LaTeX installation, for example ~/texlive/2020

        // If this is a valid LaTeX installation, pdflatex should be present in a subfolder in bin, e.g. $path/bin/x86_64-linux/pdflatex
        val parent = LatexDistribution.getPdflatexParentPath(path)
        return "$parent/pdflatex --version".runCommand()?.contains("pdfTeX") == true
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String?): String {
        // todo miktex
        return if (sdkHome?.contains("texlive") == true) {
            "TeX Live"
        }
        else {
            "LaTeX SDK"
        }
    }

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun getPresentableName() = "LaTeX SDK"

    override fun setupSdkPaths(sdk: Sdk) {
        val modificator = sdk.sdkModificator
        modificator.versionString = getVersionString(sdk)
        modificator.commitChanges() // save
    }

    override fun getVersionString(sdkHome: String?): String? {
        return "2020" // todo
    }
}