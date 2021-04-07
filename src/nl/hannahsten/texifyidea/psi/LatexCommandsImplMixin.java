package nl.hannahsten.texifyidea.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a mixin for LatexCommandsImpl. We use a separate mixin class instead of [LatexPsiImplUtil] because we need to add an instance variable
 * in order to implement [getName] and [setName] correctly.
 */
public abstract class LatexCommandsImplMixin extends StubBasedPsiElementBase<LatexCommandsStub> implements PsiNameIdentifierOwner {

    public String name;

    public LatexCommandsImplMixin(@NotNull LatexCommandsStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public LatexCommandsImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    public LatexCommandsImplMixin(LatexCommandsStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    @Override
    public String toString() {
        return "LatexCommandsImpl(COMMANDS)[STUB]{" + getName() + "}";
    }

    @Override
    public int getTextOffset() {
        String name = getName();
        if (name == null) {
            return super.getTextOffset();
        }
        else {
            int offset = getNode().getText().indexOf(name);
            return offset == -1 ? super.getTextOffset() : getNode().getStartOffset() + offset;
        }
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LatexVisitor) {
            accept(visitor);
        }
        else {
            super.accept(visitor);
        }
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }
}
