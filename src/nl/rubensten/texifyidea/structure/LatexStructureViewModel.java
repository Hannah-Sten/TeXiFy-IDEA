package nl.rubensten.texifyidea.structure;

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import nl.rubensten.texifyidea.structure.filter.LatexLabelFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ruben Schellekens
 */
public class LatexStructureViewModel extends StructureViewModelBase implements ElementInfoProvider {

    public static final Sorter[] SORTERS = new Sorter[] { Sorter.ALPHA_SORTER };
    public static final Filter[] FILTERS = new Filter[] {
            new LatexLabelFilter()
    };

    public LatexStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        super(psiFile, editor, new LatexStructureViewElement(psiFile));
        getFilters();
    }

    @NotNull
    @Override
    public Sorter[] getSorters() {
        return SORTERS;
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return FILTERS;
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
        return false;
    }
}
