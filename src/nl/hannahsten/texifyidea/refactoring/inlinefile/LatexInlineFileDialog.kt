package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.JavaRefactoringSettings
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.inline.InlineOptionsDialog
import nl.hannahsten.texifyidea.file.LatexFile

/**
 * Creates an inline file dialog to select what to refactor. Parts have been borrowed from the java inline method dialog
 *
 * @see com.intellij.refactoring.inline.InlineMethodDialog
 *
 * @author jojo2357
 */
class LatexInlineFileDialog(
    project: Project?,
    private val myFile: LatexFile,
    private val myReference: PsiElement?,
    myInvokedOnReference: Boolean,
) :
    InlineOptionsDialog(project, true, myFile) {

    val myOccurrencesNumber: Int

    init {
        super.myInvokedOnReference = myInvokedOnReference

        myOccurrencesNumber = getNumberOfOccurrences(myFile)
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
        return if (myOccurrencesNumber > -1) "File " + myFile.name + " has " + myOccurrencesNumber + " ocurrences"
        else "File " + myFile.name
    }

    override fun getBorderTitle(): String {
        return RefactoringBundle.message("inline.method.border.title")
    }

    override fun getInlineThisText(): String {
        return "Inline this and keep the file"
    }

    override fun getInlineAllText(): String {
        return if (myFile.isWritable) "Inline all and remove the file" else "All invocations in project"
    }

    override fun getKeepTheDeclarationText(): String {
        return if (myFile.isWritable) "Inline all and keep the file" else super.getKeepTheDeclarationText()
    }

    public override fun doAction() {
        invokeRefactoring(
            LatexInlineFileProcessor(
                project, myFile, GlobalSearchScope.projectScope(myProject), myReference, isInlineThisOnly, isKeepTheDeclaration
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
            get() = "Inline File"
    }

    override fun hasHelpAction(): Boolean {
        return false
    }
}