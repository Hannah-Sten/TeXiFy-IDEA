package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import nl.hannahsten.texifyidea.grammar.LatexLanguage

abstract class LatexInlineHandler : InlineActionHandler() {

    protected fun showDialog(
        dialog: LatexInlineDialog,
        inlineElementName: String,
        project: Project
    ) {
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
                "Could not find any usages for $inlineElementName",
                NotificationType.ERROR
            ).notify(project)
        }
    }

    override fun isEnabledForLanguage(language: Language?): Boolean {
        return language == LatexLanguage
    }
}