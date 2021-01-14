package nl.hannahsten.texifyidea.action.wizard.table

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.VerticalLayout
import nl.hannahsten.texifyidea.util.addLabeledComponent
import javax.swing.JComboBox
import javax.swing.JPanel

/**
 * Dialog to add a new column to the table.
 *
 * @author Abby Berkers
 */
class TableCreationEditColumnDialog(

        /**
         * The function to execute when clicking the OK button.
         */
        private val onOkFunction: (title: String, columnType: ColumnType, columnIndex: Int) -> Unit,

        /**
         * The index of the column being edited.
         */
        private val editingColumn: Int,

        /**
         * The name of the column that is being edited. Default is the empty string, the title of a column that does
         * not yet exist.
         */
        private val columnName: String = "",

        /**
         * The [ColumnType] of the column that is being edited. Default is a text column.
         */
        private val columnType: ColumnType = ColumnType.TEXT_COLUMN
) {

    init {
        DialogBuilder().apply {
            // Text field for the name of the column, with the old name of the editing column filled in.
            val txtColumnName = JBTextField(columnName)

            // A combobox to select the column type.
            val cboxColumnType = JComboBox(ColumnType.values().map { it.displayName }.toTypedArray())
            cboxColumnType.selectedIndex = ColumnType.values().indexOf(columnType)

            // Add UI elements.
            val panel = JPanel(VerticalLayout(8)).apply {
                addLabeledComponent(txtColumnName, "Column title:", labelWidth = 96, leftPadding = 0)
                addLabeledComponent(cboxColumnType, "Column type:", labelWidth = 96, leftPadding = 0)
            }
            setCenterPanel(panel)
            setPreferredFocusComponent(txtColumnName)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }

            if (columnName.isBlank()) {
                title("Add column")
            }
            else {
                title("Edit column")
            }

            if (show() == DialogWrapper.OK_EXIT_CODE) {
                onOkFunction(txtColumnName.text, ColumnType.values()[cboxColumnType.selectedIndex], editingColumn)
            }
        }
    }
}