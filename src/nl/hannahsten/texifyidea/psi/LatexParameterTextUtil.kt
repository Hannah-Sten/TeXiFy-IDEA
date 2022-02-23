package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.reference.BibtexIdReference
import nl.hannahsten.texifyidea.reference.LatexEnvironmentReference
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.labels.extractLabelName
import nl.hannahsten.texifyidea.util.labels.getLabelDefinitionCommands
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic

/**
 * If the normal text is the parameter of a \ref-like command, get the references to the label declaration.
 */
@Suppress("RemoveExplicitTypeArguments") // Somehow they are needed
fun getReferences(element: LatexParameterText): Array<PsiReference> {
    // If the command is a label reference
    // NOTE When adding options here, also update getNameIdentifier below
    return when {
        element.project.getLabelReferenceCommands().contains(element.firstParentOfType(LatexCommands::class)?.name) -> {
            arrayOf<PsiReference>(LatexLabelParameterReference(element))
        }
        // If the command is a bibliography reference
        CommandMagic.bibliographyReference.contains(element.firstParentOfType(LatexCommands::class)?.name) -> {
            arrayOf<PsiReference>(BibtexIdReference(element))
        }
        // If the command is an \end command (references to \begin)
        element.firstParentOfType(LatexEndCommand::class) != null -> {
            arrayOf<PsiReference>(LatexEnvironmentReference(element))
        }
        else -> {
            emptyArray<PsiReference>()
        }
    }
}

/**
 * If [getReferences] returns one reference return that one, null otherwise.
 */
fun getReference(element: LatexParameterText): PsiReference? {
    val references = getReferences(element)
    return if (references.size != 1) {
        null
    }
    else {
        references[0]
    }
}

fun getNameIdentifier(element: LatexParameterText): PsiElement? {
    // Because we do not want to trigger the NonAsciiCharactersInspection when the LatexParameterText is not an identifier
    // (think non-ASCII characters in a \section command), we return null here when the element is not an identifier
    // It is important not to return null for any identifier, otherwise exceptions like "Throwable: null byMemberInplaceRenamer" may occur
    val name = element.firstParentOfType(LatexCommands::class)?.name
    val environmentName = element.firstParentOfType(LatexEnvironment::class)?.environmentName
    if (!CommandMagic.labelReferenceWithoutCustomCommands.contains(name) &&
        !CommandMagic.labelDefinitionsWithoutCustomCommands.contains(name) &&
        !CommandMagic.bibliographyReference.contains(name) &&
        !CommandMagic.labelAsParameter.contains(name) &&
        !EnvironmentMagic.labelAsParameter.contains(environmentName) &&
        element.firstParentOfType(LatexEndCommand::class) == null &&
        element.firstParentOfType(LatexBeginCommand::class) == null
    ) {
        return null
    }
    return element
}

fun setName(element: LatexParameterText, name: String): PsiElement {
    val command = element.firstParentOfType(LatexCommands::class)
    val environment = element.firstParentOfType(LatexEnvironment::class)
    // If we want to rename a label
    if (CommandMagic.reference.contains(command?.name) || element.project.getLabelDefinitionCommands().contains(command?.name)) {
        // Get a new psi element for the complete label command (\label included),
        // because if we replace the complete command instead of just the normal text
        // then the indices will be updated, which is necessary for the reference resolve to work
        val oldLabel = element.extractLabelName()
        // This could go wrong in so many cases
        val labelText = command?.text?.replaceFirst(oldLabel, name) ?: "${command?.name}{$name}"
        val newElement = LatexPsiHelper(element.project).createFromText(labelText).firstChild
        val oldNode = command?.node
        val newNode = newElement.node
        if (oldNode == null) {
            command?.parent?.node?.addChild(newNode)
        }
        else {
            command.parent.node.replaceChild(oldNode, newNode)
        }
    }
    else if (CommandMagic.labelAsParameter.contains(command?.name) || EnvironmentMagic.labelAsParameter.contains(
            environment?.environmentName
        )
    ) {
        val helper = LatexPsiHelper(element.project)

        // If the label name is inside a group, keep the group
        val value = if (element.parentOfType(LatexParameterGroupText::class) != null) {
            "{$name}"
        }
        else {
            name
        }
        helper.setOptionalParameter(command ?: environment!!.beginCommand, "label", value)
    }
    else if (element.firstParentOfType(LatexEndCommand::class) != null || element.firstParentOfType(LatexBeginCommand::class) != null) {
        // We are renaming an environment, text in \begin or \end
        val newElement = LatexPsiHelper(element.project).createFromText(name).firstChild
        val oldNode = element.node
        val newNode = newElement.node
        if (oldNode == null) {
            element.parent.node.addChild(newNode)
        }
        else {
            element.parent.node.replaceChild(oldNode, newNode)
        }
    }
    // Else, element is not renamable

    return element
}

fun getName(element: LatexParameterText): String {
    return element.text ?: ""
}

val LatexParameterText.command: PsiElement?
    get() {
        return this.firstParentOfType(LatexCommands::class)?.firstChild
    }

fun delete(element: LatexParameterText) {
    val cmd = element.parentOfType(LatexCommands::class) ?: return
    if (cmd.isFigureLabel()) {
        // Look for the NoMathContent that is around the environment, because that is the PsiElement that has the
        // whitespace and other normal text as siblings.
        cmd.parentOfType(LatexEnvironment::class)
            ?.parentOfType(LatexNoMathContent::class)
            ?.remove()
    }
}
