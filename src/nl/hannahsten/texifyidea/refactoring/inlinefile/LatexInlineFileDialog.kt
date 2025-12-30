package nl.hannahsten.texifyidea.refactoring.inlinefile

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.refactoring.inlinecommand.LatexInlineDialog

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
    invokedOnReference: Boolean,
) :
    LatexInlineDialog(project, myFile, invokedOnReference) {

    init {
        title = refactoringName
        init()
    }

    override fun getNameLabelText(): String = if (getNumberOfOccurrences() > -1) "File " + myFile.name + " has " + getNumberOfOccurrences() + " ocurrences"
    else "File " + myFile.name

    override fun getInlineThisText(): String = "Inline this and keep the file"

    override fun getInlineAllText(): String = if (myFile.isWritable) "Inline all and remove the file" else "All invocations in project"

    override fun getKeepTheDeclarationText(): String = if (myFile.isWritable) "Inline all and keep the file" else super.getKeepTheDeclarationText()

    override fun doAction() {
        invokeRefactoring(
            LatexInlineFileProcessor(
                project,
                myFile,
                myReference,
                isInlineThisOnly,
                isKeepTheDeclaration,
                GlobalSearchScope.projectScope(myProject)
            )
        )
    }

    override fun getNumberOfOccurrences(): Int = super.getNumberOfOccurrences(myFile)

    companion object {

        val refactoringName: @DialogTitle String
            get() = "Inline File"
    }
}