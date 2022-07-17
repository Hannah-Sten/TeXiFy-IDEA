package nl.hannahsten.texifyidea.settings.sdk

import nl.hannahsten.texifyidea.run.ui.LatexDistributionType
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import nl.hannahsten.texifyidea.util.runCommand
import org.jdom.Element
import javax.swing.JComponent

/**
 * Currently, we only support MiKTeX Docker images, but it wouldn't be too difficult to extend for other images.
 */
class DockerSdk : LatexSdk("LaTeX Docker SDK") {

    companion object {

        val isAvailable: Boolean by lazy {
            availableImages.any { it.contains("miktex") }
        }

        val availableImages: List<String> by lazy {
            runCommand("docker", "image", "ls", "--format", "table {{.Repository}}:{{.Tag}}")?.split('\n')
                ?.drop(1) // header
                ?.filter { it.isNotBlank() } ?: emptyList()
        }
    }

    override fun suggestHomePath(): String {
        // Path to Docker executable
        return "/usr/bin"
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        // Windows is not supported for now
        if (!SystemInfo.isLinux) return mutableListOf()

        // There's not really a path with 'sources' for docker except the location of images, but that's OS-dependent
        // and we only need to be able to execute the 'docker' executable anyway
        return "which docker".runCommand()?.let { output -> mutableListOf(
            output.split("/").dropLast(1).joinToString("/")
        ) } ?: mutableListOf()
    }

    override fun isValidSdkHome(path: String): Boolean {
        // For now we only support miktex images
        return "$path/docker image ls".runCommand()?.contains("miktex") ?: false
    }

    override fun getLatexDistributionType() = LatexDistributionType.DOCKER_MIKTEX

    override fun getVersionString(sdk: Sdk): String? {
        val data = sdk.sdkAdditionalData as? DockerSdkAdditionalData
        // Throw away the (possibly long) 'prefix'
        return data?.imageName?.split("/")?.lastOrNull()
    }

    override fun getExecutableName(executable: String, homePath: String): String {
        // Could be improved by prefixing docker command here, but needs to be in sync with LatexCompiler
        return executable
    }

    override fun getHomeChooserDescriptor(): FileChooserDescriptor {
        val descriptor = super.getHomeChooserDescriptor()
        descriptor.title = "Select the Directory Containing the Docker Executable"
        return descriptor
    }

    override fun getHomeFieldLabel() = "Path to directory containing Docker executable:"

    override fun getInvalidHomeMessage(path: String): String {
        return "Could not find docker executable $path/docker"
    }

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable {
        return DockerSdkConfigurable()
    }

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
        if (additionalData is DockerSdkAdditionalData) {
            additionalData.save(additional)
        }
    }

    override fun loadAdditionalData(additional: Element): SdkAdditionalData {
        return DockerSdkAdditionalData(additional)
    }

    override fun supportsCustomCreateUI() = true

    override fun showCustomCreateUI(
        sdkModel: SdkModel,
        parentComponent: JComponent,
        selectedSdk: Sdk?,
        sdkCreatedCallback: Consumer<in Sdk>
    ) {
        val chooseImageComponent = DockerSdkConfigurable().createComponent()
        val imagesComboBox = chooseImageComponent.components.filterIsInstance<ComboBox<String>>().firstOrNull()
        val dialog = DialogBuilder().apply {
            setTitle("Choose Docker Image")
            setCenterPanel(chooseImageComponent)
        }
        dialog.show()

        // Currently, we don't ask the user for this. See SdkConfigurationUtil.selectSdkHome
        val homePath = suggestHomePath()

        val sdk = SdkConfigurationUtil.createSdk(sdkModel.sdks.toMutableList(), homePath, this, DockerSdkAdditionalData(imagesComboBox?.selectedItem as? String), name)
        sdkCreatedCallback.consume(sdk)
    }
}