package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiElementBase
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.impl.LatexCommandsImpl
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.isLatexFile


/**
 * Allows for inlining an include command
 *
 * @author jojo2357
 */
class LatexInlineFileHandler : InlineActionHandler() {

    override fun isEnabledForLanguage(language: Language?): Boolean {
        return language == LatexLanguage
    }

    override fun canInlineElement(element: PsiElement?): Boolean {
        val out = when (element) {
            is LatexFile -> {
                element.containingFile.isLatexFile()
            }

            is LatexCommandsImpl -> {
                Log.warn("Hello")
                ((element as LatexCommandsImpl).getName() == "\\input" && (element as PsiElementBase).canNavigateToSource())
            }

            else -> true
        }

        return out
    }

    /*override fun canInlineElementInEditor(element: PsiElement, editor: Editor): Boolean {
        getElementAtCaret(editor)
        return true
    }*/

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val inlineFile: LatexFile = when (element) {
            is LatexFile -> element
            is LatexCommandsImpl -> (element.containingFile.parent?.findFile( if ((element as LatexCommandsImpl).requiredParameters[0].matches("\\.\\w{3}$".toRegex())) (element as LatexCommandsImpl).requiredParameters[0] else (element as LatexCommandsImpl).requiredParameters[0] + ".tex") ?: return) as LatexFile
            else -> return
        }

        val dialog = LatexInlineFileDialog(project, inlineFile, if (element is LatexCommandsImpl) element else null, editor != null)

        if (dialog.myOccurrencesNumber > 0) {
            if (ApplicationManager.getApplication().isUnitTestMode) {
                try {
                    dialog.doAction()
                }
                finally {
                    dialog.close(DialogWrapper.OK_EXIT_CODE, true)
                }
            }
            else {
                dialog.show()
            }
        }
        else {
            // TODO No ocurrences, what to do?
        }
    }
}