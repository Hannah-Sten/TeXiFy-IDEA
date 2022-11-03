package nl.hannahsten.texifyidea.refactoring

import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.usageView.UsageInfo
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.util.files.isLatexFile

/**
 * Allows for inlining an include command
 *
 * @author jojo2357
 */
class LatexInlineInclude : InlineActionHandler() {

    override fun isEnabledForLanguage(l: Language?): Boolean {
        return l == LatexLanguage
    }

    override fun canInlineElement(element: PsiElement?): Boolean {
        if ((element == null || element.containingFile == null || !element.containingFile.isLatexFile()) || element !is LatexFile) {
            return false
        }
        return true
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        // TODO add dialog to allow choosing whether or not to delete definition, select scope, etc.
        val inlineFile = element as LatexFile

        val tempreferences =
            ReferencesSearch.search(element, GlobalSearchScope.projectScope(project)).findAll().asSequence()

        val references = tempreferences
            .distinct()
            .map(PsiReference::getElement)
            .map(::UsageInfo)
            .map { it.element }
            .toList()
            .toTypedArray()

        if (references.isNotEmpty()) {
            ApplicationManager.getApplication().runWriteAction {
                CommandProcessor.getInstance().executeCommand(project, {
                    for (replacereference in references) {
                        if (replacereference == null)
                            continue
                        val root = replacereference.replace(
                            inlineFile.children[0]
                        )

                        for (i in 1 until inlineFile.children.size) {
                            replacereference.containingFile.addAfter(inlineFile.children[i], root)
                        }
                    }
                    inlineFile.delete()
                }, "Inline File", "Texify")
            }
        }
    }
}