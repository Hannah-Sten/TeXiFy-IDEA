package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.JavaRefactoringSettings
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.inline.InlineOptionsDialog
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.definitionCommand

/**
 * Creates an inline file dialog to select what to refactor. Parts have been borrowed from the java inline method dialog
 *
 * @see com.intellij.refactoring.inline.InlineMethodDialog
 *
 * @author jojo2357
 */
class LatexInlineCommandDialog(
    project: Project?,
    private val myDefinition: LatexCommands,
    private val myReference: PsiElement?,
    myInvokedOnReference: Boolean,
) :
    InlineOptionsDialog(project, true, myDefinition) {

    val myOccurrencesNumber: Int

    init {
        super.myInvokedOnReference = myInvokedOnReference

        myOccurrencesNumber = getNumberOfOccurrences(myDefinition.definitionCommand())
        title = refactoringName
        init()
    }

    override fun getNumberOfOccurrences(nameIdentifierOwner: PsiNameIdentifierOwner?): Int {
        val tempreferences = ReferencesSearch.search(nameIdentifierOwner as PsiElement).findAll().asSequence()

        return tempreferences
            .distinct()
            .toList().size
    }

    override fun allowInlineAll(): Boolean {
        return true
    }

    override fun getNameLabelText(): String {
        return if (myOccurrencesNumber > -1) "Command " + myDefinition.name + " has " + myOccurrencesNumber + " ocurrences"
        else "Command " + myDefinition.name
    }

    override fun getBorderTitle(): String {
        return RefactoringBundle.message("inline.method.border.title")
    }

    override fun getInlineThisText(): String {
        return "Inline this and keep the command"
    }

    override fun getInlineAllText(): String {
        return if (myDefinition.isWritable) "Inline all and remove the command" else "All invocations in project"
    }

    override fun getKeepTheDeclarationText(): String {
        return if (myDefinition.isWritable) "Inline all and keep the command" else super.getKeepTheDeclarationText()
    }

    public override fun doAction() {
        invokeRefactoring(
            LatexInlineCommandProcessor(
                project, myDefinition, myReference, isInlineThisOnly, isKeepTheDeclaration, GlobalSearchScope.projectScope(myProject)
            )
        )
        val settings = JavaRefactoringSettings.getInstance()
        if (myRbInlineThisOnly.isEnabled && myRbInlineAll.isEnabled) {
            settings.INLINE_METHOD_THIS = isInlineThisOnly
        }
        if (myKeepTheDeclaration != null && myKeepTheDeclaration!!.isEnabled) {
            settings.INLINE_METHOD_KEEP = isKeepTheDeclaration
        }
    }

    override fun isInlineThis(): Boolean {
        return JavaRefactoringSettings.getInstance().INLINE_METHOD_THIS
    }

    override fun isKeepTheDeclarationByDefault(): Boolean {
        return JavaRefactoringSettings.getInstance().INLINE_METHOD_KEEP
    }

    companion object {

        val refactoringName: @DialogTitle String
            get() = "Inline Command"
    }

    override fun hasHelpAction(): Boolean {
        return false
    }
}