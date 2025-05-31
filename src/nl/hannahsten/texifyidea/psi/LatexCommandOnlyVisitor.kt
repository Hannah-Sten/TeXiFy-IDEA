package nl.hannahsten.texifyidea.psi

import com.intellij.psi.PsiElement

/**
 * A visitor that only visits [LatexCommands] elements.
 */
abstract class LatexCommandOnlyVisitor : LatexVisitor(){

    override fun visitNormalText(o: LatexNormalText) {
        // Do nothing, we only want to visit commands
        return
    }

    /**
     * Should
     */
    abstract override fun visitCommands(o: LatexCommands)

    /**
     * A magic comment may serve as a command, so we also visit it.
     */
    abstract override fun visitMagicComment(o: LatexMagicComment)

    final override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        element.acceptChildren(this)
    }
}