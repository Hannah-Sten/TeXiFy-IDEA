// This is a generated file. Not intended for manual editing.
package nl.hannahsten.texifyidea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static nl.hannahsten.texifyidea.psi.LatexTypes.*;
import nl.hannahsten.texifyidea.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub;

public class LatexCommandsImpl extends LatexCommandsImplMixin implements LatexCommands {

  public LatexCommandsImpl(ASTNode node) {
    super(node);
  }

  public LatexCommandsImpl(LatexCommandsStub stub, IStubElementType stubType) {
    super(stub, stubType);
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

}
