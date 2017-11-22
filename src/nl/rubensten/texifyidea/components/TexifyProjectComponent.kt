package nl.rubensten.texifyidea.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import nl.rubensten.texifyidea.editor.LatexSoftWrapEditorListener

/**
 *
 * @author Sten Wessel
 */
class TexifyProjectComponent : ProjectComponent {

    override fun initComponent() {
        EditorFactory.getInstance().addEditorFactoryListener(LatexSoftWrapEditorListener(), { })
    }
}
