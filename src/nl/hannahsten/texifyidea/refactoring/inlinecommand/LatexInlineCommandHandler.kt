package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase.getElementAtCaret
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.completion.LatexCommandsAndEnvironmentsCompletionProvider
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.referencedFileSet

/**
 * Allows for inlining an include command. Does not do the inlining itself.
 *
 * @author jojo2357
 */
class LatexInlineCommandHandler : InlineActionHandler() {

    override fun isEnabledForLanguage(language: Language?): Boolean {
        return language == LatexLanguage
    }

    override fun canInlineElement(element: PsiElement?): Boolean {
        return canInlineLatexElement(element)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        // Resolve the file to be inlined
        val inlineCommand: LatexCommands = resolveInlineCommandDefinition(element) ?: return

        val dialog = LatexInlineCommandDialog(
            project,
            inlineCommand,
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
                "Could not find any usages for ${inlineCommand.name}",
                NotificationType.ERROR
            ).notify(project)
        }
    }

    companion object {

        /**
         * This is static only so that the unit tests can use it, and also since it can be static
         */
        fun canInlineLatexElement(element: PsiElement?): Boolean {
            if (element !is LatexCommands) {
                return false
            }

            val file = element.containingFile
            val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())

            val cmds = getCommandsInFiles(files, file)

            val candiateUserCommands = cmds.filter { it.isDefinition() && !it.isEnvironmentDefinition() }

            val canInline = candiateUserCommands.any {
                LatexCommandsAndEnvironmentsCompletionProvider.getDefinitionName(it) == element.name
            }
            return canInline
        }

        fun resolveInlineCommandDefinition(element: PsiElement): LatexCommands? {
            val file = element.containingFile
            val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())

            val cmds = getCommandsInFiles(files, file)

            val candiateUserCommands = cmds.filter { it.isDefinition() && !it.isEnvironmentDefinition() }

            val thisCommandDefinitions = candiateUserCommands.filter {
                LatexCommandsAndEnvironmentsCompletionProvider.getDefinitionName(it) == (element as LatexCommands).name
            }
            if (thisCommandDefinitions.size != 1)
                Log.warn("Did not find a singleton definition, found " + thisCommandDefinitions.size)
            return thisCommandDefinitions.firstOrNull()
        }

        /**
         * Resolves the command that the inlining was called on. If called from file view then expected return is null
         */
        fun getReference(
            element: PsiElement,
            editor: Editor?
        ): PsiElement? {
            if (editor == null) return null
            val inliner = getElementAtCaret(editor)
            return if (inliner is LatexCommands)
                inliner
            else {
                val ref: LatexCommands? = inliner?.firstParentOfType(LatexCommands::class)
                if (ref != null)
                    ref
                else {
                    Log.warn("Could not find the command element for " + (inliner?.text ?: ""))
                    null
                }
            }
        }
    }
}