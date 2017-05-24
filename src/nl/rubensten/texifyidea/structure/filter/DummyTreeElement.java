package nl.rubensten.texifyidea.structure.filter;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Ruben Schellekens
 */
public enum DummyTreeElement implements TreeElement {

    INSTANCE;

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return DummyPresentation.INSTANCE;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return TreeElement.EMPTY_ARRAY;
    }

    /**
     * @author Ruben Schellekens
     */
    private static class DummyPresentation implements ItemPresentation {

        private static final ItemPresentation INSTANCE = new DummyPresentation();

        @Nullable
        @Override
        public String getPresentableText() {
            return null;
        }

        @Nullable
        @Override
        public String getLocationString() {
            return null;
        }

        @Nullable
        @Override
        public Icon getIcon(boolean b) {
            return null;
        }
    }
}
