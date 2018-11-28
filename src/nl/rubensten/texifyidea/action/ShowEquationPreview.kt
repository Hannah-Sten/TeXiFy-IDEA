package nl.rubensten.texifyidea.action

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexDisplayMath
import nl.rubensten.texifyidea.psi.LatexInlineMath
import nl.rubensten.texifyidea.ui.EquationPreviewToolWindow
import nl.rubensten.texifyidea.ui.PreviewFormUpdater
import nl.rubensten.texifyidea.util.inMathContext

/**
 * @author Sergei Izmailov
 */
class ShowEquationPreview : EditorAction("Equation preview", TexifyIcons.EQUATION_PREVIEW) {

    companion object {

        @JvmStatic
        val FORM_KEY = Key<PreviewFormUpdater>("updater")
    }

    override fun actionPerformed(file: VirtualFile, project: Project, textEditor: TextEditor) {
        val element: PsiElement? = getElement(file, project, textEditor) ?: return

        val outerMathEnvironment = findOuterMathEnvironment(element) ?: return

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

        val containingFile = outerMathEnvironment.containingFile
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(containingFile)
        val textOffset = outerMathEnvironment.textOffset
        val lineNumber = document?.getLineNumber(textOffset) ?: 0;
        val displayName = "${containingFile.name}:${lineNumber + 1}"

        val contentFactory = ContentFactory.SERVICE.getInstance()
        val contentCount = toolWindow.contentManager.contentCount

        var replaced = false
        for (i in 0 until contentCount) {
            val content = toolWindow.contentManager.getContent(i) ?: continue
            if (content.isPinned.not()) {
                val form = content.getUserData(FORM_KEY) ?: continue
                form.setEquationText(outerMathEnvironment.text)
                content.displayName = displayName
                replaced = true
                break
            }
        }

        if (!replaced) {
            val previewToolWindow = EquationPreviewToolWindow()
            val newContent = contentFactory.createContent(
                    previewToolWindow.content,
                    displayName,
                    true
            )
            toolWindow.contentManager.addContent(newContent)
            val updater = PreviewFormUpdater(previewToolWindow.form)
            newContent.putUserData(FORM_KEY, updater)
            updater.setEquationText(outerMathEnvironment.text)
        }
        toolWindow.activate(null)
    }

    private fun findOuterMathEnvironment(element: PsiElement?): PsiElement? {
        var element1 = element
        var outerMathEnvironment: PsiElement? = null

        while (element1 != null) {
            // get to parent which is *IN* math content
            while (element1 != null && element1.inMathContext().not()) {
                element1 = element1.parent
            }
            // find the marginal element which is NOT IN math content
            while (element1 != null && element1.inMathContext()) {
                element1 = element1.parent
            }

            if (element1 != null) {
                outerMathEnvironment = when (element1.parent) {
                    is LatexInlineMath -> element1.parent
                    is LatexDisplayMath -> element1.parent
                    else -> element1
                }
                element1 = element1.parent
            }
        }
        return outerMathEnvironment
    }
}