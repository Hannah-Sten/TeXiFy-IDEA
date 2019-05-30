package nl.rubensten.texifyidea.action.tablewizard

import nl.rubensten.texifyidea.ui.tablecreationdialog.ColumnType
import nl.rubensten.texifyidea.ui.tablecreationdialog.TableCreationTableModel

data class TableInformation(
        val tableModel: TableCreationTableModel,
        val columnTypes: List<ColumnType>,
        val caption: String,
        val label: String
)