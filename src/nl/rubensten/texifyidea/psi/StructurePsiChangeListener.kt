package nl.rubensten.texifyidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.impl.PsiModificationTrackerImpl

/**
 * @author Ruben Schellekens
 */
class StructurePsiChangeListener(project: Project) : PsiTreeChangeListener {

    private val psiModificationTracker = PsiManager
            .getInstance(project)
            .modificationTracker as PsiModificationTrackerImpl

    private fun updateTracker() {
        psiModificationTracker.incOutOfCodeBlockModificationCounter()
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
