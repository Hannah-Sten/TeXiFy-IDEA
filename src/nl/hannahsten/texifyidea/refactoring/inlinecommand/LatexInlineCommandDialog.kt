package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.parser.definitionCommand

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
    invokedOnReference: Boolean,
) :
    LatexInlineDialog(project, myDefinition, invokedOnReference) {

    init {
        title = "Inline Command"
        init()
    }

    override fun getNameLabelText(): String {
        return if (getNumberOfOccurrences() > -1) "Command " + myDefinition.name + " has " + getNumberOfOccurrences() + " ocurrences"
        else "Command " + myDefinition.name
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

    override fun doAction() {
        invokeRefactoring(
            LatexInlineCommandProcessor(
                project,
                myDefinition,
                myReference,
                isInlineThisOnly,
                isKeepTheDeclaration,
                GlobalSearchScope.projectScope(myProject)
            )
        )
    }

    override fun getNumberOfOccurrences(): Int {
        return if (myDefinition.definitionCommand() == null) 0 else super.getNumberOfOccurrences(myDefinition.definitionCommand())
    }
}