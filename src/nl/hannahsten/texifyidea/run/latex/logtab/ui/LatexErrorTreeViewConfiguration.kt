package nl.hannahsten.texifyidea.run.latex.logtab.ui

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Save the chosen treeview filters.
 * Note that [showBibtexWarnings] only applies to bibtex warnings shown for the latex run config, like for latexmk
 */
@State(
    name = "LatexErrorTreeViewConfiguration",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
@Service(Service.Level.PROJECT)
data class LatexErrorTreeViewConfiguration(
    var showKeywordWarnings: MutableMap<LatexKeywordFilter, Boolean> = LatexKeywordFilter.values().associateWith { true }.toMutableMap(),
    var showBibtexWarnings: Boolean = true,
    var expanded: Boolean = true // Unfortunately we cannot use this, because expandAll() apparently only works in Actions
) :
    PersistentStateComponent<LatexErrorTreeViewConfiguration> {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): LatexErrorTreeViewConfiguration =
            project.getService(LatexErrorTreeViewConfiguration::class.java)
    }

    override fun getState(): LatexErrorTreeViewConfiguration {
        return this
    }

    override fun loadState(state: LatexErrorTreeViewConfiguration) {
        XmlSerializerUtil.copyBean(state, this)
    }
}