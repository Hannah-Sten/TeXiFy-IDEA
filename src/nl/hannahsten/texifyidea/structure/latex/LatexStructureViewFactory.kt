package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(psiFile: PsiFile) = object : TreeBasedStructureViewBuilder() {

        override fun createStructureViewModel(editor: Editor?) = LatexStructureViewModel(psiFile, editor)
    }
}