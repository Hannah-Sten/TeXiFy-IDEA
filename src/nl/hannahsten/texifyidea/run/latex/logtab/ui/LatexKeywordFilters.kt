package nl.hannahsten.texifyidea.run.latex.logtab.ui

import nl.hannahsten.texifyidea.TexifyIcons
import javax.swing.Icon

enum class LatexKeywordFilters(vararg val triggers: Set<String>, val icon: Icon) {
    OVERFULL_HBOX(setOf("overfull \\hbox", "underfull \\hbox", "overfull \\vbox", "underfull \\vbox"), icon = TexifyIcons.STATS)
}