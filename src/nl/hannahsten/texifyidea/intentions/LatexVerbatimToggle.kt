package nl.hannahsten.texifyidea.intentions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.ui.PopupChooserCellRenderer
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.getAllRequiredArguments
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

class LatexVerbatimToggle : TexifyIntentionBase("Convert to other verbatim command or environment") {

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
            .filter { it != element.getVerbatimName() }

        // Ask for the new environment name.
        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(availableEnvironments)
            .setTitle("Verbatim Environments")
            .setItemChosenCallback { replaceVerbatim(element, it, file, project) }
            .setRenderer(PopupChooserCellRenderer())
            .createPopup()
            .showInBestPositionFor(editor)
    }

    private fun PsiElement.isVerbCommand() = getVerbatimName() in CommandMagic.verbatim

    private fun PsiElement.isVerbEnvironment() = getVerbatimName() in EnvironmentMagic.verbatim

    private fun PsiElement.getVerbatimName(): String? =
        firstParentOfType(LatexCommands::class)?.name?.removePrefix("\\")
        ?: firstParentOfType(LatexEnvironment::class)?.name()?.name

    private fun replaceVerbatim(oldVerbatim: PsiElement, newVerbatim: String, file: PsiFile, project: Project) {
        val (content, parent, oldArgCharacter) = if (oldVerbatim.isVerbCommand()) {
            val content = oldVerbatim.firstParentOfType(LatexCommands::class)?.getAllRequiredArguments()?.firstOrNull()
            val parent = oldVerbatim.firstParentOfType(LatexCommands::class)?.parent
            Triple(content, parent, CommandMagic.verbatim[oldVerbatim.getVerbatimName()] == true)
        }
        else if (oldVerbatim.isVerbEnvironment()) {
            val content = oldVerbatim.firstParentOfType(LatexEnvironment::class)?.environmentContent?.text
            val parent = oldVerbatim.firstParentOfType(LatexEnvironment::class)?.parent
            Triple(content, parent, false)
        }
        else Triple(null, null, false)

        val newElement = if (newVerbatim in CommandMagic.verbatim.keys) {
            val newArgCharacter = CommandMagic.verbatim[newVerbatim] == true
            val arg = when {
                oldArgCharacter && newArgCharacter -> content
                oldArgCharacter && !newArgCharacter -> "{${content?.drop(1)?.dropLast(1)}}"
                !oldArgCharacter && newArgCharacter -> "|$content|"
                else -> "{$content}"
            }
            LatexPsiHelper(project).createFromText("\\$newVerbatim$arg")
                .firstChildOfType(LatexCommands::class)
        }
        else {
            val environmentContent = if (oldArgCharacter) content?.drop(1)?.dropLast(1) else content
            LatexPsiHelper(project).createFromText("\\begin{$newVerbatim}\n$environmentContent\n\\end{$newVerbatim}")
                .firstChildOfType(LatexEnvironment::class)
        } ?: return

        runWriteCommandAction(project) {
            parent?.node?.replaceChild(parent.firstChild.node, newElement.node)

            if (oldVerbatim.isVerbCommand() && newVerbatim in EnvironmentMagic.verbatim) {
               LatexPsiHelper(project).createFromText("\n")
                    .firstChildOfType(PsiWhiteSpace::class)
                    ?.node
                    ?.let {
                        parent?.node?.addChild(it, newElement.node)
                    }
            }

            findDependency(newElement)?.let { file.insertUsepackage(it) }
        }
    }

    private fun findDependency(verbatim: PsiElement): LatexPackage? =
        (verbatim as? LatexCommands)?.let {
            LatexCommand.lookup(it)?.firstOrNull()?.dependency
        } ?: verbatim.getVerbatimName()?.let {
            Environment.lookup(it)?.dependency
        }
}