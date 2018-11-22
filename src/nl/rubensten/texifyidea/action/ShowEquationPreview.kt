package nl.rubensten.texifyidea.action

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexMathEnvironment
import nl.rubensten.texifyidea.util.*
import nl.rubensten.texifyidea.ui.EquationPreviewToolWindow
import nl.rubensten.texifyidea.ui.PreviewForm

/**
 * @author Sergei Izmailov
 */
class ShowEquationPreview : EditorAction("Equation preview", TexifyIcons.EQUATION_PREVIEW) {

    companion object {

        @JvmStatic
        lateinit var FORM_KEY: Key<PreviewForm>
    }

    init {
        FORM_KEY = Key<PreviewForm>("form")
    }

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        var element = getElement(file, project, textEditor) ?: return

        if (!LatexMathEnvironment::class.java.isAssignableFrom(element.javaClass)) {
            val parentMath = element.parentOfType(LatexMathEnvironment::class)

            if (parentMath != null) {
                element = parentMath
            } else {
                if (element.inMathContext().not()) return

                while (element.inMathContext()) {
                    element = element.parent
                }
            }
        }

        val toolWindowId = "Equation Preview"
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val previewToolWindow = EquationPreviewToolWindow(element.text)

        var toolWindow = toolWindowManager.getToolWindow(toolWindowId)
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(
                    toolWindowId,
                    true,
                    ToolWindowAnchor.BOTTOM
            )

            toolWindow.icon = TexifyIcons.EQUATION_PREVIEW
        }

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val contentCount = toolWindow.contentManager.contentCount

        var replaced = false
        for (i in 0 until contentCount) {
            val content = toolWindow.contentManager.getContent(i) ?: continue
            if (content.isPinned.not()) {
                val form = content.getUserData(FORM_KEY) ?: continue
                form.setEquationText(element.text)
                replaced = true
                break
            }
        }

        if (!replaced) {
            val newContent = contentFactory.createContent(
                    previewToolWindow.content,
                    "Equation preview",
                    true
            )
            toolWindow.contentManager.addContent(newContent)
            newContent.putUserData(FORM_KEY, previewToolWindow.form)
            previewToolWindow.form.setEquationText(element.text)
        }
    }

}