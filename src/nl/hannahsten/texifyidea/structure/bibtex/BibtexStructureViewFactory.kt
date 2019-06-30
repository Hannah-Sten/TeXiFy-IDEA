package nl.hannahsten.texifyidea.structure.bibtex

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
open class BibtexStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(file: PsiFile) = object : TreeBasedStructureViewBuilder() {

        override fun createStructureViewModel(editor: Editor?): StructureViewModel {
            val project = editor?.project ?: return BibtexStructureViewModel(file, editor)
            val manager = PsiManager.getInstance(project)
            manager.addPsiTreeChangeListener(StructurePsiChangeListener(project))

            return BibtexStructureViewModel(file, editor)
        }
    }
}