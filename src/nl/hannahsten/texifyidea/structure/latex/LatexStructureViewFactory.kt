package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.psi.StructurePsiChangeListener

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile) = object : TreeBasedStructureViewBuilder() {

        override fun createStructureViewModel(editor: Editor?): StructureViewModel {
            val project = editor?.project ?: return LatexStructureViewModel(psiFile, editor)
            val manager = PsiManager.getInstance(project)
            manager.addPsiTreeChangeListener(StructurePsiChangeListener(project))
            return LatexStructureViewModel(psiFile, editor)
        }
    }
}