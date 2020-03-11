package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import nl.hannahsten.texifyidea.reference.BibtexIdReference
import nl.hannahsten.texifyidea.reference.LatexEnvironmentReference
import nl.hannahsten.texifyidea.reference.LatexLabelParameterReference
import nl.hannahsten.texifyidea.util.Magic
import nl.hannahsten.texifyidea.util.firstParentOfType

/**
 * If the normal text is the parameter of a \ref-like command, get the references to the label declaration.
 */
@Suppress("RemoveExplicitTypeArguments") // Somehow they are needed
fun getReferences(element: LatexNormalText): Array<PsiReference> {
    val command = element.firstParentOfType(LatexCommands::class)
    // If the command is a label reference
    return if (Magic.Command.labelReference.contains(command?.name)) {
        val reference = LatexLabelParameterReference(element)
        if (reference.multiResolve(false).isNotEmpty()) {
            arrayOf<PsiReference>(reference)
        }
        else {
            emptyArray<PsiReference>()
        }
    }
    // If the command is a bibliography reference
    else if (Magic.Command.bibliographyReference.contains(command?.name)) {
        val reference = BibtexIdReference(element)
        if (reference.multiResolve(false).isNotEmpty()) {
            arrayOf<PsiReference>(reference)
        }
        else {
            emptyArray<PsiReference>()
        }
    }
    // If the command is an \end command (references to \begin)
    else if (element.firstParentOfType(LatexEndCommand::class) != null) {
        arrayOf<PsiReference>(LatexEnvironmentReference(element))
    }
    else {
        emptyArray<PsiReference>()
    }
}

/**
 * If [getReferences] returns one reference return that one, null otherwise.
 */
fun getReference(element: LatexNormalText): PsiReference? {
    val references = getReferences(element)
    return if (references.size != 1) {
        null
    }
    else {
        references[0]
    }
}

fun getNameIdentifier(element: LatexNormalText): PsiElement {
    return element
}

fun setName(element: LatexNormalText, name: String): PsiElement {
    val command = element.firstParentOfType(LatexCommands::class)
    // If we want to rename a label
    if (Magic.Command.reference.contains(command?.name) || Magic.Command.labelDefinition.contains(command?.name)) {
        // Get a new psi element for the complete label command (\label included),
        // because if we replace the complete command instead of just the normal text
        // then the indices will be updated, which is necessary for the reference resolve to work
        val labelText = "${command?.name}{$name}"
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

fun getName(element: LatexNormalText): String {
    return element.text ?: ""
}