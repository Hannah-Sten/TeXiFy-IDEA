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
import nl.hannahsten.texifyidea.psi.LatexCommandWithParams
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.util.parser.definitionCommand

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
                ApplicationManager.getApplication().runWriteAction {
                    CommandProcessor.getInstance().executeCommand(myProject, {
                        replaceUsage(originalReference)
                    }, commandName, "Texify")
                }
            else if (!ApplicationManager.getApplication().isUnitTestMode)
                throw IllegalStateException("Inline this requested with no original reference (" + usages.size + ")")
            // else we are probably trying to elicit no response because this is illegal
        }
        else if (usages.isNotEmpty()) {
            ApplicationManager.getApplication().runWriteAction {
                CommandProcessor.getInstance().executeCommand(myProject, {
                    var allSuccessfull = true
                    for (replaceUsage in usages) {
                        val replacereference = replaceUsage.element ?: continue
                        allSuccessfull = allSuccessfull && replaceUsage(replacereference)
                    }
                    if (!isKeepTheDeclaration && allSuccessfull)
                        inlineCommand.delete()
                }, commandName, "Texify")
            }
        }
    }

    /**
     * Does the lifting by...replacing usages with the entirety of the command to be inlined
     *
     * @param psiElement The element to remove and replace with the contents of the command
     */
    private fun replaceUsage(psiElement: PsiElement): Boolean {
        val calledRequiredArgs = (psiElement as? LatexCommandWithParams)?.requiredParametersText() ?: listOf()
        val calledOptionalArgs = (psiElement as? LatexCommandWithParams)?.getOptionalParameterMap()?.keys?.toList()?.map { it.text } ?: listOf<String>()

        val parentOptionalArgs = (inlineCommand as? LatexCommandWithParams)?.getOptionalParameterMap()?.keys?.toList()?.map { it.text } ?: listOf<String>()

        if (calledOptionalArgs.size > 1)
            return false // error case

        if (parentOptionalArgs.size > 2)
            return false // error case

        val defaultParam = if (parentOptionalArgs.size == 2) parentOptionalArgs[1] else null
        val offsetIndex = if (defaultParam == null) 1 else 2
        val numArgs = if (parentOptionalArgs.isNotEmpty()) Integer.parseInt(parentOptionalArgs[0]) else 0

        val commandParameterRegex = "(?<!\\\\)#(\\d)".toRegex()

        // Assume that the command should be replaced by the contents of the last required parameter
        val functionString = (inlineCommand as LatexCommandWithParams).parameterList.lastOrNull()?.requiredParam?.requiredParamContentList?.joinToString { it.text } ?: return false
        val parameterUsages = commandParameterRegex.findAll(functionString).toSet()
        val actualNumberOfParameters = parameterUsages.maxOfOrNull { Integer.parseInt(it.value.substring(1)) }

        if (actualNumberOfParameters != null && actualNumberOfParameters > numArgs)
            return false

        var outText = functionString

        for (i in calledRequiredArgs.indices) {
            outText = outText.replace("#${i + offsetIndex}", calledRequiredArgs[i])
        }

        if (defaultParam != null) {
            outText = if (calledOptionalArgs.size == 1)
                outText.replace("#1", calledOptionalArgs[0])
            else
                outText.replace("#1", defaultParam)
        }

        val tempFile = LatexPsiHelper(psiElement.project).createFromText(outText)
        psiElement.replace(tempFile.firstChild)

        return true
    }

    override fun getCommandName(): String {
        return "Inlining Command " + this.inlineCommand.name
    }
}