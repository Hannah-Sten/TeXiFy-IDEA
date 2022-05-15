package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Customized UI for the SDK settings page of [DockerSdk].
 *
 * @author Thomas
 */
class DockerSdkConfigurable : AdditionalDataConfigurable {

    private var sdk: Sdk? = null
    override fun setSdk(sdk: Sdk?) { this.sdk = sdk }

    lateinit var imageName: ComboBox<String>

    override fun createComponent(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)

                    imageName = ComboBox(arrayOf("item1", "item2"))
                    add(
                        JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                            add(JBLabel("Docker image name: "))
                            add(imageName)
                        }
                    )

                }
            )
        }
    }

    override fun isModified(): Boolean {
        val data = sdk?.sdkAdditionalData as? DockerSdkAdditionalData
        return data?.imageName == this.imageName.selectedItem as? String
    }

    override fun apply() {
        if (sdk == null) return
        val newData = DockerSdkAdditionalData(this.imageName.selectedItem as String)
        sdk!!.sdkModificator.sdkAdditionalData = newData
        ApplicationManager.getApplication().runWriteAction { sdk!!.sdkModificator.commitChanges() }
    }
}