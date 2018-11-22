package nl.rubensten.texifyidea.action

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexDisplayMath
import nl.rubensten.texifyidea.psi.LatexInlineMath
import nl.rubensten.texifyidea.util.*
import nl.rubensten.texifyidea.ui.EquationPreviewToolWindow
import nl.rubensten.texifyidea.ui.PreviewFormUpdater

/**
 * @author Sergei Izmailov
 */
class ShowEquationPreview : EditorAction("Equation preview", TexifyIcons.EQUATION_PREVIEW) {

    companion object {

        @JvmStatic
        val FORM_KEY = Key<PreviewFormUpdater>("updater")
    }

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        var element: PsiElement? = getElement(file, project, textEditor) ?: return

        var outerMathEnvironment: PsiElement? = null

        while (element != null) {
            // get to parent which is *IN* math content
            while (element != null && element.inMathContext().not()) {
                element = element.parent
            }
            // find the marginal element which is NOT IN math content
            while (element != null && element.inMathContext()) {
                element = element.parent
            }

            if (element != null) {
                outerMathEnvironment = when (element.parent) {
                    is LatexInlineMath -> element.parent
                    is LatexDisplayMath -> element.parent
                    else -> element
                }
                element = element.parent
            }
        }
        outerMathEnvironment ?: return

        val toolWindowId = "Equation Preview"
        val toolWindowManager = ToolWindowManager.getInstance(project)

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
                form.setEquationText(outerMathEnvironment.text)
                replaced = true
                break
            }
        }

        if (!replaced) {
            val previewToolWindow = EquationPreviewToolWindow()
            val newContent = contentFactory.createContent(
                    previewToolWindow.content,
                    "Equation preview",
                    true
            )
            toolWindow.contentManager.addContent(newContent)
            val updater = PreviewFormUpdater(previewToolWindow.form)
            newContent.putUserData(FORM_KEY, updater)
            updater.setEquationText(outerMathEnvironment.text)
        }
    }

}