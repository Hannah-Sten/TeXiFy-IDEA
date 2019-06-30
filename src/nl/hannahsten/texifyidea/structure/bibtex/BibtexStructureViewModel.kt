package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.structure.filter.EntryFilter
import nl.hannahsten.texifyidea.structure.filter.PreambleFilter
import nl.hannahsten.texifyidea.structure.filter.StringFilter

/**
 * @author Hannah Schellekens
 */
open class BibtexStructureViewModel(
        val file: PsiFile,
        theEditor: Editor?
) : StructureViewModelBase(file, theEditor, BibtexStructureViewElement(file)), ElementInfoProvider {

    companion object {

        val sorterArray = arrayOf(Sorter.ALPHA_SORTER)
        val filterArray = arrayOf(EntryFilter, StringFilter, PreambleFilter)
    }

    override fun getSorters() = sorterArray

    override fun getFilters() = filterArray

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?) = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement?) = false
}