package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.LatexDistributionType
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.runCommand
import org.jdom.Element
import java.nio.file.Path
import java.util.function.Consumer
import javax.swing.JComponent

/**
 * Currently, we only support official MiKTeX/texlive Docker images, but it wouldn't be too difficult to extend for other images.
 */
class DockerSdk : LatexSdk(TexifyBundle.message("settings.sdk.docker.name")) {

    val defaultHomePath = "/usr/bin"

    object Availability {

        val isAvailable: Boolean by lazy {
            getAvailableImages().any { it.contains("miktex") || it.contains("texlive") }
        }

        fun getAvailableImages(): List<String> =
            runCommand("docker", "image", "ls", "--format", "table {{.Repository}}:{{.Tag}}")?.split('\n')
                ?.drop(1) // header
                ?.filter { it.isNotBlank() } ?: emptyList()
    }

    override fun suggestHomePath(path: Path): String {
        // Path to Docker executable
        return defaultHomePath
    }

    override fun suggestHomePaths(project: Project?): MutableCollection<String> {
        // Windows is not supported for now
        if (!SystemInfo.isLinux) return mutableListOf()

        // There's not really a path with 'sources' for docker except the location of images, but that's OS-dependent
        // and we only need to be able to execute the 'docker' executable anyway
        return "which docker".runCommand()?.let { output ->
            mutableListOf(
                output.split("/").dropLast(1).joinToString("/")
            )
        } ?: mutableListOf()
    }

    override fun isValidSdkHome(path: String): Boolean = "$path/docker image ls".runCommand()?.containsAny(setOf("miktex", "texlive")) == true

    override fun getLatexDistributionType(sdk: Sdk): LatexDistributionType {
        val imageName = (sdk.sdkAdditionalData as? DockerSdkAdditionalData)?.imageName
        return if (imageName?.contains("texlive") == true) {
            LatexDistributionType.DOCKER_TEXLIVE
        }
        else {
            LatexDistributionType.DOCKER_MIKTEX
        }
    }

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
        descriptor.title = TexifyBundle.message("settings.docker.home.chooser.title")
        return descriptor
    }

    override fun getHomeFieldLabel() = TexifyBundle.message("settings.docker.home.field.label")

    override fun getInvalidHomeMessage(path: String): String = TexifyBundle.message("settings.docker.invalid.home.message", path)

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable = DockerSdkConfigurable()

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
        if (additionalData is DockerSdkAdditionalData) {
            additionalData.save(additional)
        }
    }

    override fun loadAdditionalData(additional: Element): SdkAdditionalData = DockerSdkAdditionalData(additional)

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
            setTitle(TexifyBundle.message("settings.docker.choose.image.title"))
            setCenterPanel(chooseImageComponent)
        }
        dialog.show()

        // Currently, we don't ask the user for this. See SdkConfigurationUtil.selectSdkHome
        val homePath = defaultHomePath

        val sdk = SdkConfigurationUtil.createSdk(sdkModel.sdks.toMutableList(), homePath, this, DockerSdkAdditionalData(imagesComboBox?.selectedItem as? String), name)
        sdkCreatedCallback.accept(sdk)
    }
}
