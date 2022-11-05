package nl.hannahsten.texifyidea.action.insert

import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.action.InsertEditorAction

class InsertStyledText(tableAsString: String? = "") :
    InsertEditorAction("Table", TexifyIcons.STATS, tableAsString, "") // The icon is never used.