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

    private static final TokenSet LATEX_ENVIRONMENT_DELIM = TokenSet.create(
            LatexTypes.BEGIN_COMMAND, LatexTypes.END_COMMAND
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
            else if (myNode.getElementType() == LatexTypes.ENVIRONMENT) {
                // Skip \begin and \end
                if (child != myNode.getFirstChildNode() && child != myNode.getLastChildNode()) {
                    indent = Indent.getNormalIndent(true);
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
        else if (myNode.getElementType() == LatexTypes.ENVIRONMENT) {
            return new ChildAttributes(Indent.getNormalIndent(), null);
        }

        return new ChildAttributes(Indent.getNoneIndent(), null);
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
