// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub;
import nl.hannahsten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LatexEnvironmentImpl extends StubBasedPsiElementBase<LatexEnvironmentStub> implements LatexEnvironment {

  public LatexEnvironmentImpl(@NotNull LatexEnvironmentStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LatexEnvironmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LatexEnvironmentImpl(LatexEnvironmentStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitEnvironment(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor) visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public LatexBeginCommand getBeginCommand() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, LatexBeginCommand.class));
  }

  @Override
  @Nullable
  public LatexEndCommand getEndCommand() {
    return PsiTreeUtil.getChildOfType(this, LatexEndCommand.class);
  }

  @Override
  @Nullable
  public LatexEnvironmentContent getEnvironmentContent() {
    return PsiTreeUtil.getChildOfType(this, LatexEnvironmentContent.class);
  }

  @Override
  public String getEnvironmentName() {
    return LatexPsiImplUtil.getEnvironmentName(this);
  }

  @Override
  public boolean hasLabel() {
    return LatexPsiImplUtil.hasLabel(this);
  }

}
