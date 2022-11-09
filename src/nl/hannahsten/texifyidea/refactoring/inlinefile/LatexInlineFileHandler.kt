package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiElementBase
import com.intellij.psi.util.PsiUtilBase.getElementAtCaret
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.firstParentOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * Allows for inlining an include command. Does not do the inlining itself.
 *
 * @author jojo2357
 */
class LatexInlineFileHandler : InlineActionHandler() {

    override fun isEnabledForLanguage(language: Language?): Boolean {
        return language == LatexLanguage
    }

    override fun canInlineElement(element: PsiElement?): Boolean {
        return canInlineLatexElement(element)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        // Resolve the file to be inlined
        val inlineFile: LatexFile = resolveInlineFile(element) ?: return

        val dialog = LatexInlineFileDialog(
            project,
            inlineFile,
            getReference(element, editor),
            editor != null
        )

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
            Notification(
                "LaTeX",
                "No usages found",
                "Could not find any usages for ${inlineFile.name}",
                NotificationType.ERROR
            ).notify(project)
        }
    }
}

/**
 * This is static only so that the unit tests can use it, and also since it can be static
 */
@Suppress("USELESS_CAST")
fun canInlineLatexElement(element: PsiElement?): Boolean {
    val out = when (element) {
        is LatexFile -> {
            element.containingFile.isLatexFile()
        }

        is LatexCommands -> {
            CommandMagic.includeOnlyExtensions
            ((element as LatexCommands).name == "\\input" && (element as PsiElementBase).canNavigateToSource())
        }

        else -> true
    }

    return out
}

fun resolveInlineFile(element: PsiElement) = when (element) {
    // If we tried to inline the file in file view, or the argument in `input`
    is LatexFile -> element
    // If the caret was on the `input` command
    is LatexCommands -> element.containingFile.parent?.findFile(
        // Guess that the first param is our file
        if (element.requiredParameters[0].matches(
                ".*\\.\\w{3}$".toRegex()
            )
        ) element.requiredParameters[0]
        else element.requiredParameters[0] + ".tex"
    ) as? LatexFile

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