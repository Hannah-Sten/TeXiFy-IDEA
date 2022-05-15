package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.ValidatableSdkAdditionalData
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.jdom.Element

/**
 * Additional data to be saved with the [DockerSdk].
 *
 * @author Thomas
 */
class DockerSdkAdditionalData(val imageName: String?) : ValidatableSdkAdditionalData {

    companion object {
        private const val IMAGE = "image"
    }

    constructor(element: Element) : this(element.getAttributeValue(IMAGE))

    override fun checkValid(sdkModel: SdkModel?) {
        if (imageName == null) throw ConfigurationException("No image name known")
        if (runCommandWithExitCode("docker", "image", "inspect", imageName).second != 0) {
            throw ConfigurationException("Cannot find docker image $imageName")
        }
    }

    fun save(element: Element) {
        if (imageName != null) {
            element.setAttribute(IMAGE, imageName)
        }
    }
}