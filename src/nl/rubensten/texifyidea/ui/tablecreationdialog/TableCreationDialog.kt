package nl.rubensten.texifyidea.ui.tablecreationdialog

import com.intellij.ide.ui.laf.darcula.DarculaDefaultTableHeaderRenderer
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class TableCreationDialog(var tableAsLatex: String? = "bloop") {
    init {
        DialogBuilder().apply {
            setTitle("Table Creation Wizard")
            val table = JTable()
            val panel = JPanel()
            panel.add(table)
            setCenterPanel(panel)

            addOkAction()
            setOkOperation {
                dialogWrapper.close(0)
            }
            if (show() == DialogWrapper.OK_EXIT_CODE) {
                tableAsLatex = "tex table"
            }
        }
    }
}