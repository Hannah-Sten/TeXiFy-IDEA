package nl.hannahsten.texifyidea.settings.conventions

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import com.intellij.util.ui.CollectionItemEditor
import com.intellij.util.ui.table.IconTableCellRenderer
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

        val typeColumnInfo =
            object : TableModelEditor.EditableColumnInfo<LabelConvention, LabelConventionType>("Type") {
                override fun valueOf(item: LabelConvention): LabelConventionType = item.type
                override fun isCellEditable(item: LabelConvention?): Boolean = false
                override fun getColumnClass(): Class<*> = LabelConventionType::class.java
                override fun getRenderer(item: LabelConvention?): TableCellRenderer {
                    return object : IconTableCellRenderer<LabelConventionType>() {
                        override fun getTableCellRendererComponent(
                            table: JTable?,
                            value: Any?,
                            selected: Boolean,
                            focus: Boolean,
                            row: Int,
                            column: Int
                        ): Component {
                            super.getTableCellRendererComponent(table, null, selected, focus, row, column)
                            //noinspection unchecked
                            icon = if (value != null) {
                                getIcon(value as LabelConventionType, table, row)
                            }
                            else {
                                null
                            }

                            if (isCenterAlignment) {
                                horizontalAlignment = CENTER
                                verticalAlignment = CENTER
                            }
                            return this
                        }

                        override fun isCenterAlignment(): Boolean = true

                        override fun getIcon(value: LabelConventionType, table: JTable?, row: Int): Icon {
                            return when (value) {
                                LabelConventionType.ENVIRONMENT -> TexifyIcons.DOT_ENVIRONMENT
                                LabelConventionType.COMMAND -> TexifyIcons.DOT_COMMAND
                            }
                        }
                    }
                }
            }


        val nameColumnInfo = object : TableModelEditor.EditableColumnInfo<LabelConvention, String>("Name") {
            override fun valueOf(item: LabelConvention): String = item.name
            override fun isCellEditable(item: LabelConvention?): Boolean = false
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
            listOf(enabledColumnInfo, typeColumnInfo, nameColumnInfo, prefixColumnInfo).toTypedArray(),
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