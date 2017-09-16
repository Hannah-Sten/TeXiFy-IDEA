package nl.rubensten.texifyidea.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ruben Schellekens
 */
public class StructurePsiChangeListener implements PsiTreeChangeListener {

    private PsiModificationTrackerImpl psiModificationTracker;

    public StructurePsiChangeListener(@NotNull Project project) {
        this.psiModificationTracker = (PsiModificationTrackerImpl)PsiManager
                .getInstance(project)
                .getModificationTracker();
    }

    private void updateTracker() {
        psiModificationTracker.incOutOfCodeBlockModificationCounter();
    }

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing.
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTracker();
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTracker();
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTracker();
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTracker();
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTracker();
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        // Do nothing
    }
}
