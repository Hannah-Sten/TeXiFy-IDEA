// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import nl.hannahsten.texifyidea.psi.LatexCommandsImplMixin;
import nl.hannahsten.texifyidea.psi.*;
import java.util.LinkedHashMap;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

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
  public LinkedHashMap<LatexKeyValKey, LatexKeyValValue> getOptionalParameterMap() {
    return LatexPsiImplUtil.getOptionalParameterMap(this);
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
