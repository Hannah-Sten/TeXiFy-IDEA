package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.toArray
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.psi.getIdentifier
import nl.hannahsten.texifyidea.util.psi.getTagContent
import org.jetbrains.annotations.NonNls

abstract class BibtexEntryImplMixin : BibtexEntry, StubBasedPsiElementBase<BibtexEntryStub> {

    constructor(stub: BibtexEntryStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
    constructor(stub: BibtexEntryStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)

    /**
     * Get all the references from a BibTeX entry.
     */
    override fun getReferences(): Array<PsiReference> {
        val urls = CommandMagic.bibUrls
            .map { this.getTagContent(it) }
            .filter { it.isNotBlank() }

        if (urls.isNotEmpty()) {
            // We cannot simply return urls.map { WebReference(this, it) } because
            // then IJ cannot find the range of the url in the entry (and thus it
            // doesn't know which text to underline). So we have to return a list of
            // WebReference(this, text range) instead.
            val contentText = this.entryContent?.text ?: return emptyArray()
            val rangesInParent = urls.mapNotNull {
                if (it !in contentText) {
                    null
                }
                else {
                    TextRange.from(contentText.indexOf(it), it.length)
                }
            }

            return rangesInParent.map {
                WebReference(this, it.shiftRight(this.entryContent!!.textOffset - this.textOffset))
            }.toArray(emptyArray())
        }

        return emptyArray()
    }

    override fun setName(name: @NonNls String): PsiElement {
        return this
    }

    override fun getName(): String? {
        val stub = this.stub
        return if (stub != null) stub.name else this.getIdentifier()
    }

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    override fun toString(): String {
        return this.text
    }

}