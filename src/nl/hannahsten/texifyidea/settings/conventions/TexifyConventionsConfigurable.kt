package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.table.TableModelEditor
import nl.hannahsten.texifyidea.TexifyIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

/**
 * Configurable for configuring Texify convention settings.
 *
 * The goal of this configurable is to make certain parts of the conventions currently hardcoded in the *Magic classes
 * configurable.
 *
 * The Configurable can manage settings for both, project and IDE scope. To manage the settings for the project scope,
 * the Configurable is registered as a project configurable. Changes made in the UI are transferred to a
 * [TexifyConventionsSettings] object.
 */
class TexifyConventionsConfigurable(project: Project) : SearchableConfigurable, Configurable.VariableProjectAppLevel {

    private val settingsManager: TexifyConventionsSettingsManager =
        TexifyConventionsSettingsManager.getInstance(project)

    /**
     * A settings instance to record active changes. This instance is copied to the actual instance whenever settings
     * should be applied.
     */
    private val unsavedSettings: TexifyConventionsSettings = TexifyConventionsSettings()

    // used for the selector
    private lateinit var labelCommandType: ComboBox<LabelConventionType>
    private lateinit var schemesPanel: TexifyConventionsSchemesPanel
    private lateinit var mainPanel: JPanel
    private lateinit var maxSectionSize: JBIntSpinner
    private lateinit var labelConventionsTable: TableView<LabelConvention>

    private val typeColumnInfo =
        object : TableModelEditor.EditableColumnInfo<LabelConvention, LabelConvention>("Type") {
            override fun valueOf(item: LabelConvention?): LabelConvention? = item
            override fun getColumnClass(): Class<*> = LabelConvention::class.java
            override fun getRenderer(item: LabelConvention?): TableCellRenderer {
                return object : DefaultTableCellRenderer() {
                    override fun getTableCellRendererComponent(
                        table: JTable?,
                        value: Any?,
                        selected: Boolean,
                        focus: Boolean,
                        row: Int,
                        column: Int
                    ): Component {
                        val convention = value as LabelConvention?
                        super.getTableCellRendererComponent(table, convention?.type, selected, focus, row, column)
                        //noinspection unchecked
                        icon = if (value != null) {
                            when (value.type!!) {
                                LabelConventionType.ENVIRONMENT -> TexifyIcons.DOT_ENVIRONMENT
                                LabelConventionType.COMMAND -> TexifyIcons.DOT_COMMAND
                            }
                        }
                        else {
                            null
                        }

                        return this
                    }
                }
            }

            override fun setValue(item: LabelConvention?, value: LabelConvention?) {
                if (item != null && value != null)
                    item.type = value.type
            }

            override fun getEditor(o: LabelConvention?): TableCellEditor {
                return object : AbstractTableCellEditor() {
                    override fun getCellEditorValue(): Any {
                        val labelType =
                            (labelCommandType.selectedItem ?: LabelConventionType.ENVIRONMENT) as LabelConventionType
                        return LabelConvention(false, labelType)
                    }

                    override fun getTableCellEditorComponent(
                        table: JTable,
                        value: Any,
                        isSelected: Boolean,
                        row: Int,
                        column: Int
                    ): Component {
                        if (value !is LabelConvention) {
                            labelCommandType.selectedItem = LabelConventionType.ENVIRONMENT
                        }
                        else {
                            labelCommandType.selectedItem = value.type
                        }
                        return labelCommandType
                    }
                }
            }
        }

    private val prefixColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, String>("Prefix") {
        override fun valueOf(item: LabelConvention): String = item.prefix!!
        override fun getTooltipText(): String = "The prefix labels for the given Latex element should have"
        override fun setValue(item: LabelConvention, value: String?) {
            item.prefix = value ?: ""
        }
    }

    private val nameColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, String>("Element") {
        override fun valueOf(item: LabelConvention): String = item.name!!
        override fun getColumnClass(): Class<*> = String::class.java
        override fun setValue(item: LabelConvention, value: String?) {
            item.name = value ?: ""
        }
    }

    private val enabledColumnInfo =
        object : TableModelEditor.EditableColumnInfo<LabelConvention, Boolean>("Should Have Label") {
            override fun getColumnClass(): Class<*> = Boolean::class.java
            override fun getTooltipText(): String =
                "If enabled, TeXiFy issues a warning if the given Latex element does not have a label"

            override fun valueOf(item: LabelConvention): Boolean = item.enabled
            override fun setValue(item: LabelConvention, value: Boolean) {
                item.enabled = value
            }

            override fun getMaxStringValue(): String? {
                // Give a hint that the column won't ever need more space than its label
                return super.getName()
            }
        }

    private fun createConventionsTable(): TableView<LabelConvention> {
        val model = ListTableModel<LabelConvention>(typeColumnInfo, nameColumnInfo, prefixColumnInfo, enabledColumnInfo)
        labelConventionsTable = TableView(model)

        return labelConventionsTable
    }

    /**
     * This creates that header you see at the top of the table with the plus minus, etc
     */
    private fun createMappingsTableDecorator(): JComponent {
        val panelForTable = ToolbarDecorator.createDecorator(labelConventionsTable, null)
            .setAddActionUpdater { _: AnActionEvent? -> true }
            .setAddAction { _: AnActionButton? ->
                createAddLabelConventionDialog()
            }
            .setRemoveActionUpdater { _: AnActionEvent? -> labelConventionsTable.selection.isNotEmpty() }
            .setRemoveAction { _: AnActionButton? ->
                labelConventionsTable.selectedObjects.forEach { unsavedSettings.currentScheme.labelConventions.remove(it) }
                loadScheme(unsavedSettings.currentScheme)
            }
            // Up/down actions currently have no function, other than allowing users to group them together in the UI
            .setMoveUpActionUpdater { _: AnActionEvent? -> labelConventionsTable.selectedRow > 0 }
            .setMoveUpAction {
                // take the list element above the selection block and move it to the end of the block
                val startRow = labelConventionsTable.selectedRow
                val endRow = labelConventionsTable.selectedRowCount + startRow - 1
                val holder = unsavedSettings.currentScheme.labelConventions[startRow - 1]

                unsavedSettings.currentScheme.labelConventions.removeAt(startRow - 1)
                unsavedSettings.currentScheme.labelConventions.add(endRow, holder)

                loadScheme(unsavedSettings.currentScheme)
                labelConventionsTable.setRowSelectionInterval(startRow - 1, endRow - 1)
            }
            .setMoveDownActionUpdater { _: AnActionEvent? -> labelConventionsTable.selectedRow < labelConventionsTable.rowCount - 1 }
            .setMoveDownAction {
                // take the list element below the selection block and move it to the start of the block
                val startRow = labelConventionsTable.selectedRow
                val endRow = labelConventionsTable.selectedRowCount + startRow - 1
                val holder = unsavedSettings.currentScheme.labelConventions[endRow + 1]

                unsavedSettings.currentScheme.labelConventions.removeAt(endRow + 1)
                unsavedSettings.currentScheme.labelConventions.add(startRow, holder)

                loadScheme(unsavedSettings.currentScheme)
                labelConventionsTable.setRowSelectionInterval(startRow + 1, endRow + 1)
            }
            .createPanel()
        panelForTable.preferredSize = JBDimension(-1, 200)
        return panelForTable
    }

    /**
     * In order to make it easier for the user to enter new values, we use a dialog before adding the entry to the list (might be out of view if the list is long).
     */
    private fun createAddLabelConventionDialog() {
        val typeField = ComboBox(LabelConventionType.values())
        val elementField = JBTextField().apply { preferredSize = Dimension(200, preferredSize.height) }
        val prefixField = JBTextField().apply { preferredSize = Dimension(200, preferredSize.height) }
        val labelField = JBCheckBox()

        DialogBuilder().apply {
            setCenterPanel(
                panel {
                    row("Type:") { cell(typeField) }
                    row("Environment/command name:") { cell(elementField) }
                    row("Prefix:") { cell(prefixField) }
                    row("Should have label:") { cell(labelField) }
                }
            )

            addOkAction()
            addCancelAction()
            title("Add Label Convention")

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                val newLabel = LabelConvention(
                    labelField.isSelected,
                    typeField.selectedItem as LabelConventionType,
                    elementField.text,
                    prefixField.text
                )
                unsavedSettings.currentScheme.labelConventions.add(newLabel)
                loadScheme(unsavedSettings.currentScheme)
                labelConventionsTable.setRowSelectionInterval(
                    labelConventionsTable.rowCount - 1,
                    labelConventionsTable.rowCount - 1
                )
            }
        }
    }

    private fun buildEnvironmentSelector(): ComboBox<LabelConventionType> {
        return ComboBox(LabelConventionType.values())
    }

    override fun createComponent(): JComponent {
        schemesPanel = TexifyConventionsSchemesPanel(unsavedSettings)

        /**
         * Save the current scheme and load the new scheme whenever the combobox changes
         */
        schemesPanel.addListener(object : TexifyConventionsSchemesPanel.Listener {
            override fun onCurrentSchemeWillChange(scheme: TexifyConventionsScheme) {
                saveScheme(scheme)
            }

            override fun onCurrentSchemeHasChanged(scheme: TexifyConventionsScheme) {
                loadScheme(scheme)
            }
        })

        labelConventionsTable = createConventionsTable()

        createMappingsTableDecorator()

        labelCommandType = buildEnvironmentSelector()
        labelCommandType.addItemListener {
            if (labelConventionsTable.isEditing) {
                labelConventionsTable.stopEditing()
            }
        }

        val centerPanel = panel {
            row {
                label("Maximum section size (characters)")
                spinner(1..Integer.MAX_VALUE, 500).apply {
                    maxSectionSize = component
                    component.number = 4000
                }
            }

            group("Labels") {
                row {
                    cell(
                        createMappingsTableDecorator()
                    )
                        .resizableColumn()
                        .gap(RightGap.SMALL)
                        .align(AlignX.FILL)
                }
            }
        }

        mainPanel = JPanel(BorderLayout())
        mainPanel.add(schemesPanel, BorderLayout.NORTH)
        mainPanel.add(centerPanel, BorderLayout.CENTER)
        loadSettings(settingsManager.getSettings())
        return mainPanel
    }

    /**
     * Initialize the view elements with the settings from the supplied scheme
     */
    private fun loadScheme(scheme: TexifyConventionsScheme) {
        maxSectionSize.value = scheme.maxSectionSize

        // make sure to make a copy so changes to the elements are transferred explicitly
        val items = scheme.labelConventions.map { l -> l.copy() }
        val model = ListTableModel(arrayOf(typeColumnInfo, nameColumnInfo, prefixColumnInfo, enabledColumnInfo), items)
        labelConventionsTable.model = model
    }

    /**
     * Transfer the view elements into the supplied scheme
     */
    private fun saveScheme(scheme: TexifyConventionsScheme) {
        scheme.maxSectionSize = maxSectionSize.number
        scheme.labelConventions.clear()
        scheme.labelConventions.addAll(labelConventionsTable.items)
    }

    /**
     * Load the supplied settings and update the view component to reflect the setting values
     */
    private fun loadSettings(settings: TexifyConventionsSettings) {
        unsavedSettings.copyFrom(settings)
        loadScheme(unsavedSettings.currentScheme)
        schemesPanel.updateComboBoxList()
    }

    override fun isModified(): Boolean {
        saveScheme(unsavedSettings.currentScheme)
        return settingsManager.getSettings() != unsavedSettings
    }

    override fun reset() = loadSettings(settingsManager.getSettings())

    /**
     * Persist the currently active settings
     */
    override fun apply() {
        saveScheme(unsavedSettings.currentScheme)
        settingsManager.saveSettings(unsavedSettings)
    }

    override fun getDisplayName() = "Conventions"

    override fun getId() = "TexifyConventionsConfigurable"

    override fun isProjectLevel(): Boolean =
        ::schemesPanel.isInitialized && schemesPanel.model.settings.currentScheme.isProjectScheme
}