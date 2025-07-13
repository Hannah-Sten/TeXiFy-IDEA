package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilBase.getElementAtCaret
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.refactoring.inlinecommand.LatexInlineHandler
import nl.hannahsten.texifyidea.reference.InputFileReference
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

/**
 * Allows for inlining an include command. Does not do the inlining itself.
 *
 * @author jojo2357
 */
class LatexInlineFileHandler : LatexInlineHandler() {

    override fun canInlineElement(element: PsiElement?): Boolean {
        return Util.canInlineLatexElement(element)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        // Resolve the file to be inlined
        val inlineFile: LatexFile = Util.resolveInlineFile(element) ?: return

        val dialog = LatexInlineFileDialog(
            project,
            inlineFile,
            Util.getReference(element, editor),
            editor != null
        )

        if (dialog.getNumberOfOccurrences() > 0) {
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
            Notification(
                "LaTeX",
                "No usages found",
                "Could not find any usages for ${inlineFile.name}",
                NotificationType.ERROR
            ).notify(project)
        }
    }

    object Util {
        /**
         * This is static only so that the unit tests can use it, and also since it can be static
         */
        fun canInlineLatexElement(element: PsiElement?): Boolean {
            return when (element) {
                is LatexFile -> {
                    element.containingFile.isLatexFile()
                }

                is LatexCommands -> {
                    (element.references.filterIsInstance<InputFileReference>().isNotEmpty())
                }

                else -> false
            }
        }

        fun resolveInlineFile(element: PsiElement) = when (element) {
            // If we tried to inline the file in file view, or the argument in `input`
            is LatexFile -> element
            // If the caret was on the `input` command
            is LatexCommands -> {
                element.references.filterIsInstance<InputFileReference>()[0].resolve() as? LatexFile
            }

            else -> null
        }

        /**
         * Resolves the command that the inlining was called on. If called from file view then expected return is null
         */
        fun getReference(
            element: PsiElement,
            editor: Editor?
        ): PsiElement? {
            return if (element is LatexCommands)
                element
            else if (editor != null) {
                val ref: LatexCommands? = getElementAtCaret(editor)?.firstParentOfType(LatexCommands::class)
                if (ref != null)
                    ref
                else {
                    Log.warn("Could not find the command element for " + element.text)
                    null
                }
            }
            else null
        }
    }
}