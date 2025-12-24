package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.util.parser.firstParentOfType

abstract class BibtexIdImplMixin(node: ASTNode) : BibtexId, ASTWrapperPsiElement(node) {

    override fun getNameIdentifier(): PsiElement = this

    override fun setName(name: String): PsiElement {
        // Replace the complete bibtex entry to automatically update the index (which is on entries, not ids)
        val entry = this.firstParentOfType(BibtexEntry::class)
        val oldName = this.name
        val newText = entry?.text?.replaceFirst(oldName, name) ?: return this
        val newElement = PsiFileFactory.getInstance(this.project).createFileFromText("DUMMY.tex", BibtexLanguage, newText, false, true).firstChild
        entry.parent.node.replaceChild(entry.node, newElement.node)
        return this
    }

    override fun getName(): String = this.text

    override fun delete() {
        val parent = firstParentOfType<BibtexEntry>() ?: return
        // remove the whole entry, not just the id
        parent.delete()
//        super.delete()
        // removing the reference is done separately by usage search
    }
}