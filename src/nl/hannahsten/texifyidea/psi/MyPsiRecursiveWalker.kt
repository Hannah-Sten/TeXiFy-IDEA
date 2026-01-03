package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
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

/**
 * Defines a recursive walker for PSI elements, allowing for custom traversal behavior.
 *
 *
 * Consider the following tree structure:
 *
 * ```
 * root
 *  ├── 1
 *  │   ├── 1.1
 *  │   └── 1.2
 *  └── 2
 * ```
 *
 * The walker will visit the elements in the following order:
 * ```
 * root start
 * * 1 start
 *   * 1.1 start end
 *   * 1.2 start end
 * * 1 end
 * * 2 start end
 * root end
 * ```
 */
abstract class MyPsiRecursiveWalker(private var depth: Int) : PsiRecursiveElementVisitor() {

    var isWalkingStopped = false
        protected set

    protected var goDown = true

    final override fun visitElement(element: PsiElement) {
        if (isWalkingStopped) return
        ProgressIndicatorProvider.checkCanceled()
        goDown = true
        elementStart(element)
        if (isWalkingStopped) return
        if(depth > 0 && goDown) {
            depth--
            element.acceptChildren(this)
            depth++
        }
        elementEnd(element)
    }

    protected fun stopWalking() {
        isWalkingStopped = true
    }

    @Suppress("unused")
    protected fun stopGoingDown() {
        goDown = false
    }

    /**
     *
     */
    protected open fun elementStart(e: PsiElement) = Unit

    protected open fun elementEnd(e: PsiElement) = Unit
}

abstract class LatexPsiRecursiveWalker(depth: Int) : MyPsiRecursiveWalker(depth) {

    override fun elementStart(e: PsiElement) {
        if(e is LatexNormalText || e is PsiWhiteSpace) {
            // Skip normal text and whitespace elements
            goDown = false
        }
    }
}