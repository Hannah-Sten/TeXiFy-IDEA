// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package nl.hannahsten.texifyidea.modules.intellij

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.projectRoots.SimpleJavaSdkType
import com.intellij.openapi.roots.ui.configuration.*
import com.intellij.openapi.roots.ui.configuration.SdkListItem.*
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.ui.ComboBoxPopupState
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Conditions
import com.intellij.util.Consumer
import java.util.*
import javax.swing.AbstractListModel
import javax.swing.ComboBoxModel
import javax.swing.JButton
import javax.swing.ListModel

/**
 * @author Eugene Zhuravlev
 * @author Thomas
 *
 * JavaUiBundle messages: https://github.com/JetBrains/intellij-community/blob/0781974c1104d2e7766522e48eff34dfe29483ba/java/idea-ui/resources/messages/JavaUiBundle.properties#L1
 */
class JdkComboBox(
    project: Project?,
    sdkModel: ProjectSdksModel,
    sdkTypeFilter: Condition<in SdkTypeId?>?,
    sdkFilter: Condition<in Sdk?>?,
    creationFilter: Condition<in SdkTypeId?>?,
    onNewSdkAdded: Consumer<in Sdk?>?
) : SdkComboBoxBase<JdkComboBox.JdkComboBoxItem?>(
    SdkListModelBuilder(
        project,
        sdkModel,
        sdkTypeFilter,
        SimpleJavaSdkType.notSimpleJavaSdkType(creationFilter),
        sdkFilter
    )
) {
    private val myOnNewSdkAdded: Consumer<Sdk>

    @get:Deprecated(
        """the popup shown by the SetUp button is now included
      directly into the popup, you may remove the button from your UI,
      see {@link #setSetupButton(JButton, Project, ProjectSdksModel, JdkComboBoxItem, Condition, boolean)}
      for more details"""
    )
    var setUpButton: JButton? = null
        private set

    @Deprecated(
        """since {@link #setSetupButton} methods are deprecated, use the
      more specific constructor to pass all parameters"""
    )
    constructor(jdkModel: ProjectSdksModel) : this(jdkModel, null) {
    }

    @Deprecated(
        """since {@link #setSetupButton} methods are deprecated, use the
      more specific constructor to pass all parameters"""
    )
    constructor(
        jdkModel: ProjectSdksModel,
        filter: Condition<in SdkTypeId?>?
    ) : this(jdkModel, filter, getSdkFilter(filter), filter, false) {
    }

    @Deprecated(
        """since {@link #setSetupButton} methods are deprecated, use the
      more specific constructor to pass all parameters
     
      The {@param addSuggestedItems} is ignored (it was not actively used) and
      it is no longer possible to have {@link SuggestedJdkItem} as a selected
      item of that ComboBox. The implementation will take care about turning a
      suggested SDKs into {@link Sdk}s"""
    )
    constructor(
        jdkModel: ProjectSdksModel,
        sdkTypeFilter: Condition<in SdkTypeId?>?,
        filter: Condition<in Sdk?>?,
        creationFilter: Condition<in SdkTypeId?>?,
        addSuggestedItems: Boolean
    ) : this(null, jdkModel, sdkTypeFilter, filter, creationFilter, null) {
    }

    override fun onModelUpdated(model: SdkListModel) {
        val previousSelection: Any? = selectedItem
        val newModel: ComboBoxModel<JdkComboBoxItem?> = JdkComboBoxModel(model)
        newModel.selectedItem = previousSelection!!
        setModel(newModel)
    }

    @Deprecated(
        """Use the overloaded constructor to pass these parameters directly to
      that class. The {@param setUpButton} is no longer used, the JdkComboBox shows
      all the needed actions in the popup. The button will be made invisible."""
    )
    fun setSetupButton(
        setUpButton: JButton?,
        project: Project?,
        jdksModel: ProjectSdksModel?,
        firstItem: JdkComboBoxItem?,
        additionalSetup: Condition<in Sdk?>?,
        moduleJdkSetup: Boolean
    ) {
        setSetupButton(setUpButton, project, jdksModel, firstItem, additionalSetup, "")
    }

    @Deprecated(
        """Use the overloaded constructor to pass these parameters directly to
      that class. The {@param setUpButton} is no longer used, the JdkComboBox shows
      all the needed actions in the popup. The button will be made invisible."""
    )
    fun setSetupButton(
        setUpButton: JButton?,
        project: Project?,
        jdksModel: ProjectSdksModel?,
        firstItem: JdkComboBoxItem?,
        additionalSetup: Condition<in Sdk?>?,
        actionGroupTitle: String?
    ) {
        this.setUpButton = setUpButton
        setUpButton!!.isVisible = false
    }

    override fun getSelectedItem(): JdkComboBoxItem? {
        return super.getSelectedItem() as JdkComboBoxItem
    }

    var selectedJdk: Sdk?
        get() {
            val selectedItem = selectedItem
            return selectedItem?.jdk
        }
        set(jdk) {
            setSelectedItem(jdk)
        }

    @Deprecated(
        """use {@link #reloadModel()}, you may also need to call
      {@link #showNoneSdkItem()} or {@link #showProjectSdkItem()} once"""
    )
    fun reloadModel(firstItem: JdkComboBoxItem?, project: Project?) {
        processFirstItem(firstItem)
        reloadModel()
    }

    private fun processFirstItem(firstItem: JdkComboBoxItem?) {
        if (firstItem is ProjectJdkComboBoxItem) {
            myModel.showProjectSdkItem()
        }
        else if (firstItem is NoneJdkComboBoxItem) {
            myModel.showNoneSdkItem()
        }
        else if (firstItem is ActualJdkComboBoxItem) {
            selectedJdk = firstItem.jdk
        }
    }

    override fun firePopupMenuWillBecomeVisible() {
        resolveSuggestionsIfNeeded()
        super.firePopupMenuWillBecomeVisible()
    }

    private fun resolveSuggestionsIfNeeded() {
        myModel.reloadActions()
        val dialogWrapper = DialogWrapper.findInstance(this)
        if (dialogWrapper == null) {
            LOG.warn(
                "Cannot find DialogWrapper parent for the JdkComboBox $this, SDK search is disabled",
                RuntimeException()
            )
            return
        }
        myModel.detectItems(this, dialogWrapper.disposable)
    }

    override fun setSelectedItem(anObject: Any?) {
        if (anObject is SdkListItem) {
            selectedItem = wrapItem(anObject)
            return
        }
        if (anObject == null) {
            val innerModel = (model as JdkComboBoxModel).myInnerModel
            var candidate = innerModel.findProjectSdkItem()
            if (candidate == null) {
                candidate = innerModel.findNoneSdkItem()
            }
            if (candidate == null) {
                candidate = myModel.showProjectSdkItem()
            }
            setSelectedItem(candidate)
            return
        }
        if (anObject is Sdk) {
            // it is a chance we have a cloned SDK instance from the model here, or an original one
            // reload model is needed to make sure we see all instances
            myModel.reloadSdks()
            (model as JdkComboBoxModel).trySelectSdk(anObject)
            return
        }
        if (anObject is InnerComboBoxItem) {
            val item = anObject.item
            if (myModel.executeAction(this, item) { newItem: SdkListItem? ->
                    setSelectedItem(newItem)
                    if (newItem is SdkItem) {
                        myOnNewSdkAdded.consume(newItem.sdk)
                    }
                }) return
        }
        if (anObject is SelectableComboBoxItem) {
            super.setSelectedItem(anObject)
        }
    }

    private class JdkComboBoxModel internal constructor(val myInnerModel: SdkListModel) :
        AbstractListModel<JdkComboBoxItem>(), ComboBoxPopupState<JdkComboBoxItem>, ComboBoxModel<JdkComboBoxItem?> {
        private var mySelectedItem: JdkComboBoxItem? = null
        override fun getSize(): Int {
            return myInnerModel.items.size
        }

        override fun getElementAt(index: Int): JdkComboBoxItem {
            return wrapItem(myInnerModel.items[index])
        }

        override fun onChosen(selectedValue: JdkComboBoxItem): ListModel<JdkComboBoxItem?>? {
            if (selectedValue is InnerComboBoxItem) {
                val inner = myInnerModel.onChosen((selectedValue as InnerComboBoxItem).item)
                return if (inner == null) null else JdkComboBoxModel(inner)
            }
            return null
        }

        override fun hasSubstep(selectedValue: JdkComboBoxItem): Boolean {
            return if (selectedValue is InnerComboBoxItem) {
                myInnerModel.hasSubstep((selectedValue as InnerComboBoxItem).item)
            }
            else false
        }

        override fun setSelectedItem(anObject: Any) {
            if (anObject !is JdkComboBoxItem) return
            if (anObject !is InnerComboBoxItem) return
            val innerItem = (anObject as InnerComboBoxItem).item
            if (!myInnerModel.items.contains(innerItem)) return
            mySelectedItem = anObject
            fireContentsChanged(this, -1, -1)
        }

        override fun getSelectedItem(): Any {
            return mySelectedItem!!
        }

        fun trySelectSdk(sdk: Sdk) {
            val item = myInnerModel.findSdkItem(sdk) ?: return
            selectedItem = wrapItem(item)
        }
    }

    private interface InnerComboBoxItem {
        val item: SdkListItem
    }

    private interface SelectableComboBoxItem
    abstract class JdkComboBoxItem {
        open val jdk: Sdk?
            get() = null
        open val sdkName: String?
            get() = null
    }

    private class InnerJdkComboBoxItem(override val item: SdkListItem) : JdkComboBoxItem(), InnerComboBoxItem {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val item = o as InnerJdkComboBoxItem
            return this.item == item.item
        }

        override fun hashCode(): Int {
            return Objects.hash(item)
        }
    }

    private class ActualJdkInnerItem(private val myItem: SdkItem) : ActualJdkComboBoxItem(
        myItem.sdk
    ), InnerComboBoxItem {
        override val item: SdkListItem
            get() = myItem
    }

    open class ActualJdkComboBoxItem(override val jdk: Sdk) : JdkComboBoxItem(), SelectableComboBoxItem {
        override fun toString(): String {
            return jdk.name
        }

        override val sdkName: String?
            get() = jdk.name

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val item = o as ActualJdkComboBoxItem
            return jdk == item.jdk
        }

        override fun hashCode(): Int {
            return Objects.hash(jdk)
        }
    }

    class ProjectJdkComboBoxItem : JdkComboBoxItem(), InnerComboBoxItem, SelectableComboBoxItem {
        override val item: SdkListItem
            get() = ProjectSdkItem()

        override fun hashCode(): Int {
            return 42
        }

        override fun equals(obj: Any?): Boolean {
            return obj is ProjectJdkComboBoxItem
        }
    }

    class NoneJdkComboBoxItem : JdkComboBoxItem(), InnerComboBoxItem, SelectableComboBoxItem {
        override val item: SdkListItem
            get() = NoneSdkItem()

        override fun toString(): String {
            return "<None>"
        }

        override fun hashCode(): Int {
            return 42
        }

        override fun equals(obj: Any?): Boolean {
            return obj is NoneJdkComboBoxItem
        }
    }

    @Deprecated(
        """this type is never visible from the {@link #getSelectedItem()} method,
      it is kept here for binary compatibility"""
    )
    class SuggestedJdkItem internal constructor(val sdkType: SdkType, val path: String) : JdkComboBoxItem() {
        override fun toString(): String {
            return path
        }
    }

    @Deprecated(
        """Use the {@link JdkComboBox} API to manage shown items,
      this call is ignored"""
    )
    override fun insertItemAt(item: JdkComboBoxItem?, index: Int) {
        super.insertItemAt(item, index)
    }

    companion object {
        private val LOG = Logger.getInstance(
            JdkComboBox::class.java
        )

        private fun unwrapItem(item: JdkComboBoxItem?): SdkListItem {
            var item = item
            if (item == null) item = ProjectJdkComboBoxItem()
            if (item is InnerComboBoxItem) {
                return (item as InnerComboBoxItem).item
            }
            throw RuntimeException("Failed to unwrap " + item.javaClass.name + ": " + item)
        }

        private fun wrapItem(item: SdkListItem): JdkComboBoxItem {
            if (item is SdkItem) {
                return ActualJdkInnerItem(item)
            }
            if (item is NoneSdkItem) {
                return NoneJdkComboBoxItem()
            }
            return if (item is ProjectSdkItem) {
                ProjectJdkComboBoxItem()
            }
            else InnerJdkComboBoxItem(item)
        }

        fun getSdkFilter(filter: Condition<in SdkTypeId?>?): Condition<Sdk?> {
            return if (filter == null) Conditions.alwaysTrue() else Condition { sdk: Sdk? -> filter.value(sdk?.sdkType) }
        }
    }

    /**
     * Creates new Sdk selector combobox
     * @param project current project (if any)
     * @param sdkModel the sdks model
     * @param sdkTypeFilter sdk types filter predicate to show
     * @param sdkFilter filters Sdk instances that are listed, it implicitly includes the {@param sdkTypeFilter}
     * @param creationFilter a filter of SdkType that allowed to create a new Sdk with that control
     * @param onNewSdkAdded a callback that is executed once a new Sdk is added to the list
     */
    init {
        myOnNewSdkAdded = Consumer { sdk: Sdk? -> onNewSdkAdded?.consume(sdk) }
        setRenderer(SdkListPresenter { (this.model as JdkComboBoxModel).myInnerModel }.forType { item: JdkComboBoxItem? ->
            unwrapItem(
                item
            )
        })
        reloadModel()
    }
}