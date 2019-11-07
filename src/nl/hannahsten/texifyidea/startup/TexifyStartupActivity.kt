package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import nl.hannahsten.texifyidea.editor.LatexSoftWrapEditorListener
import nl.hannahsten.texifyidea.run.linuxpdfviewer.PdfViewer
import nl.hannahsten.texifyidea.run.linuxpdfviewer.evince.EvinceInverseSearchListener
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * @author Sten Wessel
 */
class TexifyStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        EditorFactory.getInstance().addEditorFactoryListener(
                LatexSoftWrapEditorListener(),
                project
        )

        if (SystemInfo.isLinux && TexifySettings.getInstance().pdfViewer == PdfViewer.EVINCE) {
            EvinceInverseSearchListener().start()
        }
    }
}
