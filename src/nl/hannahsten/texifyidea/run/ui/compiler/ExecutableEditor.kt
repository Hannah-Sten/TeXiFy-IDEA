package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.ui.BrowseFolderRunnable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.SortedComboBoxModel
import com.intellij.util.ui.JBInsets
import nl.hannahsten.texifyidea.run.executable.CustomExecutable
import nl.hannahsten.texifyidea.run.executable.Executable
import nl.hannahsten.texifyidea.run.executable.SupportedExecutable
import java.awt.BorderLayout
import javax.swing.JList

/**
 * UI element to select an executable (e.g. LaTeX compiler).
 *
 * Inspired heavily by [com.intellij.execution.ui.JrePathEditor].
 *
 * @property executables: Executables for in the list.
 * @property createCustomExecutable: Custom executable for in the list (based on the path).
 *
 * @param S: The type of the default executables in the list (which are not custom executables).
 * @param E: The type of executables in the list (may be supported or custom).
 *
 * @author Sten Wessel
 */
class ExecutableEditor<in S : SupportedExecutable, E : Executable>(label: String, private val executables: Iterable<S>, private val createCustomExecutable: (path: String) -> CustomExecutable) : LabeledComponent<ComboBox<ExecutableComboBoxItem>>(), PanelWithAnchor {

    private val comboBoxModel: SortedComboBoxModel<ExecutableComboBoxItem>

    init {
        comboBoxModel = initComboBoxModel()
        populateModel(executables)
        initComboBox()

        text = label
        labelLocation = BorderLayout.WEST

        updateUI()
    }

    private fun initComboBoxModel() = object : SortedComboBoxModel<ExecutableComboBoxItem>(
        compareBy<ExecutableComboBoxItem> { it.order }.thenBy { it.presentableText.toLowerCase() }
    ) {

        override fun setSelectedItem(anItem: Any?) {
            if (anItem is AddExecutableItem) {
                component.hidePopup()
                buildBrowseRunnable().run()
                return
            }

            super.setSelectedItem(anItem)
        }
    }

    private fun populateModel(executables: Iterable<S>) {
        comboBoxModel.apply {
            addAll(executables.map { BuiltinExecutableItem(it) })
            add(AddExecutableItem(executables.firstOrNull()?.displayType ?: "something"))
        }
    }

    private fun initComboBox() {
        val comboBox = ComboBox(comboBoxModel).apply {
            isEditable = false
            renderer = object : ColoredListCellRenderer<ExecutableComboBoxItem>() {

                init {
                    ipad = JBInsets.create(1, 0)
                    myBorder = null
                }

                override fun customizeCellRenderer(
                    list: JList<out ExecutableComboBoxItem>,
                    value: ExecutableComboBoxItem?,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    value?.render(this, index == -1)
                }
            }
        }

        component = comboBox
    }

    private fun buildBrowseRunnable(): Runnable = BrowseFolderRunnable(
        "Select Alternative ${executables.first().displayType.capitalize()}",
        "Select ${executables.first().displayType} executable to run with",
        null,
        BrowseFilesListener.SINGLE_FILE_DESCRIPTOR,
        component,
        ExecutableComboBoxTextComponentAccessor { createCustomExecutable(it) }
    )

    fun setSelectedExecutable(executable: E?) {
        comboBoxModel.selectedItem = when (executable) {
            is SupportedExecutable -> findSupportedExecutable(executable)
            is CustomExecutable -> findOrCreateCustomExecutable(executable)
            else -> null
        }
    }

    fun getSelectedExecutable(): E? {
        return when (val selected = comboBoxModel.selectedItem) {
            is BuiltinExecutableItem -> selected.executable as? E
            is CustomExecutableItem -> selected.executable as? E
            else -> null
        }
    }

    private fun findSupportedExecutable(executable: SupportedExecutable): BuiltinExecutableItem? {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? BuiltinExecutableItem }
            .firstOrNull { it.executable == executable }
    }

    private fun findOrCreateCustomExecutable(executable: CustomExecutable): CustomExecutableItem {
        return comboBoxModel.items.asSequence()
            .mapNotNull { it as? CustomExecutableItem }
            .firstOrNull { it.executable.executablePath == executable.executablePath }
            ?: CustomExecutableItem(executable).also { comboBoxModel.add(it) }
    }
}