package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.table.TableModelEditor
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.settings.TexifyConventionsScheme
import nl.hannahsten.texifyidea.settings.TexifyConventionsSchemesPanel
import nl.hannahsten.texifyidea.settings.TexifyConventionsSettings
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

enum class LabelConventionType {
    ENVIRONMENT,
    COMMAND
}

data class LabelConvention(var enabled: Boolean, var type: LabelConventionType, var name: String, var prefix: String) {

}

class TexifyConventionsConfigurable(project: Project) : SearchableConfigurable, Configurable.VariableProjectAppLevel {
    private val settings: TexifyConventionsSettings = TexifyConventionsSettings.getInstance(project)
    private val unsavedSettings: TexifyConventionsSettings = TexifyConventionsSettings.getInstance(project).deepCopy()
    private lateinit var schemesPanel: TexifyConventionsSchemesPanel
    private lateinit var mainPanel: JPanel
    private lateinit var maxSectionSize: JLongSpinner


    override fun createComponent(): JComponent? {

        schemesPanel = TexifyConventionsSchemesPanel(unsavedSettings)
        schemesPanel.addListener(object : TexifyConventionsSchemesPanel.Listener<TexifyConventionsScheme> {
            override fun onCurrentSchemeWillChange(scheme: TexifyConventionsScheme) {
                saveScheme(scheme)
            }

            override fun onCurrentSchemeHasChanged(scheme: TexifyConventionsScheme) {
                loadScheme(scheme)
            }

        })

        val prefixColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, String>("Prefix") {
            override fun valueOf(item: LabelConvention): String = item.prefix
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
                                getIcon(value, table, row)
                            }
                            else {
                                null
                            }

                            return this
                        }

                        private fun getIcon(value: LabelConvention, table: JTable?, row: Int): Icon {
                            return when (value.type) {
                                LabelConventionType.ENVIRONMENT -> TexifyIcons.DOT_ENVIRONMENT
                                LabelConventionType.COMMAND -> TexifyIcons.DOT_COMMAND
                            }
                        }
                    }
                }
            }

        val enabledColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, Boolean>("") {
            override fun getColumnClass(): Class<*> = Boolean::class.java

            override fun valueOf(item: LabelConvention): Boolean = item.enabled
            override fun setValue(item: LabelConvention, value: Boolean) {
                item.enabled = value
            }
        }

        val itemEditor = object : CollectionItemEditor<LabelConvention> {
            override fun getItemClass(): Class<out LabelConvention> = LabelConvention::class.java

            override fun clone(item: LabelConvention, forInPlaceEditing: Boolean): LabelConvention {
                TODO("Not yet implemented")
            }

            override fun isRemovable(item: LabelConvention): Boolean = false
        }

        val browsersEditor = object : TableModelEditor<LabelConvention>(
            listOf(enabledColumnInfo, nameColumnInfo, prefixColumnInfo).toTypedArray(),
            itemEditor,
            "Label Conventions"
        ) {
            override fun canCreateElement(): Boolean = false
        }
        browsersEditor.disableUpDownActions()

        maxSectionSize = JLongSpinner(minValue = 0, stepSize = 10)
        val centerPanel = panel {
            row {
                label("Maximum section size (characters)")
                maxSectionSize(grow)
            }

            titledRow("Labels") {
                row {
                    browsersEditor.createComponent()(grow)
                }
            }
        }
        browsersEditor.model.addRow(LabelConvention(false, LabelConventionType.ENVIRONMENT, "lstlisting", "lst"))
        browsersEditor.model.addRow(LabelConvention(true, LabelConventionType.COMMAND, "section", "sec"))

        mainPanel = JPanel(BorderLayout())
        mainPanel.add(schemesPanel, BorderLayout.NORTH)
        mainPanel.add(centerPanel, BorderLayout.CENTER)
        loadSettings(settings)
        return mainPanel
    }

    fun loadScheme(scheme: TexifyConventionsScheme) {
        maxSectionSize.value = scheme.maxSectionSize
    }

    fun saveScheme(scheme: TexifyConventionsScheme) {
        scheme.maxSectionSize = maxSectionSize.value
    }

    fun loadSettings(settings: TexifyConventionsSettings) {
        unsavedSettings.loadState(settings.deepCopy())
        loadScheme(unsavedSettings.currentScheme)
        schemesPanel.updateComboBoxList()
    }

    private fun saveSettings(settings: TexifyConventionsSettings) {
        saveScheme(unsavedSettings.currentScheme)
        settings.loadState(unsavedSettings.deepCopy())
    }

    override fun isModified(): Boolean = settings.deepCopy().also { saveSettings(it) } != settings

    override fun reset() = loadSettings(settings)

    override fun apply() {
        saveSettings(settings)
    }

    override fun getDisplayName() = "Conventions"

    override fun getId() = "TexifyConventionsConfigurable"

    override fun isProjectLevel(): Boolean =
        ::schemesPanel.isInitialized && schemesPanel.model.settings.currentScheme.isProjectScheme()

}