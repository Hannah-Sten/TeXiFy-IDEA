package nl.rubensten.texifyidea.settings.labelDefiningCommands

import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.StatusText
import java.awt.event.ActionListener
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel

/**
 * JBTable is extended to set own text when table is empty and define action
 */
class MySettingsTable : JBTable {
    private var listener: ActionListener

    constructor(listener: ActionListener) : super() {
        this.listener = listener
    }
    constructor(model: TableModel, listener: ActionListener) : super(model) {
        this.listener = listener
    }
    constructor(model: TableModel, columnModel: TableColumnModel, listener: ActionListener) : super(model, columnModel) {
        this.listener = listener
    }

    /**
     * create empty text and define a clickable link to add new command
     */
    override fun getEmptyText(): StatusText {
        val test = object: StatusText(this) {
            override fun isStatusVisible(): Boolean {
                return isEmpty
            }
        }
        test.text = "No command defined"
        test.appendSecondaryText("Define new command", SimpleTextAttributes.LINK_ATTRIBUTES, listener)
        return test
    }
}
