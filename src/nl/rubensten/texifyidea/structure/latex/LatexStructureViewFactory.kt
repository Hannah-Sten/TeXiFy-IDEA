package nl.rubensten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.psi.StructurePsiChangeListener

/**
 * @author Ruben Schellekens
 */
class LatexStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                val project = editor!!.project
                val manager = PsiManager.getInstance(project!!)
                manager.addPsiTreeChangeListener(StructurePsiChangeListener(project))

                return LatexStructureViewModel(psiFile, editor)
            }
        }
    }
}