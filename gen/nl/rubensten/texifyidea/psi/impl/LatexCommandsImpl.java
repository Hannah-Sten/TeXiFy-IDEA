// This is a generated file. Not intended for manual editing.
package nl.rubensten.texifyidea.psi.impl;

import java.util.List;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import nl.rubensten.texifyidea.index.stub.LatexCommandsStub;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;

import static nl.rubensten.texifyidea.psi.LatexTypes.*;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import nl.rubensten.texifyidea.psi.*;

public class LatexCommandsImpl extends StubBasedPsiElementBase<LatexCommandsStub>
        implements LatexCommands {

    public LatexCommandsImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull LatexVisitor visitor) {
        visitor.visitCommands(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LatexVisitor) {
            accept((LatexVisitor)visitor);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    @NotNull
    public List<LatexParameter> getParameterList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexParameter.class);
    }

    @Override
    @NotNull
    public PsiElement getCommandToken() {
        return findNotNullChildByType(COMMAND_TOKEN);
    }

    @Override
    public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
        // TODO: Implement.
        return null;
    }

    @Override
    public IStubElementType getElementType() {
        // TODO: Implement.
        return null;
    }

    @Override
    public LatexCommandsStub getStub() {
        // TODO: Implement.
        return null;
    }
}
