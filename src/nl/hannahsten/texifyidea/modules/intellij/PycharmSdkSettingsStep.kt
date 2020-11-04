// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package nl.hannahsten.texifyidea.modules.intellij

import com.intellij.CommonBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.modules.intellij.JdkComboBox.ActualJdkComboBoxItem
import nl.hannahsten.texifyidea.modules.intellij.JdkComboBox.ProjectJdkComboBoxItem
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Dmitry Avdeev
 *
 * todo [SdkSettingsStep] for non-IJ IDE
 * because the project generator peer does not have a WizardContext/SettingsStep/Project at that moment
 */
class PycharmSdkSettingsStep(
    moduleBuilder: ModuleBuilder,
    sdkTypeIdFilter: Condition<in SdkTypeId?>,
    sdkFilter: Condition<in Sdk?>?
) : ModuleWizardStep() {
    private val myComboBox: ComboBox<Sdk>
    private val myModel: ProjectSdksModel
    private val myModuleBuilder: ModuleBuilder
    val myJdkPanel: JPanel

    private fun preselectSdk(project: Project?, lastUsedSdk: String?, sdkFilter: Condition<in SdkTypeId?>) {
//        myComboBox.reloadModel()
        if (project != null) {
            val sdk = ProjectRootManager.getInstance(project).projectSdk
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.sdkType)) {
                // use project SDK
//                myComboBox.setSelectedItem(myComboBox.showProjectSdkItem())
                return
            }
        }
        if (lastUsedSdk != null) {
            val sdk = ProjectJdkTable.getInstance().findJdk(lastUsedSdk)
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.sdkType)) {
//                myComboBox.selectedJdk = sdk
                return
            }
        }

        // set default project SDK
        val defaultProject = ProjectManager.getInstance().defaultProject
        val selected = ProjectRootManager.getInstance(defaultProject).projectSdk
        if (selected != null && sdkFilter.value(selected.sdkType)) {
//            myComboBox.selectedJdk = selected
            return
        }
        var best: Sdk? = null
        val model = myComboBox.model
        for (i in 0 until model.size) {
            val item = model.getElementAt(i) as? ActualJdkComboBoxItem ?: continue
            val jdk = item.jdk ?: continue
            val jdkType = jdk.sdkType
            if (!sdkFilter.value(jdkType)) continue
            if (best == null) {
                best = jdk
                continue
            }
            val bestType = best.sdkType
            //it is in theory possible to have several SDK types here, let's just pick the first lucky type for now
            if (bestType === jdkType && bestType.versionComparator().compare(best, jdk) < 0) {
                best = jdk
            }
        }
        if (best != null) {
//            myComboBox.selectedJdk = best
        }
        else {
//            myComboBox.setSelectedItem(myComboBox.showNoneSdkItem())
        }
    }

    protected fun onSdkSelected(sdk: Sdk?) {}
    val isEmpty: Boolean
        get() = myJdkPanel.componentCount == 0

    protected fun getSdkFieldLabel(project: Project?): @NlsContexts.Label String {
//    return JavaUiBundle.message("sdk.setting.step.label", project == null ? 0 : 1);
        return "Sdk.setting.step.label" // todo
    }

    override fun getComponent(): JComponent {
        return myJdkPanel
    }

    @Throws(ConfigurationException::class)
    override fun validate(): Boolean {
        val item = myComboBox.selectedItem
        if (myComboBox.selectedItem == null && item !is ProjectJdkComboBoxItem) {
            if (Messages.showDialog(
                    noSdkMessage,
                    "title.no.jdk.specified",
                    arrayOf(CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()),
                    1,
                    Messages.getWarningIcon()
                ) != Messages.YES
            ) {
                return false
            }
        }
        try {
            myModel.apply(null, true)
        }
        catch (e: ConfigurationException) {
            //IDEA-98382 We should allow Next step if user has wrong SDK
            if (Messages.showDialog(
                    "dialog.message.0.do.you.want.to.proceed" + e.message,  // todo
                    e.title,
                    arrayOf(CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText()),
                    1,
                    Messages.getWarningIcon()
                ) != Messages.YES
            ) {
                return false
            }
        }
        return true
    }

    // todo
    protected val noSdkMessage: @DialogMessage String?
        protected get() = "prompt.confirm.project.no.jdk" // todo

    init {
        var sdkFilter = sdkFilter
        myModuleBuilder = moduleBuilder
        myModel = ProjectSdksModel()
        if (sdkFilter == null) {
            sdkFilter = JdkComboBox.getSdkFilter(sdkTypeIdFilter)
        }
        myComboBox = ComboBox<Sdk>()
        myJdkPanel = JPanel(GridBagLayout())
        myJdkPanel.isFocusable = false
        myComboBox.accessibleContext.accessibleName = myJdkPanel.accessibleContext.accessibleName
        val component = PropertiesComponent.getInstance()
        val moduleType = moduleBuilder.moduleType
        val selectedJdkProperty = "jdk.selected." + (moduleType?.id ?: "")
        myComboBox.addActionListener {
            val jdk = myComboBox.selectedItem
            if (jdk != null) {
                component.setValue(selectedJdkProperty, jdk.name)
            }
            onSdkSelected(jdk)
        }
        preselectSdk(component.getValue(selectedJdkProperty), sdkTypeIdFilter)
        myJdkPanel.add(
            myComboBox,
            GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(),
                0,
                0
            )
        )
    }
}