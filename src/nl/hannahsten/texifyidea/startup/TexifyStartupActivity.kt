package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.editor.LatexSoftWrapEditorListener
import nl.hannahsten.texifyidea.run.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.run.evince.isEvinceAvailable

/**
 * @author Sten Wessel
 */
class TexifyStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        EditorFactory.getInstance().addEditorFactoryListener(
                LatexSoftWrapEditorListener(),
                project
        )

        if (SystemInfo.isLinux && isEvinceAvailable()) {
            EvinceInverseSearchListener().start()
        }
    }
}
