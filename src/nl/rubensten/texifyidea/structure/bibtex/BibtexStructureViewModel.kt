package nl.rubensten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.structure.filter.EntryFilter
import nl.rubensten.texifyidea.structure.filter.PreambleFilter
import nl.rubensten.texifyidea.structure.filter.StringFilter

/**
 * @author Ruben Schellekens
 */
open class BibtexStructureViewModel(
        val file: PsiFile,
        val theEditor: Editor?
) : StructureViewModelBase(file, theEditor, BibtexStructureViewElement(file)), ElementInfoProvider {

    companion object {

        val SORTERS = arrayOf(Sorter.ALPHA_SORTER)
        val FILTERS = arrayOf(EntryFilter, StringFilter, PreambleFilter)
    }

    override fun getSorters() = SORTERS

    override fun getFilters() = FILTERS

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?) = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement?) = false
}