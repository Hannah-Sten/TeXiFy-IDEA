package nl.hannahsten.texifyidea.refactoring.inlinecommand

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
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexKeyvalPair
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.LatexRequiredParamContent
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.definitionCommand

/**
 * Recieves settings and a target and performs the requested refactoring
 *
 * @see com.intellij.refactoring.inline.InlineMethodProcessor
 *
 * @author jojo2357
 */
class LatexInlineCommandProcessor(
    myProject: Project,
    private val inlineCommand: LatexCommands,
    private val originalReference: PsiElement?,
    private val isInlineThisOnly: Boolean,
    private val isKeepTheDeclaration: Boolean,
    private val myScope: SearchScope = GlobalSearchScope.projectScope(myProject)
) : BaseRefactoringProcessor(myProject) {

    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor {
        return LatexInlineCommandDescriptor(inlineCommand)
    }

    override fun findUsages(): Array<UsageInfo> {
        val tempreferences =
            ReferencesSearch.search(inlineCommand.definitionCommand()!!, myScope).findAll().asSequence()

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
                        inlineCommand.delete()
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
        val calledRequiredArgs = psiElement.childrenOfType(LatexRequiredParamContent::class).toList()
        val calledOptionalArgs = psiElement.childrenOfType(LatexKeyvalPair::class).toList()

        val parentOptionalArgs = inlineCommand.childrenOfType(LatexKeyvalPair::class).toList()

        if (calledOptionalArgs.size > 1)
            return // error case

        if (parentOptionalArgs.size > 2)
            return // error case

        val defaultParam = if (parentOptionalArgs.size == 2) parentOptionalArgs[1].text else null
        val offsetIndex = if (defaultParam == null) 1 else 2
        val numArgs = if (parentOptionalArgs.size >= 1) Integer.parseInt(parentOptionalArgs[0].text) else 0

        val regex = "(?<!\\\\)#(\\d)".toRegex()
        val functionString = inlineCommand.lastChild.childrenOfType(LatexRequiredParamContent::class)
            .fold("") { out, some -> out + some.text }
        val totalTehxt = regex.findAll(functionString).iterator().asSequence().toSet()
        val expectedArgs = totalTehxt.maxOf { Integer.parseInt(it.value.substring(1)) }

        if (expectedArgs > numArgs)
            return

        var outText = functionString

        for (i in offsetIndex..expectedArgs) {
            outText = outText.replace("#$i", calledRequiredArgs[i - offsetIndex].text)
        }

        if (defaultParam != null) {
            if (calledOptionalArgs.size == 1)
                outText = outText.replace("#1", calledOptionalArgs[0].text)
            else
                outText = outText.replace("#1", defaultParam)
        }

        psiElement.replace(LatexPsiHelper(psiElement.project).createFromText(outText))
    }

    override fun getCommandName(): String {
        return "Inlining Command " + this.inlineCommand.name
    }
}