package nl.rubensten.texifyidea.action.insert

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.action.InsertEditorAction

class InsertTable(tableAsString: String? = "") :
        InsertEditorAction("Table", TexifyIcons.STATS, tableAsString, "") // The icon is never used.