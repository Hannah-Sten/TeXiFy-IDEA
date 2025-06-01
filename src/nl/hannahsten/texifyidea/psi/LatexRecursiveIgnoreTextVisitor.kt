package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace

/**
 * A visitor that ignores all text elements and only visits commands and magic comments.
 */
abstract class LatexRecursiveIgnoreTextVisitor : LatexVisitor() {

    override fun visitNormalText(o: LatexNormalText) {
        // Do nothing, we only want to visit commands
        return
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        return
    }

    override fun visitCommands(o: LatexCommands) {
        ProgressIndicatorProvider.checkCanceled()
        o.acceptChildren(this)
    }

    /**
     * A magic comment may serve as a command, so we also visit it.
     */
    override fun visitMagicComment(o: LatexMagicComment) {
        ProgressIndicatorProvider.checkCanceled()
        o.acceptChildren(this)
    }

    /**
     * Override more specific visit methods if needed.
     */
    override fun visitComposite(o: LatexComposite) {
        ProgressIndicatorProvider.checkCanceled()
        o.acceptChildren(this)
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }
}

class LatexCommandTraverser(private val action: (LatexCommands) -> Unit) : LatexRecursiveIgnoreTextVisitor() {

    override fun visitCommands(o: LatexCommands) {
        action(o)
        o.acceptChildren(this)
    }

    override fun visitMagicComment(o: LatexMagicComment) {
        // Do nothing, we only want to visit commands
        o.acceptChildren(this)
    }
}

class LatexCommandTraverserStoppable(private val action: (LatexCommands) -> Boolean) : LatexRecursiveIgnoreTextVisitor() {

    private var stopTraversal = false

    override fun visitElement(element: PsiElement) {
        if (stopTraversal) return
        super.visitElement(element)
    }

    override fun visitCommands(o: LatexCommands) {
        if (stopTraversal) return
        if(!action(o)) {
            stopTraversal = true
            return
        }
        o.acceptChildren(this)
    }

    override fun visitMagicComment(o: LatexMagicComment) {
        if (stopTraversal) return
        // Do nothing, we only want to visit commands
        o.acceptChildren(this)
    }
}