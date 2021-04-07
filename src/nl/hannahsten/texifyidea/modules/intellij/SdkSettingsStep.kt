// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package nl.hannahsten.texifyidea.modules.intellij

import com.intellij.CommonBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.modules.intellij.JdkComboBox.ActualJdkComboBoxItem
import nl.hannahsten.texifyidea.modules.intellij.JdkComboBox.ProjectJdkComboBoxItem
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Dmitry Avdeev
 */
class SdkSettingsStep(
    context: WizardContext,
    moduleBuilder: ModuleBuilder,
    sdkTypeIdFilter: Condition<in SdkTypeId?>,
    sdkFilter: Condition<in Sdk?>?
) : ModuleWizardStep() {

    private val myJdkComboBox: JdkComboBox
    private val myWizardContext: WizardContext
    private val myModel: ProjectSdksModel
    private val myModuleBuilder: ModuleBuilder
    private val myJdkPanel: JPanel

    @JvmOverloads
    constructor(
        settingsStep: SettingsStep,
        moduleBuilder: ModuleBuilder,
        sdkTypeIdFilter: Condition<in SdkTypeId?>,
        sdkFilter: Condition<in Sdk?>? = null
    ) : this(settingsStep.context, moduleBuilder, sdkTypeIdFilter, sdkFilter) {
        if (!isEmpty) {
            settingsStep.addSettingsField(getSdkFieldLabel(settingsStep.context.project), myJdkPanel)
        }
    }

    private fun preselectSdk(project: Project?, lastUsedSdk: String?, sdkFilter: Condition<in SdkTypeId?>) {
        myJdkComboBox.reloadModel()
        if (project != null) {
            val sdk = ProjectRootManager.getInstance(project).projectSdk
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.sdkType)) {
                // use project SDK
                myJdkComboBox.setSelectedItem(myJdkComboBox.showProjectSdkItem())
                return
            }
        }
        if (lastUsedSdk != null) {
            val sdk = ProjectJdkTable.getInstance().findJdk(lastUsedSdk)
            if (sdk != null && myModuleBuilder.isSuitableSdkType(sdk.sdkType)) {
                myJdkComboBox.selectedJdk = sdk
                return
            }
        }

        // set default project SDK
        val defaultProject = ProjectManager.getInstance().defaultProject
        val selected = ProjectRootManager.getInstance(defaultProject).projectSdk
        if (selected != null && sdkFilter.value(selected.sdkType)) {
            myJdkComboBox.selectedJdk = selected
            return
        }
        var best: Sdk? = null
        val model = myJdkComboBox.model
        for (i in 0 until model.size) {
            val item = model.getElementAt(i) as? ActualJdkComboBoxItem ?: continue
            val jdk = item.jdk
            val jdkType = jdk.sdkType
            if (!sdkFilter.value(jdkType)) continue
            if (best == null) {
                best = jdk
                continue
            }
            val bestType = best.sdkType
            // it is in theory possible to have several SDK types here, let's just pick the first lucky type for now
            if (bestType === jdkType && bestType.versionComparator().compare(best, jdk) < 0) {
                best = jdk
            }
        }
        if (best != null) {
            myJdkComboBox.selectedJdk = best
        }
        else {
            myJdkComboBox.setSelectedItem(myJdkComboBox.showNoneSdkItem())
        }
    }

    val isEmpty: Boolean
        get() = myJdkPanel.componentCount == 0

    private fun getSdkFieldLabel(project: Project?): String {
        return if (project == null) "Project SDK:" else "Module SDK:"
    }

    override fun getComponent(): JComponent {
        return myJdkPanel
    }

    override fun updateDataModel() {
        val project = myWizardContext.project
        val jdk = myJdkComboBox.selectedJdk
        if (project == null) {
            myWizardContext.projectJdk = jdk
        }
        else {
            myModuleBuilder.moduleJdk = jdk
        }
    }

    @Throws(ConfigurationException::class)
    override fun validate(): Boolean {
        val item = myJdkComboBox.selectedItem
        if (myJdkComboBox.selectedJdk == null && item !is ProjectJdkComboBoxItem) {
            if (Messages.showDialog(
                    noSdkMessage,
                    "No SDK Specified",
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
            // IDEA-98382 We should allow Next step if user has wrong SDK
            if (Messages.showDialog(
                    e.message + "\n\nDo you want to proceed?",
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

    private val noSdkMessage = "Do you want to create a project with no SDK assigned?\nAn SDK is required for compiling and resolving installed packages."

    init {
        var mySdkFilter = sdkFilter
        myModuleBuilder = moduleBuilder
        myWizardContext = context
        myModel = ProjectSdksModel()
        val project = myWizardContext.project
        myModel.reset(project)
        val disposable = context.disposable
        if (disposable != null) {
            val stepDisposable = Disposable { myModel.disposeUIResources() }
            Disposer.register(disposable, stepDisposable)
        }
        if (mySdkFilter == null) {
            mySdkFilter = JdkComboBox.getSdkFilter(sdkTypeIdFilter)
        }
        myJdkComboBox = JdkComboBox(myWizardContext.project, myModel, sdkTypeIdFilter, mySdkFilter, sdkTypeIdFilter, null)
        myJdkPanel = JPanel(GridBagLayout())
        myJdkPanel.isFocusable = false
        myJdkComboBox.accessibleContext.accessibleName = myJdkPanel.accessibleContext.accessibleName
        val component =
            if (project == null) PropertiesComponent.getInstance() else PropertiesComponent.getInstance(project)
        val moduleType = moduleBuilder.moduleType
        val selectedJdkProperty = "jdk.selected." + (moduleType?.id ?: "")
        myJdkComboBox.addActionListener {
            val jdk = myJdkComboBox.selectedJdk
            if (jdk != null) {
                component.setValue(selectedJdkProperty, jdk.name)
            }
        }
        preselectSdk(project, component.getValue(selectedJdkProperty), sdkTypeIdFilter)
        myJdkPanel.add(
            myJdkComboBox,
            GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(),
                0,
                0
            )
        )
    }
}