package nl.rubensten.texifyidea.action.insert

import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.action.InsertEditorAction

class InsertTable(before: String? = "", after: String? = "") :
        InsertEditorAction("Table", TexifyIcons.STATS, before, after)