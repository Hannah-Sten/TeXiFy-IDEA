package nl.rubensten.texifyidea.startup

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import nl.rubensten.texifyidea.editor.LatexSoftWrapEditorListener
import nl.rubensten.texifyidea.run.evince.EvinceInverseSearchListener

/**
 * @author Sten Wessel
 */
class TexifyStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        EditorFactory.getInstance().addEditorFactoryListener(
                LatexSoftWrapEditorListener(),
                project
        )

        if (SystemInfo.isLinux) {
            EvinceInverseSearchListener().start()
        }
    }
}
