package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.structure.filter.*

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewModel(
    psiFile: PsiFile,
    editor: Editor?
) : StructureViewModelBase(psiFile, editor, LatexStructureViewElement(psiFile)), ElementInfoProvider {

    companion object {

        private val sorterArray = arrayOf(Sorter.ALPHA_SORTER)
        private val filterArray = arrayOf(
            IncludesFilter(),
            SectionFilter(),
            CommandDefinitionFilter(),
            LabelFilter(),
            BibitemFilter()
        )
    }

    override fun getSorters() = sorterArray

    override fun getFilters() = filterArray

    override fun isAlwaysShowsPlus(structureViewTreeElement: StructureViewTreeElement) = false

    override fun isAlwaysLeaf(structureViewTreeElement: StructureViewTreeElement) = false
}