package nl.hannahsten.texifyidea.structure.bibtex

import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * @author Hannah Schellekens
 */
open class BibtexStructureViewFactory : PsiStructureViewFactory {

    override fun getStructureViewBuilder(file: PsiFile) = object : TreeBasedStructureViewBuilder() {

        override fun createStructureViewModel(editor: Editor?) = BibtexStructureViewModel(file, editor)
    }
}