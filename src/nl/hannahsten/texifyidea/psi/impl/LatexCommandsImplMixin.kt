package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.reference.CommandDefinitionReference
import nl.hannahsten.texifyidea.util.labels.getLabelReferenceCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.psi.*

/**
 * This class is a mixin for LatexCommandsImpl. We use a separate mixin class instead of [LatexPsiImplUtil] because we need to add an instance variable
 * in order to implement [getName] and [setName] correctly.
 */
abstract class LatexCommandsImplMixin : StubBasedPsiElementBase<LatexCommandsStub?>, PsiNameIdentifierOwner, LatexCommands, LatexCommandWithParams {

    @JvmField
    var name: String? = null

    constructor(stub: LatexCommandsStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
    constructor(stub: LatexCommandsStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)

    override fun toString(): String {
        return "LatexCommandsImpl(COMMANDS)[STUB]{" + getName() + "}"
    }

    override fun getTextOffset(): Int {
        val name = getName()
        return if (name == null) {
            super.getTextOffset()
        }
        else {
            val offset = node.text.indexOf(name)
            if (offset == -1) super.getTextOffset() else node.startOffset + offset
        }
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is LatexVisitor) {
            accept(visitor)
        }
        else {
            super.accept(visitor)
        }
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }


    override fun getName(): String? {
        val stub = this.stub
        return if (stub != null) stub.name else this.commandToken.text
    }

    override fun setName(newName: String): PsiElement {
        var newText = this.text.replace(getName() ?: return this, newName)
        if (!newText.startsWith("\\"))
            newText = "\\" + newText
        val newElement = LatexPsiHelper(this.project).createFromText(newText).firstChild
        val oldNode = this.node
        val newNode = newElement.node
        if (oldNode == null) {
            this.parent?.node?.addChild(newNode)
        }
        else {
            this.parent?.node?.replaceChild(oldNode, newNode)
        }
        return this
    }

    /**
     * References which do not need a find usages to work on lower level psi elements (normal text) can be implemented on the command, otherwise they are in {@link LatexPsiImplUtil#getReference(LatexParameterText)}.
     * For more info and an example, see {@link nl.hannahsten.texifyidea.reference.LatexLabelParameterReference}.
     */
    override fun getReferences(): Array<PsiReference> {
        val requiredParameters = getRequiredParameters(this)
        val firstParam = requiredParameters.getOrNull(0)

        val references = mutableListOf<PsiReference>()

        // If it is a reference to a label (used for autocompletion, do not confuse with reference resolving from LatexParameterText)
        if (this.project.getLabelReferenceCommands().contains(this.commandToken.text) && firstParam != null) {
            references.addAll(extractLabelReferences(this, requiredParameters))
        }

        // If it is a reference to a file
        references.addAll(this.getFileArgumentsReferences())

        if (CommandMagic.urls.contains(this.getName()) && firstParam != null) {
            references.addAll(this.extractUrlReferences(firstParam))
        }

        // Else, we assume the command itself is important instead of its parameters,
        // and the user is interested in the location of the command definition
        val definitionReference = CommandDefinitionReference(this)
        // Only create a reference if there is something to resolve to, otherwise autocompletion won't work
        if (definitionReference.multiResolve(false).isNotEmpty()) {
            references.add(definitionReference)
        }

        return references.toTypedArray()
    }

    /**
     * Get the reference for this command, assuming it has exactly one reference (return null otherwise).
     */
    override fun getReference(): PsiReference? {
        return this.references.firstOrNull()
    }

    override fun getOptionalParameterMap() = getOptionalParameterMapFromParameters(this.parameterList)

    override fun getRequiredParameters() = getRequiredParameters(this.parameterList)
}
