package nl.rubensten.texifyidea.startup

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import nl.rubensten.texifyidea.editor.LatexSoftWrapEditorListener

/**
 * @author Sten Wessel
 */
class TexifyStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        EditorFactory.getInstance().addEditorFactoryListener(
                LatexSoftWrapEditorListener(),
                project
        )
    }
}
