package nl.hannahsten.texifyidea.psi;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import nl.hannahsten.texifyidea.index.stub.BibtexIdStub;
import org.jetbrains.annotations.NotNull;

/**
 * This class is a mixin for BibtexIdImpl. We use a separate mixin class instead of [BibtexPsiImplUtil] because we need to add an instance variable
 * in order to implement [getName] and [setName] correctly.
 */
public class BibtexIdImplMixin extends StubBasedPsiElementBase<BibtexIdStub> implements PsiNamedElement {

    private String identifier;

    public BibtexIdImplMixin(@NotNull BibtexIdStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public BibtexIdImplMixin(@NotNull ASTNode node) {
        super(node);
    }

    public BibtexIdImplMixin(BibtexIdStub stub, IElementType nodeType, ASTNode node) {
        super(stub, nodeType, node);
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return getIdentifier();
    }

    @Override
    public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
        this.identifier = s;
        return this;
    }

    @Override
    public String toString() {
        return "BibtexId{" + getName() + "}";
    }
}
