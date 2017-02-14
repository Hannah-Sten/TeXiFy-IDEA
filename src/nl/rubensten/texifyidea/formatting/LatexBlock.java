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
import com.intellij.psi.tree.TokenSet;
import nl.rubensten.texifyidea.file.LatexFile;
import nl.rubensten.texifyidea.psi.LatexTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static nl.rubensten.texifyidea.psi.LatexPsiUtil.hasElementType;

/**
 * @author Sten Wessel
 */
public class LatexBlock extends AbstractBlock {
    private static final TokenSet LATEX_DISPLAY_MATH_DELIM = TokenSet.create(
            LatexTypes.DISPLAY_MATH_START, LatexTypes.DISPLAY_MATH_END
    );

    private SpacingBuilder spacingBuilder;
    private Indent indent;

    protected LatexBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment
            alignment, Indent indent, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
        this.indent = indent;
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();

        while (child != null) {
            Alignment align = null;
            Wrap wrap = null;
            Indent indent = Indent.getNoneIndent();

            if (myNode.getElementType() == LatexTypes.DISPLAY_MATH) {
                if (!hasElementType(child, LATEX_DISPLAY_MATH_DELIM)) {
                    wrap = Wrap.createWrap(WrapType.ALWAYS, true);
                    indent = Indent.getNormalIndent();
                }

            }

            if (child.getElementType() != TokenType.WHITE_SPACE) {
                blocks.add(new LatexBlock(child, wrap, align, indent, spacingBuilder));
            }

            child = child.getTreeNext();
        }

        return blocks;
    }

    @Override
    public Indent getIndent() {
        return indent;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        if (myNode.getElementType() == LatexTypes.DISPLAY_MATH) {
            return new ChildAttributes(Indent.getNormalIndent(), null);
        }
        else if (myNode.getPsi() instanceof LatexFile) {
            return new ChildAttributes(Indent.getNoneIndent(), null);
        }

        return new ChildAttributes(null, null);
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
