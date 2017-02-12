package nl.rubensten.texifyidea.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.formatter.common.AbstractBlock;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sten Wessel
 */
public class LatexBlock extends AbstractBlock {

    private SpacingBuilder spacingBuilder;

    protected LatexBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment
            alignment, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();

        while (child != null) {
            if (child.getElementType() == LatexTypes.DISPLAY_MATH) {
                Block block = new LatexBlock(
                        child,
                        Wrap.createWrap(WrapType.ALWAYS, true),
                        myAlignment,
                        spacingBuilder
                );

                blocks.add(block);
            }

            child.getTreeNext();
        }

        return blocks;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        if (myNode.getElementType() == LatexTypes.DISPLAY_MATH) {
            return new ChildAttributes(Indent.getNormalIndent(true), Alignment.createAlignment());
        }

        return super.getChildAttributes(newChildIndex);
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }
}
