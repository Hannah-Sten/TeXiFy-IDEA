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
        // For some reason this method is called on the initial additional data as well, and if it's not valid then the user cannot change it. So, we provide null as default value and then make null a valid name.
        if (imageName != null) {
            if (runCommandWithExitCode("docker", "image", "inspect", imageName).second != 0) {
                throw ConfigurationException("Cannot find docker image $imageName")
            }
        }
    }

    fun save(element: Element) {
        if (imageName != null) {
            element.setAttribute(IMAGE, imageName)
        }
    }
}