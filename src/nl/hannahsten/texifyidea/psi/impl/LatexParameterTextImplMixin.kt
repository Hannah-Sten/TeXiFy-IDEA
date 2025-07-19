package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.BibtexIdReference
import nl.hannahsten.texifyidea.reference.LatexGlossaryReference
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.isFigureLabel
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.firstParentOfType
import nl.hannahsten.texifyidea.util.parser.parentOfType
import nl.hannahsten.texifyidea.util.parser.remove

abstract class LatexParameterTextImplMixin(node: ASTNode) : LatexParameterText, ASTWrapperPsiElement(node) {

//    override fun getReferences(): Array<out PsiReference?> {
//
//    }

    /**
     * If the normal text is the parameter of a \ref-like command, get the references to the label declaration.
     */
    override fun getReference(): PsiReference? {
        val project = this.project
        if (DumbService.isDumb(project)) {
            // we cannot resolve references, so return empty array
            return null
        }
        val command = this.firstParentOfType<LatexCommands>() ?: return null
        val name = command.name ?: return null
        if (name in CommandMagic.bibliographyReference) {
            // First check if the command is a bibliography reference, then we return a reference to the bibtex id
            return BibtexIdReference(this)
        }
        if (name in CommandMagic.glossaryReference) {
            // If the command is a glossary reference, we return a reference to the glossary label
            return LatexGlossaryReference(this)
        }
        if (name in CommandMagic.reference) {
            // If the command is a reference, we return a reference to the label parameter
            // TODO: allow custom reference
            return LatexLabelParameterReference(this)
        }

        return null
    }

    override fun getNameIdentifier(): PsiElement? {
        // Because we do not want to trigger the NonAsciiCharactersInspection when the LatexParameterText is not an identifier
        // (think non-ASCII characters in a \section command), we return null here when the this is not an identifier
        // It is important not to return null for any identifier, otherwise exceptions like "Throwable: null byMemberInplaceRenamer" may occur

        val name = this.firstParentOfType(LatexCommands::class)?.name

        // reference
        if (name in CommandMagic.labels || name in CommandMagic.reference || name in CommandMagic.labelAsParameter ||
            name in CommandMagic.bibliographyReference || name in CommandMagic.glossaryEntry
        ) {
            return this
        }
        // definition
        if(name in CommandMagic.definitions) {
            return this
        }
        // environment labels
        val environmentName = this.firstParentOfType(LatexEnvironment::class)?.getEnvironmentName()
        if (EnvironmentMagic.labelAsParameter.contains(environmentName)
        ) {
            return this
        }
        // begin/end
        if (this.firstParentOfType(LatexEndCommand::class) != null ||
            this.firstParentOfType(LatexBeginCommand::class) != null
        ) {
            return this
        }

        return null
    }

    private fun setPlainTextName(name: String) {
        val newElement = LatexPsiHelper(this.project).createFromText(name)
        val normalText = newElement.findFirstChildTyped<LatexNormalText>()?.node
        require(normalText != null) {
            "Expected NORMAL_TEXT, but got null."
        }
//        require(normalText.text == name){
//            "Expected NORMAL_TEXT to have text '$name', but got '${normalText.text}' instead."
//        }
        this.node.replaceAllChildrenToChildrenOf(normalText)
        return
    }

    private fun setBracedName(name: String): PsiElement {
        if(this.parent is LatexParameterGroupText) {
            // already inside a group, so we can just set the name
            setPlainTextName(name)
            return this
        }
        val originalContent = firstParentOfType(LatexKeyValContent::class) ?: return this
        val content = LatexPsiHelper(this.project).createFromText("\\cmd[label=$name]")
            .firstChild // The first child is the command with parameters
            .findFirstChildTyped<LatexKeyValContent>() ?: return this
//        originalContent.node.replaceAllChildrenToChildrenOf(content.node)
        val newNode = content.firstChild ?: return this
        return originalContent.firstChild.replace(newNode)
    }

    override fun setName(name: String): PsiElement {
        val command = this.firstParentOfType(LatexCommands::class)
        val environment = this.firstParentOfType(LatexEnvironment::class)

        if(command?.name in CommandMagic.labelAsParameter || environment?.getEnvironmentName() in EnvironmentMagic.labelAsParameter) {
            // we need to keep the pair of braces around the label name
            return setBracedName(name)
        }
        /**
         * Build a new PSI this where [old] is replaced with [new] and replace the old PSI this
         */
        fun replaceInCommand(command: LatexCommands?, old: String, new: String) {
            // This could go wrong in so many cases
            val labelText = command?.text?.replaceFirst(old, new) ?: "${command?.name}{$new}"
            val newElement = LatexPsiHelper(this.project).createFromText(labelText).firstChild
            val oldNode = command?.node
            val newNode = newElement.node
            if (oldNode == null) {
                command?.parent?.node?.addChild(newNode)
            }
            else {
                command.parent.node.replaceChild(oldNode, newNode)
            }
        }

        // If the label name is inside a group, keep the group
        // If we want to rename a label
        if (CommandMagic.glossaryReference.contains(command?.name) || CommandMagic.glossaryEntry.contains(command?.name)) {
            // This assumes that glossary entry commands (e.g. \newglossaryentry) as well as glossary references (e.g. \gls)
            // have the glossary label as their first required parameter. This is true for all currently supported glossary
            // commands, but might change in the future.
            if (command != null) {
                val glossaryLabel = LatexGlossariesCommand.extractGlossaryLabel(command) ?: ""
                replaceInCommand(command, glossaryLabel, name)
            }
            return this
        }
        // Else, we just replace the label name in the command
        setPlainTextName(name)
        return this
    }

    override fun getName(): String {
        return this.text ?: ""
    }

    override fun delete() {
        val cmd = this.parentOfType(LatexCommands::class) ?: return
        if (cmd.isFigureLabel()) {
            // Look for the NoMathContent that is around the environment, because that is the PsiElement that has the
            // whitespace and other normal text as siblings.
            cmd.parentOfType(LatexEnvironment::class)
                ?.parentOfType(LatexNoMathContent::class)
                ?.remove()
        }
    }
}