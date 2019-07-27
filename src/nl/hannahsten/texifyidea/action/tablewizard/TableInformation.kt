package nl.hannahsten.texifyidea.action.tablewizard

import nl.hannahsten.texifyidea.ui.tablecreationdialog.ColumnType
import nl.hannahsten.texifyidea.ui.tablecreationdialog.TableCreationTableModel

/**
 * Stores all information about a table.
 *
 * @param tableModel contains all information about the contents of the table. That is, the column names and the table
 * entries.
 * @param columnTypes contains the type of each column.
 * @param caption contains the caption to go along with the table.
 * @param label contains the label that is to be used to reference to the table.
 *
 * @author Abby Berkers
 */
data class TableInformation(
        val tableModel: TableCreationTableModel,
        val columnTypes: List<ColumnType>,
        val caption: String,
        val label: String
)