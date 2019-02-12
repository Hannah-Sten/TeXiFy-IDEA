package nl.rubensten.texifyidea.settings

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.StatusText
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel

class MySettingsTable : JBTable {
    constructor() : super()
    constructor(model: TableModel) : super(model)
    constructor(model: TableModel, columnModel: TableColumnModel) : super(model, columnModel)

    override fun getEmptyText(): StatusText {
        val test = object: StatusText(this) {
            override fun isStatusVisible(): Boolean {
                return isEmpty
            }
        }
        test.text = "No command defined"
        return test
    }
}
