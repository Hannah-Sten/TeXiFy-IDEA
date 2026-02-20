package nl.hannahsten.texifyidea.action.wizard.table

/**
 * Stores all information about a table.
 *
 * @author Abby Berkers
 */
data class TableInformation(

    /**
     * Contains all information about the contents of the table.
     * That is, the column names and the table entries.
     */
    val tableModel: TableCreationTableModel,

    /**
     * Contains the type of each column.
     */
    val columnTypes: List<ColumnType>,

    /**
     * Contains the caption to go along with the table.
     */
    val caption: String,

    /**
     * Contains the label that is to be used to reference the table.
     */
    val label: String,

    /**
     * Define column spans, which is a cell which covers multiple columns
     */
    val columnSpanMap: ColumnSpanMap
)