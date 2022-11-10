package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import nl.hannahsten.texifyidea.file.LatexFile

/**
 * Recieves settings and a target and performs the requested refactoring
 *
 * @see com.intellij.refactoring.inline.InlineMethodProcessor
 *
 * @author jojo2357
 */
class LatexInlineFileProcessor(
    myProject: Project,
    private val inlineFile: LatexFile,
    private val originalReference: PsiElement?,
    private val isInlineThisOnly: Boolean,
    private val isKeepTheDeclaration: Boolean,
    private val myScope: SearchScope = GlobalSearchScope.projectScope(myProject)
) : BaseRefactoringProcessor(myProject) {

    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor {
        return LatexInlineFileDescriptor(inlineFile)
    }

    override fun findUsages(): Array<UsageInfo> {
        val tempreferences = ReferencesSearch.search(inlineFile, myScope).findAll().asSequence()

        return tempreferences
            .distinct()
            .map(PsiReference::getElement)
            .map(::UsageInfo)
            .toList()
            .toTypedArray()
    }

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        if (isInlineThisOnly) {
            if (originalReference != null)
                replaceUsage(originalReference)
            else if (!ApplicationManager.getApplication().isUnitTestMode)
                throw IllegalStateException("Inline this requested with no original reference (" + usages.size + ")")
            // else we are probably trying to elicit no response because this is illegal
        }
        else if (usages.isNotEmpty()) {
            ApplicationManager.getApplication().runWriteAction {
                CommandProcessor.getInstance().executeCommand(myProject, {
                    for (replaceUsage in usages) {
                        val replacereference = replaceUsage.element ?: continue
                        replaceUsage(replacereference)
                    }
                    if (!isKeepTheDeclaration)
                        inlineFile.delete()
                }, commandName, "Texify")
            }
        }
    }

    /**
     * Does the lifting by...replacing usages with the entirety of the file to be inlined
     *
     * @param psiElement The element to remove and replace with the contents of the file
     */
    private fun replaceUsage(psiElement: PsiElement) {
        val root = psiElement.replace(
            inlineFile.children[0]
        )

        for (i in 1 until inlineFile.children.size) {
            psiElement.containingFile.addAfter(inlineFile.children[i], root)
        }
    }

    override fun getCommandName(): String {
        return "Inlining File " + this.inlineFile.name
    }
}