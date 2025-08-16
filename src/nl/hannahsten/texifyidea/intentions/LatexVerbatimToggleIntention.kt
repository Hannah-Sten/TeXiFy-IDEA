package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.ui.PopupChooserCellRenderer
import nl.hannahsten.texifyidea.util.PackageUtils
import nl.hannahsten.texifyidea.util.files.getAllRequiredArguments
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.lookupCommand
import nl.hannahsten.texifyidea.util.runWriteCommandAction

class LatexVerbatimToggleIntention : TexifyIntentionBase("Convert to other verbatim command or environment") {

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null || !file.isLatexFile()) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return false

        return element.isVerbCommand() || element.isVerbEnvironment()
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null || !file.isLatexFile()) {
            return
        }

        val element = file.findElementAt(editor.caretModel.offset) ?: return

        val availableEnvironments: List<String> = (CommandMagic.verbatim.keys + EnvironmentMagic.verbatim)
            .filter { it != element.getName() }

        if (availableEnvironments.size == 1) {
            replaceVerbatim(element, availableEnvironments.firstOrNull()!!, file, project)
        }
        // Ask for the new environment name when there are multiple options.
        else {
            JBPopupFactory.getInstance()
                .createPopupChooserBuilder(availableEnvironments)
                .setTitle("Verbatim Environments")
                .setItemChosenCallback { replaceVerbatim(element, it, file, project) }
                .setRenderer(PopupChooserCellRenderer())
                .createPopup()
                .showInBestPositionFor(editor)
        }
    }

    private fun PsiElement.isVerbCommand() = getName() in CommandMagic.verbatim

    private fun PsiElement.isVerbEnvironment() = getName() in EnvironmentMagic.verbatim

    /**
     * Get the name of the command or environment.
     */
    private fun PsiElement.getName(): String? =
        firstParentOfType(LatexCommands::class)?.name?.removePrefix("\\")
            ?: firstParentOfType(LatexEnvironment::class)?.getEnvironmentName()

    /**
     * Replace the psi element of the old verbatim with a psi element with the new verbatim.
     */
    private fun replaceVerbatim(oldVerbatim: PsiElement, newVerbatim: String, file: PsiFile, project: Project) {
        val (content, parent, commandArgCharacter) = findVerbatimInformation(oldVerbatim)

        val newElement = constructNewVerbatim(newVerbatim, content, commandArgCharacter, project) ?: return

        runWriteCommandAction(project) {
            parent?.node?.replaceChild(parent.firstChild.node, newElement.node)

            // When the old verbatim was a command (and thus inline) and the new verbatim is an environment,
            // add a newline before the environment.
            if (oldVerbatim.isVerbCommand() && newVerbatim in EnvironmentMagic.verbatim) {
                addNewLines(parent, newElement, project)
            }

            // When the old verbatim was an environment and the new verbatim is a command, replace the new lines that
            // surrounded the environment with single spaces.
            if (oldVerbatim.isVerbEnvironment() && newVerbatim in CommandMagic.verbatim) {
                removeNewLines(parent, project)
            }

            // Check if the newly inserted verbatim depends on a package and insert the package when needed.
            findDependency(newElement)?.let {
                PackageUtils.insertUsePackage(file, it)
            }
        }
    }

    /**
     * Use the index to find if the `verbatim` element we insert depends on a package.
     */
    private fun findDependency(verbatim: PsiElement): LatexLib? =
        (verbatim as? LatexCommands)?.let {
            AllPredefined.lookupCommand(it)?.dependency
        } ?: verbatim.getName()?.let {
            AllPredefined.lookupEnv(it)?.dependency
        }

    /**
     * Get all information about [oldVerbatim] that is needed to replace it with a new verbatim.
     *
     * Returns a triple with
     * - `content: String` The text inside the command or environment. This has to be included in the new verbatim.
     * - `parent: PsiElement` The parent psi element. The child of this parent will be replaced with the new verbatim.
     * - `Boolean` Does the old verbatim command use characters other than `{}` to enclose its argument?
     *   (e.g. `\verb|test|` would set this boolean to true).
     */
    private fun findVerbatimInformation(oldVerbatim: PsiElement): Triple<String?, PsiElement?, Boolean> {
        return if (oldVerbatim.isVerbCommand()) {
            val content = oldVerbatim.firstParentOfType(LatexCommands::class)?.getAllRequiredArguments()?.firstOrNull()
            val parent = oldVerbatim.firstParentOfType(LatexCommands::class)?.parent
            Triple(content, parent, CommandMagic.verbatim[oldVerbatim.getName()] == true)
        }
        else if (oldVerbatim.isVerbEnvironment()) {
            val content = oldVerbatim.firstParentOfType(LatexEnvironment::class)?.environmentContent?.text
            val parent = oldVerbatim.firstParentOfType(LatexEnvironment::class)?.parent
            Triple(content, parent, false)
        }
        else Triple(null, null, false)
    }

    /**
     * Use [LatexPsiHelper] to create a new psi element.
     */
    private fun constructNewVerbatim(
        newVerbatim: String,
        content: String?,
        commandArgCharacter: Boolean,
        project: Project
    ): PsiElement? =
        if (newVerbatim in CommandMagic.verbatim.keys) {
            val newArgCharacter = CommandMagic.verbatim[newVerbatim] == true
            val arg = when {
                commandArgCharacter && newArgCharacter -> content
                commandArgCharacter && !newArgCharacter -> "{${content?.drop(1)?.dropLast(1)}}"
                !commandArgCharacter && newArgCharacter -> "|$content|"
                else -> "{$content}"
            }
            LatexPsiHelper(project).createFromText("\\$newVerbatim$arg")
                .findFirstChildOfType(LatexCommands::class)
        }
        else {
            val environmentContent = if (commandArgCharacter) content?.drop(1)?.dropLast(1) else content
            LatexPsiHelper(project).createFromText("\\begin{$newVerbatim}\n$environmentContent\n\\end{$newVerbatim}")
                .findFirstChildOfType(LatexEnvironment::class)
        }

    /**
     * Add a new line before and after the new environment when the current white space there is not a white space.
     */
    private fun addNewLines(parent: PsiElement?, newElement: PsiElement, project: Project) {
        if (parent?.prevSibling is PsiWhiteSpace && !parent.prevSibling.textContains('\n')) {
            LatexPsiHelper(project).createSpacing("\n")?.node?.let {
                parent.node?.addChild(it, newElement.node)
            }
        }

        if (parent?.nextSibling is PsiWhiteSpace && !parent.nextSibling.textContains('\n')) {
            LatexPsiHelper(project).createSpacing("\n")?.node?.let {
                parent.node?.addChild(it, null)
            }
        }
    }

    /**
     * Replace the new lines before and after [parent] with single spaces.
     */
    private fun removeNewLines(parent: PsiElement?, project: Project) {
        if (parent?.prevSibling is PsiWhiteSpace && parent.prevSibling.textContains('\n')) {
            parent.prevSibling.node.let { newLine ->
                LatexPsiHelper(project).createSpacing()?.node?.let {
                    newLine.psi.parent?.node?.replaceChild(newLine, it)
                }
            }
        }

        if (parent?.nextSibling is PsiWhiteSpace && parent.nextSibling.textContains('\n')) {
            parent.nextSibling.node.let { newLine ->
                LatexPsiHelper(project).createSpacing()?.node?.let {
                    newLine.psi.parent?.node?.replaceChild(newLine, it)
                }
            }
        }
    }
}