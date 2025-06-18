package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import com.intellij.psi.PsiWhiteSpace

/**
 * A visitor that ignores all text elements.
 */
abstract class LatexRecursiveVisitor : LatexVisitor(), PsiRecursiveVisitor {

    override fun visitNormalText(o: LatexNormalText) {
        return
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        return
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        element.acceptChildren(this)
    }
}

abstract class LatexRecursiveDepthLimitedVisitor(private var depth: Int) : LatexRecursiveVisitor() {

    override fun visitElement(element: PsiElement) {
        if (depth <= 0) return
        ProgressIndicatorProvider.checkCanceled()
        depth--
        element.acceptChildren(this)
        depth++
    }
}

class LatexCompositeTraverser(
    private val action: (PsiElement) -> Boolean,
    depth: Int = Int.MAX_VALUE
) : LatexRecursiveDepthLimitedVisitor(depth) {

    var traversalStopped = false
        private set

    override fun visitNormalText(o: LatexNormalText) {
        if (traversalStopped) return
        ProgressIndicatorProvider.checkCanceled()
        if (!action(o)) {
            traversalStopped = true
            return
        }
    }

    override fun visitElement(element: PsiElement) {
        if (traversalStopped) return
        ProgressIndicatorProvider.checkCanceled()
        if(!action(element)) {
            traversalStopped = true
            return
        }
        super.visitElement(element)
    }
}
