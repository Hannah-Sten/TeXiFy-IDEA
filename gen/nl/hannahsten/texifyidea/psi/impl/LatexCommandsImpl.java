// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import nl.hannahsten.texifyidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static nl.hannahsten.texifyidea.psi.LatexTypes.COMMAND_TOKEN;

public class LatexCommandsImpl extends LatexCommandsImplMixin implements LatexCommands {

  public LatexCommandsImpl(@NotNull LatexCommandsStub stub, @NotNull IStubElementType type) {
    super(stub, type);
  }

  public LatexCommandsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LatexCommandsImpl(LatexCommandsStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitCommands(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof LatexVisitor) accept((LatexVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<LatexParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, LatexParameter.class);
  }

  @Override
  @NotNull
  public PsiElement getCommandToken() {
    return notNullChild(findChildByType(COMMAND_TOKEN));
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return LatexPsiImplUtil.getReferences(this);
  }

  @Override
  public List<String> getOptionalParameters() {
    return LatexPsiImplUtil.getOptionalParameters(this);
  }

  @Override
  public List<String> getRequiredParameters() {
    return LatexPsiImplUtil.getRequiredParameters(this);
  }

  @Override
  public boolean hasLabel() {
    return LatexPsiImplUtil.hasLabel(this);
  }

  @Override
  public String getName() {
    return LatexPsiImplUtil.getName(this);
  }

}
