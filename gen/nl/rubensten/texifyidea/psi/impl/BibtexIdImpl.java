// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import nl.rubensten.texifyidea.index.stub.BibtexIdStub;
import nl.rubensten.texifyidea.psi.BibtexComment;
import nl.rubensten.texifyidea.psi.BibtexId;
import nl.rubensten.texifyidea.psi.BibtexVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BibtexIdImpl extends StubBasedPsiElementBase<BibtexIdStub> implements BibtexId {

    private String identifier;

    public BibtexIdImpl(ASTNode node) {
        super(node);
    }

    public BibtexIdImpl(BibtexIdStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(@NotNull BibtexVisitor visitor) {
        visitor.visitId(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BibtexVisitor) {
            accept((BibtexVisitor)visitor);
        }
        else {
            super.accept(visitor);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    @NotNull
    public List<BibtexComment> getCommentList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, BibtexComment.class);
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
