package nl.rubensten.texifyidea.action

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.ContentFactory
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.psi.LatexMathEnvironment
import nl.rubensten.texifyidea.psi.LatexPsiUtil
import nl.rubensten.texifyidea.window.EquationPreviewToolWindow
import nl.rubensten.texifyidea.window.PreviewForm
import org.jetbrains.jsonProtocol.prepareWriteRaw
import org.picocontainer.Disposable


/**
 * @author Ruben Schellekens
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
        val editor = textEditor.editor
        val document = editor.document
        val selection = editor.selectionModel
        val start = selection.selectionStart
        val end = selection.selectionEnd
        println("Equation preview!!!")
        var element = this.getElement(file, project, textEditor)
        if (element != null) {
            if (!LatexMathEnvironment::class.java.isAssignableFrom(element.javaClass)) {
                val parent_math = LatexPsiUtil.getParentOfType(element, LatexMathEnvironment::class.java)
//                todo: recognize math environments as well (e.g. eqnarray, etc.)
                element = parent_math
            }
            if (element != null) {
                println("EQUATION!!!!")
                println(element.text)

                val tool_window_id = "Equation Preview";
                val toolWindowManager = ToolWindowManager.getInstance(project)
                val preview_window = EquationPreviewToolWindow(element.text)

                var tw = toolWindowManager.getToolWindow(tool_window_id);
                if (tw == null) {
                    tw = toolWindowManager.registerToolWindow(
                            tool_window_id,
                            true,
                            ToolWindowAnchor.BOTTOM
                    )

                    tw.icon = TexifyIcons.EQUATION_PREVIEW
                }
                val contentFactory = ContentFactory.SERVICE.getInstance()
                val content_count = tw.contentManager.contentCount;

                var replaced = false;
                for (i in 0..content_count - 1) {
                    val content = tw.contentManager.getContent(i)
                    if (content != null && !content.isPinned) {
                        val form = content.getUserData(FORM_KEY)
                        if (form != null) {
                            form.set_equation_text(element.text)
                            replaced = true
                            break
                        } else {
                            println("FORM IS NULL")
                        }
                    }
                }
                if (!replaced) {

                    val new_content = contentFactory.createContent(
                            preview_window.content,
                            "Equation preview",
                            true)
                    tw.contentManager.addContent(new_content)
                    new_content.putUserData(FORM_KEY, preview_window.form)
                    preview_window.form.set_equation_text(element.text)


                }
            } else {
                println("Not an equation!!!")
            }
        }

    }

    override fun update(e: AnActionEvent?) {
        super.update(e)
    }
}
