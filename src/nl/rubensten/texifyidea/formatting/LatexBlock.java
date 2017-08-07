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
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
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
            if (child.getElementType() != TokenType.WHITE_SPACE) {
                Block block = new LatexBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        spacingBuilder
                );
                blocks.add(block);
            }

            child = child.getTreeNext();
        }

        return blocks;
    }

    @Override
    public Indent getIndent() {
        // Fix for leading comments inside an environment, because somehow they are not placed inside environments
        if (myNode.getElementType() == LatexTypes.ENVIRONMENT_CONTENT || (myNode.getElementType() == LatexTypes.COMMENT_TOKEN && myNode.getTreeParent().getElementType() == LatexTypes.ENVIRONMENT)) {
            return Indent.getNormalIndent(true);
        }

        // Displaymath
        if ((myNode.getElementType() == LatexTypes.MATH_CONTENT || myNode.getElementType() == LatexTypes.COMMENT_TOKEN) && myNode.getTreeParent().getElementType() == LatexTypes.DISPLAY_MATH) {
            return Indent.getNormalIndent(true);
        }
        return Indent.getNoneIndent();
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

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        IElementType type = myNode.getElementType();
        if (type == LatexTypes.DISPLAY_MATH) {
            return new ChildAttributes(Indent.getNormalIndent(true), null);
        }
        else if (type == LatexTypes.ENVIRONMENT) {
            return new ChildAttributes(Indent.getNormalIndent(true), null);
        }

        return new ChildAttributes(Indent.getNoneIndent(), null);
    }
}
