package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.layout.panel
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.table.TableModelEditor
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.settings.TexifyConventionsSchemesPanel
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
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

    private lateinit var schemesPanel: TexifyConventionsSchemesPanel
    private lateinit var mainPanel: JPanel
    private lateinit var maxSectionSize: JBIntSpinner
    private lateinit var labelConventionsTable: TableModelEditor<LabelConvention>

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

        val prefixColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, String>("Prefix") {
            override fun valueOf(item: LabelConvention): String = item.prefix!!
            override fun getTooltipText(): String = "The prefix labels for the given Latex element should have"
            override fun setValue(item: LabelConvention, value: String?) {
                item.prefix = value ?: ""
            }
        }

        val nameColumnInfo =
            object : TableModelEditor.EditableColumnInfo<LabelConvention, LabelConvention>("Element") {
                override fun valueOf(item: LabelConvention): LabelConvention = item
                override fun isCellEditable(item: LabelConvention?): Boolean = false
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
                            super.getTableCellRendererComponent(table, convention?.name, selected, focus, row, column)
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
            }

        val enabledColumnInfo =
            object : TableModelEditor.EditableColumnInfo<LabelConvention, Boolean>("Should have label") {
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

        val itemEditor = object : CollectionItemEditor<LabelConvention> {
            override fun getItemClass(): Class<out LabelConvention> = LabelConvention::class.java

            override fun clone(item: LabelConvention, forInPlaceEditing: Boolean): LabelConvention {
                TODO("Not yet implemented")
            }

            override fun isRemovable(item: LabelConvention): Boolean = false
        }

        labelConventionsTable = object : TableModelEditor<LabelConvention>(
            listOf(nameColumnInfo, prefixColumnInfo, enabledColumnInfo).toTypedArray(),
            itemEditor,
            "Label Conventions"
        ) {
            override fun canCreateElement(): Boolean = false
        }
        labelConventionsTable.disableUpDownActions()

        maxSectionSize = JBIntSpinner(4000, 100, Integer.MAX_VALUE)
        val centerPanel = panel {
            row {
                label("Maximum section size (characters)")
                maxSectionSize(grow)
            }

            titledRow("Labels") {
                row {
                    labelConventionsTable.createComponent()(grow)
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
    fun loadScheme(scheme: TexifyConventionsScheme) {
        maxSectionSize.value = scheme.maxSectionSize
        labelConventionsTable.model.items = scheme.labelConventions
    }

    /**
     * Transfer the view elements into the supplied scheme
     */
    fun saveScheme(scheme: TexifyConventionsScheme) {
        scheme.maxSectionSize = maxSectionSize.number
        scheme.labelConventions = labelConventionsTable.model.items
    }

    /**
     * Load the supplied settings and update the view component to reflect the setting values
     */
    fun loadSettings(settings: TexifyConventionsSettings) {
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