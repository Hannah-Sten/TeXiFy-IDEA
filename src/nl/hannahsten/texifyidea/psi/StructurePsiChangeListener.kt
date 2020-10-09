package nl.hannahsten.texifyidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener

/**
 * @author Hannah Schellekens
 */
class StructurePsiChangeListener(val project: Project) : PsiTreeChangeListener {

    private fun updateTracker() {
        PsiManager.getInstance(project).dropPsiCaches()
    }

    override fun beforeChildAddition(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun beforeChildRemoval(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun beforeChildReplacement(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun beforeChildMovement(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun beforeChildrenChange(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun beforePropertyChange(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing.
    }

    override fun childAdded(psiTreeChangeEvent: PsiTreeChangeEvent) {
        updateTracker()
    }

    override fun childRemoved(psiTreeChangeEvent: PsiTreeChangeEvent) {
        updateTracker()
    }

    override fun childReplaced(psiTreeChangeEvent: PsiTreeChangeEvent) {
        updateTracker()
    }

    override fun childrenChanged(psiTreeChangeEvent: PsiTreeChangeEvent) {
        updateTracker()
    }

    override fun childMoved(psiTreeChangeEvent: PsiTreeChangeEvent) {
        updateTracker()
    }

    override fun propertyChanged(psiTreeChangeEvent: PsiTreeChangeEvent) {
        // Do nothing
    }
}
