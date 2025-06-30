package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.reference.LatexCommandDefinitionReference
import nl.hannahsten.texifyidea.structure.latex.LatexPresentationFactory
import nl.hannahsten.texifyidea.util.parser.*

/**
 * This class is a mixin for LatexCommandsImpl.
 */
abstract class LatexCommandsImplMixin : StubBasedPsiElementBase<LatexCommandsStub>, PsiNameIdentifierOwner, LatexCommands {
    constructor(stub: LatexCommandsStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
    constructor(stub: LatexCommandsStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)

    override fun toString(): String {
        return "LatexCommandsImpl(COMMANDS)[STUB]{$name}"
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

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    /**
     * Get the name of the command, for example `\alpha`.
     */
    override fun getName(): String? {
        val stub = this.greenStub
        return if (stub != null) stub.name else this.commandToken.text
    }

    override fun setName(newName: String): PsiElement {
        var newText = this.text.replace(name ?: return this, newName)
        if (!newText.startsWith("\\"))
            newText = "\\" + newText
        val newElement = LatexPsiHelper(this.project).createFromText(newText).firstChild
        val oldNode = this.node
        val newNode = newElement.node
        this.parent?.node?.replaceChild(oldNode, newNode)
        return this
    }

    /**
     * Gets the references for this command.
     */
    override fun getReferences(): Array<PsiReference> {
//        val requiredParameters = getRequiredParameters(this)
//        val firstParam = requiredParameters.getOrNull(0)
        // If it is a reference to a label (used for autocompletion, do not confuse with reference resolving from LatexParameterText)
//        val name = name
//        if(name in CommandMagic.reference) {
//
//        }
//        if (this.project.getLabelReferenceCommands().contains(this.commandToken.text) && firstParam != null) {
//            // To improve performance, don't continue with other reference types
//            extractLabelReferences(this, requiredParameters).let { if (it.isNotEmpty()) return it.toTypedArray() }
//        }
//
//        // If it is a reference to a file
//        this.getFileArgumentsReferences().let { if (it.isNotEmpty()) return it.toTypedArray() }
//
//        if (CommandMagic.urls.contains(this.getName()) && firstParam != null) {
//            this.extractUrlReferences(firstParam).let { if (it.isNotEmpty()) return it }
//        }
        // We only deal with the command itself, not its parameters
        // and the user is interested in the location of the command definition
        return arrayOf(LatexCommandDefinitionReference(this))
//        // Only create a reference if there is something to resolve to, otherwise autocompletion won't work
//        if (definitionReference.multiResolve(false).isNotEmpty()) {
//            return arrayOf(definitionReference)
//        }
//
//        return arrayOf()
    }

    /**
     * Get the reference for this command, assuming it has exactly one reference (return null otherwise).
     */
    override fun getReference(): PsiReference? {
        return this.references.firstOrNull()
    }

    override fun getPresentation(): ItemPresentation? {
        return LatexPresentationFactory.getPresentation(this)
    }

    override fun getOptionalParameterMap() = getOptionalParameterMapFromParameters(this.parameterList)

    override fun requiredParametersText(): List<String> {
        this.greenStub?.let { return it.requiredParams }
        return super.requiredParametersText()
    }

    override fun requiredParameterText(idx: Int): String? {
        this.greenStub?.let { return it.requiredParams.getOrNull(idx) }
        return super.requiredParameterText(idx)
    }
}
