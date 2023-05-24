// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import nl.hannahsten.texifyidea.psi.LatexCommands;
import nl.hannahsten.texifyidea.psi.LatexParameter;
import nl.hannahsten.texifyidea.psi.LatexPsiImplUtil;
import nl.hannahsten.texifyidea.psi.LatexVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static nl.hannahsten.texifyidea.psi.LatexTypes.COMMAND_TOKEN;

public class LatexCommandsImpl extends LatexCommandsImplMixin implements LatexCommands {

  public LatexCommandsImpl(@NotNull LatexCommandsStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public LatexCommandsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public LatexCommandsImpl(@Nullable LatexCommandsStub stub, @Nullable IElementType type, @Nullable ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull LatexVisitor visitor) {
    visitor.visitCommands(this);
  }

  @Override
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
  public List<String> getRequiredParameters() {
    return LatexPsiImplUtil.getRequiredParameters(this);
  }

  @Override
  public String getName() {
    return LatexPsiImplUtil.getName(this);
  }

  @Override
  public PsiElement setName(String name) {
    return LatexPsiImplUtil.setName(this, name);
  }

}
