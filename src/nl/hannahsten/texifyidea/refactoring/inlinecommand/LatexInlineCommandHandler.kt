package nl.hannahsten.texifyidea.refactoring.inlinecommand

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase.getElementAtCaret
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import nl.hannahsten.texifyidea.util.getCommandsInFiles
import nl.hannahsten.texifyidea.util.psi.*

/**
 * Allows for inlining an include command. Does not do the inlining itself.
 *
 * @author jojo2357
 */
class LatexInlineCommandHandler : LatexInlineHandler() {

    override fun canInlineElement(element: PsiElement?): Boolean {
        return canInlineLatexElement(
            element
        )
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        // Resolve the file to be inlined
        val inlineCommand: LatexCommands = resolveInlineCommandDefinition(element) ?: return
        val myReference = getReference(editor)

        val dialog = LatexInlineCommandDialog(
            project,
            inlineCommand,
            myReference,
            editor != null && inlineCommand.firstChildOfType(LatexCommands::class)?.textOffset != myReference?.textOffset
        )

        showDialog(dialog, inlineCommand.name ?: "", project)
    }

    companion object {

        /**
         * This is static only so that the unit tests can use it, and also since it can be static
         */
        fun canInlineLatexElement(element: PsiElement?): Boolean {
            if (element !is LatexCommands) {
                return false
            }

            // we will not be dealing with commands that have a map of options
            if (element.getOptionalParameterMap().any { it.value != null })
                return false

            val file = element.containingFile
            val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())

            val cmds = getCommandsInFiles(files, file)

            val candiateUserCommands = cmds.filter { it.isDefinition() && !it.isEnvironmentDefinition() }

            val canInline = candiateUserCommands.any {
                it.definitionCommand()?.name == element.name
            }

            return canInline
        }

        fun resolveInlineCommandDefinition(element: PsiElement): LatexCommands? {
            val file = element.containingFile
            val files: MutableSet<PsiFile> = HashSet(file.referencedFileSet())

            val cmds = getCommandsInFiles(files, file)

            val candiateUserCommands = cmds.filter { it.isDefinition() && !it.isEnvironmentDefinition() }

            val thisCommandDefinitions = candiateUserCommands.filter {
                it.definitionCommand()?.name == (element as LatexCommands).name
            }

            // There may be an issue where it picks the wrong definition when there are multiple redefinitions
            return thisCommandDefinitions.firstOrNull()
        }

        /**
         * Resolves the command that the inlining was called on. If called from file view then expected return is null
         */
        fun getReference(
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