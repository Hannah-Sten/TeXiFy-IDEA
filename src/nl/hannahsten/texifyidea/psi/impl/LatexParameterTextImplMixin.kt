package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.lang.commands.LatexGlossariesCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.BibtexIdReference
import nl.hannahsten.texifyidea.reference.LatexEnvironmentReference
import nl.hannahsten.texifyidea.reference.LatexGlossaryReference
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.isFigureLabel
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import nl.hannahsten.texifyidea.util.psi.firstParentOfType
import nl.hannahsten.texifyidea.util.psi.parentOfType
import nl.hannahsten.texifyidea.util.psi.remove

abstract class LatexParameterTextImplMixin(node: ASTNode) : LatexParameterText, ASTWrapperPsiElement(node) {

    /**
     * If the normal text is the parameter of a \ref-like command, get the references to the label declaration.
     */
    override fun getReferences(): Array<PsiReference> {
        // If the command is a label reference
        // NOTE When adding options here, also update getNameIdentifier below
        return when {
            this.project.getLabelReferenceCommands().contains(this.firstParentOfType(LatexCommands::class)?.name) -> {
                arrayOf<PsiReference>(LatexLabelParameterReference(this))
            }
            // If the command is a bibliography reference
            CommandMagic.bibliographyReference.contains(this.firstParentOfType(LatexCommands::class)?.name) -> {
                arrayOf<PsiReference>(BibtexIdReference(this))
            }
            // If the command is an \end command (references to \begin)
            this.firstParentOfType(LatexEndCommand::class) != null -> {
                arrayOf<PsiReference>(LatexEnvironmentReference(this))
            }
            // If the command is a glossary reference
            CommandMagic.glossaryReference.contains(this.firstParentOfType(LatexCommands::class)?.name) -> {
                arrayOf<PsiReference>(LatexGlossaryReference(this))
            }

            else -> {
                emptyArray<PsiReference>()
            }
        }
    }


    /**
     * If [getReferences] returns one reference return that one, null otherwise.
     */
    override fun getReference(): PsiReference? {
        return references.firstOrNull()
    }

    override fun getNameIdentifier(): PsiElement? {
        // Because we do not want to trigger the NonAsciiCharactersInspection when the LatexParameterText is not an identifier
        // (think non-ASCII characters in a \section command), we return null here when the this is not an identifier
        // It is important not to return null for any identifier, otherwise exceptions like "Throwable: null byMemberInplaceRenamer" may occur
        val name = this.firstParentOfType(LatexCommands::class)?.name
        val environmentName = this.firstParentOfType(LatexEnvironment::class)?.getEnvironmentName()
        if (!CommandMagic.labelReferenceWithoutCustomCommands.contains(name) &&
            !CommandMagic.labelDefinitionsWithoutCustomCommands.contains(name) &&
            !CommandMagic.bibliographyReference.contains(name) &&
            !CommandMagic.labelAsParameter.contains(name) &&
            !CommandMagic.glossaryEntry.contains(name) &&
            !EnvironmentMagic.labelAsParameter.contains(environmentName) &&
            this.firstParentOfType(LatexEndCommand::class) == null &&
            this.firstParentOfType(LatexBeginCommand::class) == null
        ) {
            return null
        }
        return this
    }


    override fun setName(name: String): PsiElement {
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

        val command = this.firstParentOfType(LatexCommands::class)
        val environment = this.firstParentOfType(LatexEnvironment::class)
        // If we want to rename a label
        if (CommandMagic.reference.contains(command?.name) || this.project.getLabelDefinitionCommands()
                .contains(command?.name)
        ) {
            // Get a new psi this for the complete label command (\label included),
            // because if we replace the complete command instead of just the normal text
            // then the indices will be updated, which is necessary for the reference resolve to work
            val oldLabel = this.extractLabelName()
            replaceInCommand(command, oldLabel, name)
        }
        else if (CommandMagic.labelAsParameter.contains(command?.name) || EnvironmentMagic.labelAsParameter.contains(
                environment?.getEnvironmentName()
            )
        ) {
            val helper = LatexPsiHelper(this.project)

            // If the label name is inside a group, keep the group
            val value = if (this.parentOfType(LatexParameterGroupText::class) != null) {
                "{$name}"
            }
            else {
                name
            }
            helper.setOptionalParameter(command ?: environment!!.beginCommand, "label", value)
        }
        else if (CommandMagic.glossaryReference.contains(command?.name) || CommandMagic.glossaryEntry.contains(command?.name)) {
            // This assumes that glossary entry commands (e.g. \newglossaryentry) as well as glossary references (e.g. \gls)
            // have the glossary label as their first required parameter. This is true for all currently supported glossary
            // commands, but might change in the future.
            if (command != null) {
                val glossaryLabel = LatexGlossariesCommand.extractGlossaryLabel(command) ?: ""
                replaceInCommand(command, glossaryLabel, name)
            }
        }
        else if (this.firstParentOfType(LatexEndCommand::class) != null || this.firstParentOfType(LatexBeginCommand::class) != null) {
            // We are renaming an environment, text in \begin or \end
            val newElement = LatexPsiHelper(this.project).createFromText(name).firstChild
            val oldNode = this.node
            val newNode = newElement.node
            if (oldNode == null) {
                this.parent.node.addChild(newNode)
            }
            else {
                this.parent.node.replaceChild(oldNode, newNode)
            }
        }
        // Else, this is not renamable

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